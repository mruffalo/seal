package external;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import assembly.Fragment;
import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import generator.UniformErrorGenerator;

public abstract class AlignmentToolInterface
{
	public static final int PHRED_MATCH_THRESHOLD = 0;

	/**
	 * TODO: Examine how useful this might actually be, and remove if
	 * appropriate
	 * 
	 * @author mruffalo
	 */
	protected static class GenomeDescriptor
	{
		public File genome;
		public File reads;
		public File binaryOutput;
		public File samOutput;
	}

	public static class ResultsStruct
	{
		/**
		 * Mapped to correct location in target genome, and passes quality
		 * threshold
		 */
		public int truePositives;
		/**
		 * Mapped to incorrect location in target genome, and passes quality
		 * threshold
		 */
		public int falsePositives;
		/**
		 * <ul>
		 * <li>Mapped to correct location in target genome and does not pass
		 * quality threshold</li>
		 * <li>or not present at all in tool output</li>
		 * </ul>
		 */
		public int falseNegatives;
	}

	public abstract void preAlignmentProcessing();

	public abstract void align();

	public abstract void postAlignmentProcessing();

	public abstract ResultsStruct readAlignment();

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
		SequenceGenerator.Options sgo = new SequenceGenerator.Options();
		sgo.length = 10000;
		sgo.repeatCount = 10;
		sgo.repeatLength = 200;
		sgo.errorProbability = 0.01;
		System.out.print("Generating sequence...");
		CharSequence sequence = g.generateSequence(sgo);
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
		Fragmentizer.Options fo = new Fragmentizer.Options();
		fo.k = 50;
		fo.n = 750;
		fo.ksd = 3;

		System.out.print("Reading fragments...");
		List<? extends Fragment> list = Fragmentizer.fragmentize(sequence, fo);
		System.out.println("done.");
		System.out.print("Introducing fragment read errors...");
		UniformErrorGenerator eg = new UniformErrorGenerator(SequenceGenerator.NUCLEOTIDES, 0.02);
		list = eg.generateErrors(list);
		System.out.println("done.");

		List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();
		alignmentInterfaceList.add(new MrFastInterface(sequence, list, genome, reads,
			binary_output, sam_output));
		alignmentInterfaceList.add(new MrsFastInterface(sequence, list, genome, reads,
			binary_output, sam_output));
		alignmentInterfaceList.add(new MaqInterface(sequence, list, genome, binary_genome, reads,
			binary_reads, binary_output, sam_output));
		alignmentInterfaceList.add(new BwaInterface(sequence, list, genome, reads, binary_output,
			sam_output));

		for (AlignmentToolInterface ati : alignmentInterfaceList)
		{
			ati.preAlignmentProcessing();
			ati.align();
			ati.postAlignmentProcessing();
			ResultsStruct r = ati.readAlignment();
			System.out.printf("%d matches / %d total fragments generated (%f)%n", r.truePositives,
				fo.n, (double) r.truePositives / (double) fo.n);
		}
	}
}
