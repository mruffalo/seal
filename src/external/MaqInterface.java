package external;

import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import generator.UniformErrorGenerator;
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
import java.util.List;
import java.util.regex.Matcher;
import assembly.Fragment;

public class MaqInterface extends AlignmentToolInterface
{
	public static final String MAQ_COMMAND = "maq";
	public static final String FASTQ_TO_BFQ_COMMAND = "fastq2bfq";
	public static final String ALIGN_COMMAND = "map";
	public static final String SAM_SINGLE_END_COMMAND = "samse";
	public static final String SAM_PAIRED_END_COMMAND = "sampe";

	public static final int PHRED_MATCH_THRESHOLD = 0;

	private CharSequence sequence;
	private List<? extends Fragment> fragments;

	private File genome;
	private File reads;
	private File binary_reads;
	private File binary_output;
	private File sam_output;

	public MaqInterface(CharSequence string_, List<? extends Fragment> fragments_, File genome_,
		File reads_, File binary_reads_, File binaryOutput_, File sam_output_)
	{
		super();
		sequence = string_;
		fragments = fragments_;
		genome = genome_;
		reads = reads_;
		binary_reads = binary_reads_;
		binary_output = binaryOutput_;
		sam_output = sam_output_;
	}

	@Override
	public void align()
	{
		System.out.print("Aligning reads...");
		ProcessBuilder pb = new ProcessBuilder(MAQ_COMMAND, ALIGN_COMMAND, "-f",
			binary_output.getAbsolutePath(), genome.getAbsolutePath(), reads.getAbsolutePath());
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

	public void convertFastqToBfq(File reads, File binary_reads)
	{

	}

	public void convertToSamFormat(File genome, File binary_output, File reads, File sam_output)
	{
		ProcessBuilder pb = new ProcessBuilder(MAQ_COMMAND, SAM_SINGLE_END_COMMAND,
			genome.getAbsolutePath(), binary_output.getAbsolutePath(), reads.getAbsolutePath(),
			sam_output.getAbsolutePath());
		for (String arg : pb.command())
		{
			System.err.println(arg);
		}
		pb.directory(genome.getParentFile());
		try
		{
			FastqWriter.writeFragments(fragments, reads);
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			FileWriter w = new FileWriter(sam_output);
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
	public void readAlignment()
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
		System.out.printf("%d matches%n", matches);
		System.out.printf("%d total fragments read%n", total);
	}

	@Override
	public void preAlignmentProcessing()
	{
		System.out.print("Converting FASTQ output to BFQ...");
		convertFastqToBfq(reads, binary_reads);
		System.out.println("done.");
	}

	@Override
	public void postAlignmentProcessing()
	{
		System.out.print("Converting output to SAM format...");
		convertToSamFormat(genome, binary_output, reads, sam_output);
		System.out.println("done.");
	}

	public static void main(String args[])
	{
		SequenceGenerator g = new SeqGenSingleSequenceMultipleRepeats();
		System.out.print("Generating sequence...");
		CharSequence sequence = g.generateSequence(10000, 10, 20);
		System.out.println("done.");
		File path = new File("data");
		File genome = new File(path, "genome.fasta");
		File reads = new File(path, "fragments.fastq");
		File binaryReads = new File(path, "fragments.bfq");
		File binaryOutput = new File(path, "alignment.sai");
		File samOutput = new File(path, "alignment.sam");
		path.mkdirs();
		/*
		 * System.out.print("Reading genome..."); CharSequence sequence =
		 * FastaReader.getLargeSequence(genome); System.out.println("done.");
		 */
		Fragmentizer.Options o = new Fragmentizer.Options();
		o.k = 60;
		o.n = 750;
		o.ksd = 1;
		System.out.print("Reading fragments...");
		List<? extends Fragment> list = Fragmentizer.fragmentize(sequence, o);
		System.out.println("done.");
		System.out.print("Introducing fragment read errors...");
		UniformErrorGenerator eg = new UniformErrorGenerator();
		eg.setErrorProbability(0.05);
		list = eg.generateErrors(list, SequenceGenerator.NUCLEOTIDES);
		System.out.println("done.");
		MaqInterface m = new MaqInterface(sequence, list, genome, reads, binaryReads, binaryOutput,
			samOutput);
		m.preAlignmentProcessing();
		m.align();
		m.postAlignmentProcessing();
		m.readAlignment();
	}
}
