package external;

import io.FastaWriter;
import io.SamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import assembly.Fragment;
import external.AlignmentToolInterface.AlignmentResults;
import external.AlignmentToolInterface.Options;

public class BowtieInterface extends AlignmentToolInterface
{
	private static final String BOWTIE_COMMAND = "bowtie";
	private static final String BOWTIE_INDEX_COMMAND = "bowtie-build";

	public BowtieInterface(int index_, String description_, List<Integer> thresholds_,
		CharSequence sequence_, List<? extends Fragment> list_, Options o_,
		Map<String, Map<Integer, AlignmentResults>> m_)
	{
		super(index_, description_, thresholds_, sequence_, list_, o_, m_);
	}

	private void createIndex()
	{
		String index_base_name = "index";
		String index_addition = ".1.ebwt";
		o.index = new File(o.genome.getParentFile(), index_base_name + index_addition);
		if (o.index.isFile())
		{
			System.out.printf("%03d: %s%n", index, "Index found; skipping");
		}
		else
		{
			List<String> commands = new ArrayList<String>();
			commands.add(BOWTIE_INDEX_COMMAND);
			commands.add(o.genome.getAbsolutePath());
			commands.add(index_base_name);
			ProcessBuilder pb = new ProcessBuilder(commands);
			pb.directory(o.index.getParentFile());
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
	public void preAlignmentProcessing()
	{
		createIndex();
	}

	@Override
	public void align()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void postAlignmentProcessing()
	{
		// TODO Auto-generated method stub
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
}
