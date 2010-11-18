package external.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import assembly.Fragment;
import external.AlignmentResults;
import external.AlignmentToolInterface;

/**
 * TODO: Move some of this code into the general {@link AlignmentToolInterface}
 * class
 * 
 * @author mruffalo
 */
public class BwaInterface extends AlignmentToolInterface
{
	private static final int BYTE_BUFFER_SIZE = 65536;

	public static final String BWA_COMMAND = "bwa";
	public static final String INDEX_COMMAND = "index";
	public static final String ALIGN_COMMAND = "aln";
	public static final String SAM_SINGLE_END_COMMAND = "samse";
	public static final String SAM_PAIRED_END_COMMAND = "sampe";

	public static final String OUTPUT_TEMPLATE = "%s\t%d\t%d\t%d\t%d%n";

	public BwaInterface(int index_, String description_, List<Integer> thresholds_,
		CharSequence sequence_, List<? extends Fragment> list_, Options o_,
		Map<String, AlignmentResults> m_)
	{
		super(index_, description_, thresholds_, sequence_, list_, o_, m_);
	}

	public void createIndex()
	{
		String index_filename = o.genome.getName() + ".bwt";
		o.index = new File(o.genome.getParentFile(), index_filename);
		if (o.index.isFile())
		{
			System.out.printf("%03d: %s%n", index, "Index found; skipping");
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
		for (int i = 0; i < o.reads.size(); i++)
		{
			List<String> commands = new ArrayList<String>();
			commands.add(BWA_COMMAND);
			commands.add(ALIGN_COMMAND);
			commands.add(o.genome.getAbsolutePath());
			commands.add(o.reads.get(i).reads.getAbsolutePath());
			ProcessBuilder pb = new ProcessBuilder(commands);
			pb.directory(o.genome.getParentFile());
			try
			{
				Process p = pb.start();
				ReadableByteChannel stdout = Channels.newChannel(p.getInputStream());
				// InputStream stdout = p.getInputStream();
				BufferedReader stderr = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));

				ByteBuffer buffer = ByteBuffer.allocate(BYTE_BUFFER_SIZE);
				buffer.rewind();

				FileOutputStream w = new FileOutputStream(o.reads.get(i).aligned_reads);
				FileChannel wc = w.getChannel();
				while (stdout.read(buffer) > 0)
				{
					buffer.flip();
					wc.write(buffer);
					buffer.rewind();
				}
				wc.close();
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
		}
		System.out.printf("%03d: %s%n", index, "done aligning.");
	}

	public void convertToSamFormat()
	{
		List<String> commands = new ArrayList<String>();
		commands.add(BWA_COMMAND);
		commands.add(o.is_paired_end ? SAM_PAIRED_END_COMMAND : SAM_SINGLE_END_COMMAND);
		commands.add(o.genome.getAbsolutePath());
		for (Options.Reads r : o.reads)
		{
			commands.add(r.aligned_reads.getAbsolutePath());
		}
		for (Options.Reads r : o.reads)
		{
			commands.add(r.reads.getAbsolutePath());
		}
		commands.add(o.sam_output.getAbsolutePath());

		ProcessBuilder pb = new ProcessBuilder(commands);
		for (String arg : pb.command())
		{
			System.err.printf("%03d: %s%n", index, arg);
		}
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
		System.out.printf("%03d: %s%n", index, "Converting output to SAM format...");
		convertToSamFormat();
		System.out.printf("%03d: %s%n", index, "done converting.");
	}
}
