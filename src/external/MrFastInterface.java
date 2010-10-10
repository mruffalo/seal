package external;

import io.SamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
	 * TODO: Fix indirection
	 */
	@Override
	public AlignmentResults readAlignment(int threshold)
	{
		return SamReader.readAlignment(index, threshold, o, fragments.size(),
			correctlyMappedFragments);
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
		correctlyMappedFragments = SamReader.readMappedFragmentSet(o.sam_output, fragments.size());
	}
}
