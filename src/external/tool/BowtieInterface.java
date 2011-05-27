package external.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import external.AlignmentResults;
import external.AlignmentToolInterface;

public class BowtieInterface extends AlignmentToolInterface
{
	private static final String BOWTIE_COMMAND = "bowtie";
	private static final String BOWTIE_INDEX_COMMAND = "bowtie-build";
	private static final String FIRST_PAIR_OPTION = "-1";
	private static final String SECOND_PAIR_OPTION = "-2";
	private static final String SAM_OPTION = "--sam";

	public BowtieInterface(int index_, String description_, List<Integer> thresholds_,
		CharSequence sequence_, Options o_, Map<String, AlignmentResults> m_)
	{
		super(index_, description_, thresholds_, sequence_, o_, m_);
	}

	private void createIndex()
	{
		String index_base_name = "index";
		o.index = new File(o.genome.getParentFile(), index_base_name);
		String index_addition = ".1.ebwt";
		File index_file_to_check = new File(o.genome.getParentFile(), index_base_name
				+ index_addition);
		if (index_file_to_check.isFile())
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
		List<String> commands = new ArrayList<String>();
		commands.add(BOWTIE_COMMAND);
		commands.add(SAM_OPTION);
		commands.add(o.index.getName());
		if (o.is_paired_end)
		{
			commands.add(FIRST_PAIR_OPTION);
			commands.add(o.reads.get(0).reads.getAbsolutePath());
			commands.add(SECOND_PAIR_OPTION);
			commands.add(o.reads.get(1).reads.getAbsolutePath());
		}
		else
		{
			commands.add(o.reads.get(0).reads.getAbsolutePath());
		}
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(o.index.getParentFile());
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
	}

	@Override
	public void postAlignmentProcessing()
	{
	}
}
