package external;

import io.Constants;
import io.FastaWriter;
import io.FastqWriter;
import io.SamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import assembly.Fragment;
import external.AlignmentToolInterface.Options;
import external.AlignmentToolInterface.AlignmentResults;

public class SoapInterface extends AlignmentToolInterface
{
	public static final String SOAP_COMMAND = "soap";
	public static final String INDEX_COMMAND = "2bwt-builder";
	public static final String SOAP2SAM_COMMAND = "soap2sam";
	public static final String ALIGN_INDEX_OPTION = "-D";
	public static final String[] ALIGN_QUERY_OPTIONS = { "-a", "-b" };

	public SoapInterface(int index_, String description_, List<Integer> thresholds_,
		CharSequence sequence_, List<? extends Fragment> list_, Options o_,
		Map<String, Map<Integer, AlignmentResults>> m_)
	{
		super(index_, description_, thresholds_, sequence_, list_, o_, m_);
	}

	public void createIndex()
	{
		String index_filename = o.genome.getName() + ".index";
		File file_to_check = new File(o.genome.getParentFile(), o.genome.getName() + ".index.bwt");
		o.index = new File(o.genome.getParentFile(), index_filename);
		if (file_to_check.isFile())
		{
			System.out.printf("%03d: %s%n", index, "Index found; skipping");
		}
		else
		{
			ProcessBuilder pb = new ProcessBuilder(INDEX_COMMAND, o.genome.getAbsolutePath());
			pb.directory(o.genome.getParentFile());
			try
			{
				Process p = pb.start();
				BufferedReader stdout = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
				BufferedReader stderr = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));
				String line = null;
				while ((line = stdout.readLine()) != null)
				{
					System.out.printf("%03d: %s%n", index, line);
				}
				while ((line = stderr.readLine()) != null)
				{
					System.err.printf("%03d: %s%n", index, line);
				}
				p.waitFor();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void align()
	{
		System.out.printf("%03d: %s%n", index, "Aligning reads...");
		List<String> commands = new ArrayList<String>();
		commands.add(SOAP_COMMAND);
		commands.add(ALIGN_INDEX_OPTION);
		commands.add(o.index.getAbsolutePath());
		for (int i = 0; i < o.reads.size(); i++)
		{
			commands.add(ALIGN_QUERY_OPTIONS[i]);
			commands.add(o.reads.get(i).reads.getAbsolutePath());
		}
		commands.add("-o");
		commands.add(o.raw_output.getAbsolutePath());
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(o.genome.getParentFile());
		try
		{
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
			{
				System.out.printf("%03d: %s%n", index, line);
			}
			while ((line = stderr.readLine()) != null)
			{
				System.err.printf("%03d: %s%n", index, line);
			}
			p.waitFor();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		System.out.printf("%03d: %s%n", index, "done aligning.");
	}

	private void convertToSamFormat()
	{
		List<String> commands = new ArrayList<String>();
		commands.add(SOAP2SAM_COMMAND);
		commands.add(o.raw_output.getAbsolutePath());
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(o.genome.getParentFile());
		try
		{
			FastqWriter.writeFragments(fragments, o.reads.get(0).reads, 0);
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			FileWriter w = new FileWriter(o.sam_output);
			while ((line = stdout.readLine()) != null)
			{
				w.write(String.format("%s%n", line));
			}
			w.close();
			while ((line = stderr.readLine()) != null)
			{
				System.err.printf("%03d: %s%n", index, line);
			}
			p.waitFor();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Don't need to read fragments; we have those already. TODO: Move this
	 * logic into {@link SamReader}
	 */
	@Override
	public AlignmentResults readAlignment(int threshold)
	{
		System.out.printf("%03d: Reading alignment (threshold %d)...%n", index, threshold);
		AlignmentResults rs = new AlignmentResults();
		try
		{
			BufferedReader r = new BufferedReader(new FileReader(o.sam_output));
			String line = null;
			while ((line = r.readLine()) != null)
			{
				if (line.startsWith("@"))
				{
					continue;
				}
				String[] pieces = line.split("\\s+");
				if (pieces.length <= 3)
				{
					continue;
				}
				int readPosition = -1;
				String fragmentIdentifier = pieces[0];
				Matcher m = Constants.READ_POSITION_HEADER.matcher(fragmentIdentifier);
				if (m.matches())
				{
					readPosition = Integer.parseInt(m.group(2));
				}
				int alignedPosition = Integer.parseInt(pieces[3]) - 1;
				int phredProbability = Integer.parseInt(pieces[4]);
				if (readPosition == alignedPosition)
				{
					if (phredProbability >= threshold)
					{
						rs.truePositives++;
					}
					else
					{
						rs.falseNegatives++;
					}
				}
				else
				{
					if (phredProbability >= threshold)
					{
						rs.falsePositives++;
					}
					// System.out.println(line);
				}
				totalMappedFragments.add(fragmentIdentifier);
			}
			/*
			 * If a fragment didn't appear in the output at all, count it as a
			 * false negative
			 */
			if (fragments.size() >= totalMappedFragments.size())
			{
				rs.falseNegatives += (fragments.size() - totalMappedFragments.size());
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return rs;
	}

	@Override
	public void preAlignmentProcessing()
	{
		System.out.printf("%03d: %s%n", index, "Indexing genome...");
		createIndex();
		System.out.printf("%03d: %s%n", index, "done indexing.");
	}

	@Override
	public void postAlignmentProcessing()
	{
		System.out.printf("%03d: %s%n", index, "Converting output to SAM format...");
		convertToSamFormat();
		System.out.printf("%03d: %s%n", index, "done converting.");
	}
}
