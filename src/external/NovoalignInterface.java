package external;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import assembly.Fragment;

public class NovoalignInterface extends AlignmentToolInterface
{
	public static final String NOVOINDEX_COMMAND = "novoindex";
	public static final String NOVOALIGN_COMMAND = "novoalign";
	public static final String NOVOALIGN_INDEX_PATH_OPTION = "-d";
	public static final String NOVOALIGN_SEQUENCE_PATH_OPTION = "-f";

	public NovoalignInterface(int index, String description, List<Integer> thresholds,
		CharSequence sequence, List<? extends Fragment> list, Options o,
		Map<String, Map<Integer, AlignmentResults>> m)
	{
		super(index, description, thresholds, sequence, list, o, m);
	}

	@Override
	public void align()
	{
		System.out.printf("%03d: %s%n", index, "Aligning...");
		List<String> commands = new ArrayList<String>();
		commands.add(NOVOALIGN_COMMAND);
		commands.add(NOVOALIGN_INDEX_PATH_OPTION);
		commands.add(o.index.getAbsolutePath());
		commands.add(NOVOALIGN_SEQUENCE_PATH_OPTION);
		for (Options.Reads r : o.reads)
		{
			commands.add(r.reads.getAbsolutePath());
		}
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
		System.out.printf("%03d: %s%n", index, "Done aligning.");
	}

	@Override
	public void postAlignmentProcessing()
	{
		// TODO Auto-generated method stub
	}

	public void createIndex()
	{
		o.index = new File(o.genome.getParentFile(), o.genome.getName() + ".index");
		if (o.index.isFile())
		{
			System.out.printf("%03d: %s%n", index, "Index found; skipping");
		}
		else
		{
			System.out.printf("%03d: %s%n", index, "Building index...");
			List<String> commands = new ArrayList<String>();
			commands.add(NOVOINDEX_COMMAND);
			commands.add(o.index.getAbsolutePath());
			commands.add(o.genome.getAbsolutePath());
			ProcessBuilder pb = new ProcessBuilder(commands);
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
			System.out.printf("%03d: %s%n", index, "Done building index.");
		}
	}

	@Override
	public void preAlignmentProcessing()
	{
		createIndex();
	}

	@Override
	public AlignmentResults readAlignment(int qualityThreshold)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
