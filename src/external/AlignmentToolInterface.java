package external;

import java.io.File;
import java.util.List;
import assembly.Fragment;
import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import generator.UniformErrorGenerator;

public abstract class AlignmentToolInterface
{
	public static final int PHRED_MATCH_THRESHOLD = 0;

	protected static class GenomeDescriptor
	{
		public File genome;
		public File reads;
		public File binaryOutput;
		public File samOutput;
	}

	public abstract void preAlignmentProcessing();

	public abstract void align();

	public abstract void postAlignmentProcessing();

	public abstract int readAlignment();

	protected GenomeDescriptor processHumanGenome()
	{
		return processGenome();
	}

	protected GenomeDescriptor processGenome()
	{
		return null;
	}

	public static void main(String[] args)
	{
		SequenceGenerator g = new SeqGenSingleSequenceMultipleRepeats();
		System.out.print("Generating sequence...");
		CharSequence sequence = g.generateSequence(10000, 10, 200);
		int matches = 0;
		System.out.println("done.");
		File path = new File("data");
		File genome = new File(path, "genome.fasta");
		File binary_genome = new File(path, "genome.bfa");
		File reads = new File(path, "fragments.fastq");
		File binary_reads = new File(path, "fragments.bfq");
		File binary_output = new File(path, "alignment.sai");
		File sam_output = new File(path, "alignment.sam");
		path.mkdirs();
		/*
		 * System.out.print("Reading genome..."); CharSequence sequence =
		 * FastaReader.getLargeSequence(genome); System.out.println("done.");
		 */
		Fragmentizer.Options o = new Fragmentizer.Options();
		o.k = 50;
		o.n = 750;
		o.ksd = 3;

		System.out.print("Reading fragments...");
		List<? extends Fragment> list = Fragmentizer.fragmentize(sequence, o);
		System.out.println("done.");
		System.out.print("Introducing fragment read errors...");
		UniformErrorGenerator eg = new UniformErrorGenerator();
		eg.setErrorProbability(0.02);
		list = eg.generateErrors(list, SequenceGenerator.NUCLEOTIDES);
		System.out.println("done.");

		/*
		 * TODO: Verify that each fragment appears exactly once
		 */
		MrsFastInterface mrsFast = new MrsFastInterface(sequence, list, genome, reads,
			binary_output, sam_output);
		mrsFast.preAlignmentProcessing();
		mrsFast.align();
		mrsFast.postAlignmentProcessing();
		matches = mrsFast.readAlignment();
		System.out.printf("%d matches / %d total fragments generated (%f)%n", matches, o.n,
			(double) matches / (double) o.n);

		MaqInterface m = new MaqInterface(sequence, list, genome, binary_genome, reads,
			binary_reads, binary_output, sam_output);
		m.preAlignmentProcessing();
		m.align();
		m.postAlignmentProcessing();
		matches = m.readAlignment();
		System.out.printf("%d matches / %d total fragments generated (%f)%n", matches, o.n,
			(double) matches / (double) o.n);
		BwaInterface b = new BwaInterface(sequence, list, genome, reads, binary_output, sam_output);
		b.preAlignmentProcessing();
		b.align();
		b.postAlignmentProcessing();
		matches = b.readAlignment();
		System.out.printf("%d matches / %d total fragments generated (%f)%n", matches, o.n,
			(double) matches / (double) o.n);

	}
}
