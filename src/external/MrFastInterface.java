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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import external.AlignmentToolInterface.AlignmentResults;
import external.AlignmentToolInterface.Options;
import assembly.Fragment;

public class MrFastInterface extends AlignmentToolInterface
{
	public static final String OUTPUT_FILE_OPTION = "-o";
	public static final String SEQ_OPTION = "--seq";
	public static final String MRFAST_COMMAND = "mrfast";
	public static final String INDEX_COMMAND = "--index";
	public static final String SEARCH_COMMAND = "--search";

	public MrFastInterface(int index_, String description_, List<Integer> thresholds_,
		CharSequence sequence_, List<? extends Fragment> list_, Options o_,
		Map<String, Map<Integer, AlignmentResults>> m_)
	{
		super(index_, description_, thresholds_, sequence_, list_, o_, m_);
	}

	public void createIndex()
	{
		String index_filename = o.genome.getName() + ".index";
		o.index = new File(o.genome.getParentFile(), index_filename);
		if (o.index.isFile())
		{
			System.out.printf("%03d: %s%n", index, "Index found; skipping");
		}
		else
		{
			ProcessBuilder pb = new ProcessBuilder(MRFAST_COMMAND, INDEX_COMMAND,
				o.genome.getAbsolutePath());
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
		commands.add(MRFAST_COMMAND);
		commands.add(SEARCH_COMMAND);
		commands.add(o.genome.getAbsolutePath());
		commands.add(SEQ_OPTION);
		commands.add(o.reads.get(0).reads.getAbsolutePath());
		commands.add(OUTPUT_FILE_OPTION);
		commands.add(o.sam_output.getAbsolutePath());
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
				String fragmentIdentifier = pieces[0];
				int readPosition = -1;
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
				else if (o.penalize_duplicate_mappings
						|| (!o.penalize_duplicate_mappings && !correctlyMappedFragments.contains(fragmentIdentifier)))
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

	/**
	 * This requires a separate pass over the SAM output with a lot of the same
	 * logic as above. TODO: Don't duplicate code
	 */
	private void readMappedFragmentSet()
	{
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
				String fragmentIdentifier = pieces[0];
				int readPosition = -1;
				Matcher m = Constants.READ_POSITION_HEADER.matcher(pieces[0]);
				if (m.matches())
				{
					readPosition = Integer.parseInt(m.group(2));
				}
				int alignedPosition = Integer.parseInt(pieces[3]) - 1;
				if (readPosition == alignedPosition)
				{
					correctlyMappedFragments.add(fragmentIdentifier);
				}
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
	}

	@Override
	public void postAlignmentProcessing()
	{
		readMappedFragmentSet();
	}
}
