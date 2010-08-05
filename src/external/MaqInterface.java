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

	private CharSequence sequence;
	private List<? extends Fragment> fragments;

	private File genome;
	private File binary_genome;
	private File reads;
	private File binary_reads;
	private File binary_output;
	private File sam_output;

	public MaqInterface(CharSequence string_, List<? extends Fragment> fragments_, File genome_,
		File binary_genome_, File reads_, File binary_reads_, File binaryOutput_, File sam_output_)
	{
		super();
		sequence = string_;
		fragments = fragments_;
		genome = genome_;
		binary_genome = binary_genome_;
		reads = reads_;
		binary_reads = binary_reads_;
		binary_output = binaryOutput_;
		sam_output = sam_output_;
	}

	@Override
	public void align()
	{
		System.out.print("Aligning reads...");
		ProcessBuilder pb = new ProcessBuilder(MAQ_COMMAND, ALIGN_COMMAND,
			sam_output.getAbsolutePath(), binary_output.getAbsolutePath(),
			genome.getAbsolutePath(), reads.getAbsolutePath());
		pb.directory(genome.getParentFile());
		try
		{
			FastqWriter.writeFragments(fragments, reads);
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
	public int readAlignment()
	{
		System.out.print("Reading alignment...");
		int matches = 0;
		int total = 0;
		try
		{
			BufferedReader r = new BufferedReader(new FileReader(sam_output));
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
				if (readPosition == alignedPosition && phredProbability >= PHRED_MATCH_THRESHOLD)
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
		System.out.println("done.");
		System.out.printf("%d matches / %d total fragments read (%f)%n", matches, total,
			(double) matches / (double) total);
		return matches;
	}

	@Override
	public void preAlignmentProcessing()
	{
		System.out.print("Converting FASTQ output to BFQ...");
		convertFastqToBfq(reads, binary_reads, genome, binary_genome);
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
