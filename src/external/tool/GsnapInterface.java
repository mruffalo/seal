package external.tool;

import external.AlignmentResults;
import external.AlignmentToolInterface;
import org.apache.log4j.NDC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GsnapInterface extends AlignmentToolInterface
{
	private static final String INDEX_COMMAND = "gmap_build";
	private static final String INDEX_ARGUMENT_GENOME_NAME = "-d";
	private static final String ALIGN_COMMAND = "gsnap";

	public GsnapInterface(int index_, String description_, List<Integer> thresholds_,
			Options o_, Map<String, AlignmentResults> m_)
	{
		super(index_, description_, thresholds_, o_, m_);
	}

	@Override
	public void preAlignmentProcessing()
	{
		List<String> commands = new ArrayList<String>();
		commands.add(INDEX_COMMAND);
		commands.add(INDEX_ARGUMENT_GENOME_NAME);
		commands.add("genome");
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
	public void align()
	{
	}

	@Override
	public void postAlignmentProcessing()
	{
	}
}
