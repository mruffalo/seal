package external;

import io.SamReader;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import assembly.Fragment;

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
		System.out.printf("%03d: %s%n", index, "done aligning.");
	}

	@Override
	public void preAlignmentProcessing()
	{
	}

	/**
	 * TODO: Fix indirection
	 */
	@Override
	public AlignmentResults readAlignment(int threshold)
	{
		return SamReader.readAlignment(index, threshold, o, fragments.size(),
			correctlyMappedFragments);
	}

	@Override
	public void postAlignmentProcessing()
	{
		correctlyMappedFragments = SamReader.readMappedFragmentSet(o.sam_output, fragments.size());
	}
}
