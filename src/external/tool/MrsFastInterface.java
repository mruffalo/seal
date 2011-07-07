package external.tool;

import io.SamReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import external.AlignmentResults;
import external.AlignmentToolInterface;
import org.apache.log4j.NDC;

public class MrsFastInterface extends AlignmentToolInterface
{
	public static final String SEQ_OPTION = "--seq";
	public static final String MRSFAST_COMMAND = "mrsfast";
	public static final String INDEX_COMMAND = "--index";
	public static final String SEARCH_COMMAND = "--search";

	public MrsFastInterface(int index_, String description_, List<Integer> thresholds_, Options o_,
		Map<String, AlignmentResults> m_)
	{
		super(index_, description_, thresholds_, o_, m_);
	}

	public void createIndex()
	{
		String index_filename = o.genome.getName() + ".index";
		o.index = new File(o.genome.getParentFile(), index_filename);
		if (o.index.isFile())
		{
			log.debug("Index found; skipping");
		}
		else
		{
			ProcessBuilder pb = new ProcessBuilder(MRSFAST_COMMAND, INDEX_COMMAND,
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
				NDC.push("stdout");
				while ((line = stdout.readLine()) != null)
				{
					log.info(line);
				}
				NDC.pop();
				NDC.push("stderr");
				while ((line = stderr.readLine()) != null)
				{
					log.info(line);
				}
				NDC.pop();
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
		System.out.print("Aligning reads...");
		ProcessBuilder pb = new ProcessBuilder(MRSFAST_COMMAND, SEARCH_COMMAND,
			o.genome.getAbsolutePath(), SEQ_OPTION, o.reads.get(0).reads.getAbsolutePath(), "-o",
			o.sam_output.getAbsolutePath());
		pb.directory(o.genome.getParentFile());
		try
		{
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			NDC.push("stdout");
			while ((line = stdout.readLine()) != null)
			{
				log.info(line);
			}
			NDC.pop();
			NDC.push("stderr");
			while ((line = stderr.readLine()) != null)
			{
				log.info(line);
			}
			NDC.pop();
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
	public void preAlignmentProcessing()
	{
		log.info("Indexing genome");
		createIndex();
	}

	@Override
	public void postAlignmentProcessing()
	{
		correctlyMappedFragments = SamReader.readMappedFragmentSet(o.sam_output, fragmentCount);
	}
}
