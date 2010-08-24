package external;

import io.Constants;
import io.FastqWriter;
import io.SamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import external.AlignmentToolInterface.AlignmentResults;
import assembly.Fragment;

/**
 * TODO: Clean up local variables vs. method parameters
 * 
 * @author mruffalo
 */
public class MaqInterface extends AlignmentToolInterface
{
	public static final String MAQ_COMMAND = "maq";
	public static final String FASTQ_TO_BFQ_COMMAND = "fastq2bfq";
	public static final String FASTA_TO_BFA_COMMAND = "fastq2bfq";
	public static final String ALIGN_COMMAND = "map";
	public static final String ASSEMBLE_COMMAND = "assemble";

	public MaqInterface(CharSequence sequence_, List<? extends Fragment> fragments_, Options o_)
	{
		super(sequence_, fragments_, o_);
	}

	@Override
	public void align()
	{
		System.out.print("Aligning reads...");
		ProcessBuilder pb = new ProcessBuilder(MAQ_COMMAND, ALIGN_COMMAND,
			o.sam_output.getAbsolutePath(), o.raw_output.getAbsolutePath(),
			o.genome.getAbsolutePath(), o.reads.get(0).reads.getAbsolutePath());
		pb.directory(o.genome.getParentFile());
		try
		{
			FastqWriter.writeFragments(fragments, o.reads.get(0).reads, 0);
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

	/**
	 * TODO: Duplicate much less code here and in
	 * {@link #convertFastqToBfq(File, File)}
	 * 
	 * @param genome
	 * @param binary_genome
	 */
	public void convertFastaToBfa(File genome, File binary_genome)
	{
		ProcessBuilder pb = new ProcessBuilder(MAQ_COMMAND, FASTA_TO_BFA_COMMAND,
			genome.getAbsolutePath(), binary_genome.getAbsolutePath());
		for (String arg : pb.command())
		{
			System.err.println(arg);
		}
		pb.directory(genome.getParentFile());
		try
		{
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
			{
				System.err.println(line);
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
	 * TODO: Duplicate much less code here and in
	 * {@link #convertFastaToBfa(File, File)}
	 * 
	 * @param reads
	 * @param binary_reads
	 */
	public void convertFastqToBfq(File reads, File binary_reads, File genome, File binary_genome)
	{
		ProcessBuilder pb = new ProcessBuilder(MAQ_COMMAND, FASTQ_TO_BFQ_COMMAND,
			genome.getAbsolutePath(), binary_genome.getAbsolutePath());
		for (String arg : pb.command())
		{
			System.err.println(arg);
		}
		pb.directory(genome.getParentFile());
		try
		{
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
			{
				System.err.println(line);
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
	public AlignmentResults readAlignment()
	{
		System.out.print("Reading alignment...");
		int matches = 0;
		int total = 0;
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
				if (pieces.length <= 3)
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
				if (readPosition == alignedPosition && phredProbability >= phredMatchThreshold)
				{
					matches++;
				}
				else
				{
					System.out.println(line);
				}
				total++;
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
		AlignmentResults r = new AlignmentResults();
		r.truePositives = matches;
		// XXX: Assign other results fields
		return r;
	}

	@Override
	public void preAlignmentProcessing()
	{
		System.out.print("Converting FASTQ output to BFQ...");
		convertFastqToBfq(o.reads.get(0).reads, o.reads.get(0).binary_reads, o.genome,
			o.binary_genome);
		System.out.println("done.");
	}

	/**
	 * XXX: Do this
	 */
	@Override
	public void postAlignmentProcessing()
	{

	}
}
