package external;

import io.Constants;
import io.FastaWriter;
import io.FastqWriter;
import io.SamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import assembly.Fragment;
import external.AlignmentToolInterface.AlignmentResults;
import external.AlignmentToolInterface.Options;

public class ShrimpInterface extends AlignmentToolInterface
{
	private static final String SHRIMP_ALIGN_COMMAND = "gmapper-ls";

	public ShrimpInterface(int index, String description, List<Integer> thresholds,
		CharSequence sequence, List<? extends Fragment> list, Options o,
		Map<String, Map<Integer, AlignmentResults>> m)
	{
		super(index, description, thresholds, sequence, list, o, m);
	}

	@Override
	public void align()
	{
		System.out.printf("%03d: %s%n", index, "Aligning reads...");
		final int i = 0;
		List<String> commands = new ArrayList<String>();
		commands.add(SHRIMP_ALIGN_COMMAND);
		commands.add(o.reads.get(i).reads.getAbsolutePath());
		commands.add(o.genome.getAbsolutePath());
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(o.genome.getParentFile());
		try
		{
			FastaWriter.writeFragments(pairedEndFragments.get(i), o.reads.get(i).reads,
				o.reads.get(i).index);
			Process p = pb.start();
			InputStream stdout = p.getInputStream();
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));

			// TODO: Use a faster bulk copy method for this
			FileOutputStream w = new FileOutputStream(o.sam_output);
			int data = -1;
			while ((data = stdout.read()) != -1)
			{
				w.write(data);
			}
			w.close();
			String line = null;
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

	@Override
	public void preAlignmentProcessing()
	{
		writeGenome();
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
				if (line.startsWith("@") || line.startsWith("#"))
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
