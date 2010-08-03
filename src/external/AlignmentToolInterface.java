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

	public abstract void readAlignment();

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
		o.k = 100;
		o.n = 750;
		o.ksd = 3;
		System.out.print("Reading fragments...");
		List<? extends Fragment> list = Fragmentizer.fragmentize(sequence, o);
		System.out.println("done.");
		System.out.print("Introducing fragment read errors...");
		UniformErrorGenerator eg = new UniformErrorGenerator();
		eg.setErrorProbability(0.05);
		list = eg.generateErrors(list, SequenceGenerator.NUCLEOTIDES);
		System.out.println("done.");

		MrsFastInterface mrsFast = new MrsFastInterface(sequence, list, genome, reads,
			binaryOutput, samOutput);
		mrsFast.preAlignmentProcessing();
		mrsFast.align();
		mrsFast.postAlignmentProcessing();
		mrsFast.readAlignment();

		MaqInterface m = new MaqInterface(sequence, list, genome, reads, binaryReads, binaryOutput,
			samOutput);
		m.preAlignmentProcessing();
		m.align();
		m.postAlignmentProcessing();
		m.readAlignment();

		BwaInterface b = new BwaInterface(sequence, list, genome, reads, binaryOutput, samOutput);
		b.preAlignmentProcessing();
		b.align();
		b.postAlignmentProcessing();
		b.readAlignment();
	}
}
