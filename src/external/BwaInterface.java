package external;

import io.Constants;
import io.FastaWriter;
import io.FastqWriter;
import io.SamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import external.AlignmentToolInterface.ResultsStruct;
import assembly.Fragment;

/**
 * TODO: Move some of this code into the general {@link AlignmentToolInterface}
 * class
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

	public BwaInterface(CharSequence sequence_, List<? extends Fragment> fragments_, Options o_)
	{
		super(sequence_, fragments_, o_);
	}

	public void createIndex(File file)
	{
		ProcessBuilder pb = new ProcessBuilder(BWA_COMMAND, INDEX_COMMAND, file.getAbsolutePath());
		pb.directory(file.getParentFile());
		try
		{
			FastaWriter.writeSequence(sequence, file);
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
			{
				System.out.println(line);
			}
			while ((line = stderr.readLine()) != null)
			{
				System.err.println(line);
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
	public void align()
	{
		System.out.print("Aligning reads...");
		ProcessBuilder pb = new ProcessBuilder(BWA_COMMAND, ALIGN_COMMAND, "-f",
			o.binary_output.getAbsolutePath(), o.genome.getAbsolutePath(),
			o.reads.get(0).reads.getAbsolutePath());
		pb.directory(o.genome.getParentFile());
		try
		{
			for (Options.Reads r : o.reads)
			{

				FastqWriter.writeFragments(fragments, r.reads, r.index);
			}
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
			{
				System.out.println(line);
			}
			while ((line = stderr.readLine()) != null)
			{
				System.err.println(line);
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
		System.out.println("done.");
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
			System.err.println(arg);
		}
		pb.directory(o.genome.getParentFile());
		try
		{
			for (Options.Reads r : o.reads)
			{
				// XXX Fix this
				FastqWriter.writeFragments(fragments, r.reads, r.index);
			}
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			FileWriter w = new FileWriter(o.sam_output);
			while ((line = stdout.readLine()) != null)
			{
				w.write(String.format("%s%n", line));
			}
			while ((line = stderr.readLine()) != null)
			{
				System.err.println(line);
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

	/**
	 * Don't need to read fragments; we have those already. TODO: Move this
	 * logic into {@link SamReader}
	 */
	@Override
	public ResultsStruct readAlignment()
	{
		System.out.print("Reading alignment...");
		ResultsStruct rs = new ResultsStruct();
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
				if (pieces.length <= 4)
				{
					continue;
				}
				int readPosition = -1;
				Matcher m = Constants.READ_POSITION_HEADER.matcher(pieces[0]);
				if (m.matches())
				{
					readPosition = Integer.parseInt(m.group(2));
				}
				int alignedPosition = Integer.parseInt(pieces[3]) - 1;
				int phredProbability = Integer.parseInt(pieces[4]);
				if (readPosition == alignedPosition)
				{
					if (phredProbability >= phredMatchThreshold)
					{
						rs.truePositives++;
					}
					else
					{
						rs.falseNegatives++;
					}
				}
				else
				{
					if (phredProbability >= phredMatchThreshold)
					{
						rs.falsePositives++;
					}
					System.out.println(line);
				}
				rs.totalFragmentsRead++;
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

	@Override
	public void preAlignmentProcessing()
	{
		System.out.print("Indexing genome...");
		createIndex(o.genome);
		System.out.println("done.");
	}

	@Override
	public void postAlignmentProcessing()
	{
		System.out.print("Converting output to SAM format...");
		convertToSamFormat();
		System.out.println("done.");
	}
}
