package external;

import io.Constants;
import io.FastaWriter;
import io.FastqWriter;
import io.SamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

	public SoapInterface(CharSequence sequence_, List<? extends Fragment> fragments_, Options o_)
	{
		super(sequence_, fragments_, o_);
	}

	public void createIndex(File file)
	{
		ProcessBuilder pb = new ProcessBuilder(INDEX_COMMAND, file.getAbsolutePath());
		pb.directory(file.getParentFile());
		try
		{
			FastaWriter.writeSequence(sequence, file);
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
			{
				System.out.println(line);
			}
			while ((line = stderr.readLine()) != null)
			{
				System.err.println(line);
			}
			p.waitFor();
			String index_filename = file.getName() + ".index";
			o.index = new File(file.getParentFile(), index_filename);
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

	@Override
	public void align()
	{
		System.out.print("Aligning reads...");
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
			FastqWriter.writeFragments(fragments, o.reads.get(0).reads, 0);
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
			{
				System.out.println(line);
			}
			while ((line = stderr.readLine()) != null)
			{
				System.err.println(line);
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
		System.out.println("done.");
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
			while ((line = stdout.readLine()) != null)
			{
				System.out.println(line);
			}
			while ((line = stderr.readLine()) != null)
			{
				System.err.println(line);
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
		System.out.println("done.");
	}

	/**
	 * Don't need to read fragments; we have those already. TODO: Move this
	 * logic into {@link SamReader}
	 */
	@Override
	public AlignmentResults readAlignment()
	{
		System.out.print("Reading alignment...");
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
				Matcher m = Constants.READ_POSITION_HEADER.matcher(pieces[0]);
				if (m.matches())
				{
					readPosition = Integer.parseInt(m.group(2));
				}
				int alignedPosition = Integer.parseInt(pieces[3]) - 1;
				int phredProbability = Integer.parseInt(pieces[4]);
				if (readPosition == alignedPosition)
				{
					if (phredProbability >= phredMatchThreshold)
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
					if (phredProbability >= phredMatchThreshold)
					{
						rs.falsePositives++;
					}
					System.out.println(line);
				}
				rs.totalFragmentsRead++;
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
		System.out.print("Indexing genome...");
		createIndex(o.genome);
		System.out.println("done.");
	}

	@Override
	public void postAlignmentProcessing()
	{
		System.out.print("Converting output to SAM format...");
		convertToSamFormat();
		System.out.println("done.");
	}
}
