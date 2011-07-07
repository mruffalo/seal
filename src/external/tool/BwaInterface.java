package external.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import external.AlignmentResults;
import external.AlignmentToolInterface;
import org.apache.log4j.NDC;

/**
 * TODO: Move some of this code into the general {@link AlignmentToolInterface} class
 *
 * @author mruffalo
 */
public class BwaInterface extends AlignmentToolInterface
{
	public static final String BWA_COMMAND = "bwa";
	public static final String INDEX_COMMAND = "index";
	public static final String ALIGN_COMMAND = "aln";
	public static final String SAM_SINGLE_END_COMMAND = "samse";
	public static final String SAM_PAIRED_END_COMMAND = "sampe";
	public static final String OUTPUT_FILE_OPTION = "-f";

	public static final String OUTPUT_TEMPLATE = "%s\t%d\t%d\t%d\t%d%n";

	public BwaInterface(int index_, String description_, List<Integer> thresholds_, Options o_,
		Map<String, AlignmentResults> m_)
	{
		super(index_, description_, thresholds_, o_, m_);
	}

	public void createIndex()
	{
		String index_filename = o.genome.getName() + ".bwt";
		o.index = new File(o.genome.getParentFile(), index_filename);
		if (o.index.isFile())
		{
			log.debug("Index found; skipping");
		}
		else
		{
			ProcessBuilder pb = new ProcessBuilder(BWA_COMMAND, INDEX_COMMAND,
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
		log.info("Aligning reads");
		for (int i = 0; i < o.reads.size(); i++)
		{
			List<String> commands = new ArrayList<String>();
			commands.add(BWA_COMMAND);
			commands.add(ALIGN_COMMAND);
			commands.add(OUTPUT_FILE_OPTION);
			commands.add(o.reads.get(i).aligned_reads.getAbsolutePath());
			commands.add(o.genome.getAbsolutePath());
			commands.add(o.reads.get(i).reads.getAbsolutePath());
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

	public void convertToSamFormat()
	{
		List<String> commands = new ArrayList<String>();
		commands.add(BWA_COMMAND);
		commands.add(o.is_paired_end ? SAM_PAIRED_END_COMMAND : SAM_SINGLE_END_COMMAND);
		commands.add(OUTPUT_FILE_OPTION);
		commands.add(o.sam_output.getAbsolutePath());
		commands.add(o.genome.getAbsolutePath());
		for (Options.Reads r : o.reads)
		{
			commands.add(r.aligned_reads.getAbsolutePath());
		}
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
		log.info("Converting output to SAM format");
		convertToSamFormat();
	}
}
