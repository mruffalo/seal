package external;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import assembly.Fragment;
import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import generator.UniformErrorGenerator;

public abstract class AlignmentToolInterface
{
	protected int phredMatchThreshold = 0;
	protected CharSequence sequence;
	protected List<? extends Fragment> fragments;
	protected List<List<? extends Fragment>> pairedEndFragments;
	protected Options o;

	protected static final double[] ERROR_PROBABILITIES = { 0.0, 0.001, 0.002, 0.004, 0.01, 0.015,
			0.02, 0.03, 0.05, 0.1 };
	protected static final int[] PHRED_THRESHOLDS = { 0, 10, 20, 30, 40, 50 };

	/**
	 * Not all fields are used by every tool
	 * 
	 * @author mruffalo
	 */
	public static class Options
	{
		/**
		 * TODO: Examine how good an idea this was
		 * 
		 * @author mruffalo
		 */
		public static class Reads
		{
			public Reads(int index_)
			{
				index = index_;
			}
			public final int index;
			public File reads;
			/**
			 * Not used for every tool
			 */
			public File binary_reads;
			public File aligned_reads;
		}

		public boolean is_paired_end;
		public File genome;
		public File binary_genome;
		public List<Reads> reads = new ArrayList<Reads>(2);
		public File raw_output;
		public File sam_output;
		public File index;
		/**
		 * Produced here
		 */
		public File converted_output;
		public File unmapped_output;
	}

	public AlignmentToolInterface(CharSequence sequence_, List<? extends Fragment> list_, Options o_)
	{
		sequence = sequence_;
		fragments = list_;
		o = o_;
		if (o.is_paired_end)
		{
			pairedEndFragments = Fragment.pairedEndClone(fragments, 100);
		}
		else
		{
			pairedEndFragments = new ArrayList<List<? extends Fragment>>(1);
			pairedEndFragments.add(fragments);
		}
	}

	public static class AlignmentResults
	{
		/**
		 * We know how many fragments we generated; this is the size of
		 * {@link AlignmentToolInterface#fragments}. This is how many total
		 * fragments were present in the alignment tool's output.
		 */
		public int totalFragmentsRead;
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

	public abstract AlignmentResults readAlignment();

	public void cleanup()
	{
		o.genome.delete();
		o.binary_genome.delete();
		for (Options.Reads r : o.reads)
		{
			r.aligned_reads.delete();
			r.binary_reads.delete();
			r.reads.delete();
		}
		if (o.index != null)
		{
			o.index.delete();
		}
		o.raw_output.delete();
		o.sam_output.delete();
		if (o.unmapped_output != null)
		{
			o.unmapped_output.delete();
		}
	}

	public static void toolEvaluation(boolean paired_end)
	{
		SequenceGenerator g = new SeqGenSingleSequenceMultipleRepeats();
		SequenceGenerator.Options sgo = new SequenceGenerator.Options();
		sgo.length = 100000;
		sgo.repeatCount = 10;
		sgo.repeatLength = 200;
		sgo.repeatErrorProbability = 0.01;
		System.out.print("Generating sequence...");
		CharSequence sequence = g.generateSequence(sgo);
		System.out.println("done.");

		/*
		 * System.out.print("Reading genome..."); CharSequence sequence =
		 * FastaReader.getLargeSequence(genome); System.out.println("done.");
		 */
		Fragmentizer.Options fo = new Fragmentizer.Options();
		fo.k = 50;
		fo.n = 750;
		fo.ksd = 1;

		System.out.print("Reading fragments...");
		List<? extends Fragment> list = Fragmentizer.fragmentize(sequence, fo);
		System.out.println("done.");

		Options o = new Options();
		o.is_paired_end = paired_end;

		File path = new File("data");
		o.genome = new File(path, "genome.fasta");
		o.binary_genome = new File(path, "genome.bfa");

		int read_count = paired_end ? 2 : 1;
		for (int i = 1; i <= read_count; i++)
		{
			Options.Reads r = new Options.Reads(i);
			r.reads = new File(path, String.format("fragments%d.fastq", i));
			r.binary_reads = new File(path, String.format("fragments%d.bfq", i));
			r.aligned_reads = new File(path, String.format("alignment%d.sai", i));
			o.reads.add(r);
		}
		o.raw_output = new File(path, "out.raw");
		o.sam_output = new File(path, "alignment.sam");
		o.converted_output = new File(path, "out.txt");

		path.mkdirs();

		Map<Double, Map<Integer, Map<Class<? extends AlignmentToolInterface>, AlignmentResults>>> m = new TreeMap<Double, Map<Integer, Map<Class<? extends AlignmentToolInterface>, AlignmentResults>>>();

		for (double errorProbability : ERROR_PROBABILITIES)
		{
			Map<Integer, Map<Class<? extends AlignmentToolInterface>, AlignmentResults>> m_ep = new TreeMap<Integer, Map<Class<? extends AlignmentToolInterface>, AlignmentResults>>();
			m.put(errorProbability, m_ep);
			System.out.print("Introducing fragment read errors...");
			UniformErrorGenerator eg = new UniformErrorGenerator(SequenceGenerator.NUCLEOTIDES,
				errorProbability);
			List<? extends Fragment> errored_list = eg.generateErrors(list);
			System.out.println("done.");
			for (int phredThreshold : PHRED_THRESHOLDS)
			{
				Map<Class<? extends AlignmentToolInterface>, AlignmentResults> m_pt = new HashMap<Class<? extends AlignmentToolInterface>, AlignmentResults>();
				m_ep.put(phredThreshold, m_pt);

				List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

				/*
				 * alignmentInterfaceList.add(new MaqInterface(sequence, list,
				 * genome, binary_genome, reads, binary_reads, binary_output,
				 * sam_output));
				 */
				alignmentInterfaceList.add(new MrFastInterface(sequence, errored_list, o));
				alignmentInterfaceList.add(new MrsFastInterface(sequence, errored_list, o));
				/*
				 * alignmentInterfaceList.add(new SoapInterface(sequence,
				 * errored_list, o));
				 */
				alignmentInterfaceList.add(new BwaInterface(sequence, errored_list, o));

				for (AlignmentToolInterface ati : alignmentInterfaceList)
				{
					ati.phredMatchThreshold = phredThreshold;
					ati.preAlignmentProcessing();
					ati.align();
					ati.postAlignmentProcessing();
					AlignmentResults r = ati.readAlignment();
					ati.cleanup();

					m_pt.put(ati.getClass(), r);

					System.out.printf("%d matches / %d total fragments generated (%f)%n",
						r.truePositives, fo.n, (double) r.truePositives / (double) fo.n);
					System.out.printf("Precision: %f%n", (double) r.truePositives
							/ (double) (r.truePositives + r.falsePositives));
					System.out.printf("Recall: %f%n", (double) r.truePositives
							/ (double) (r.truePositives + r.falseNegatives));
				}
			}
		}

		for (Double d : m.keySet())
		{
			for (Integer i : m.get(d).keySet())
			{
				for (Class<? extends AlignmentToolInterface> c : m.get(d).get(i).keySet())
				{
					AlignmentResults r = m.get(d).get(i).get(c);
					System.out.printf(
						"Tool %s,\t\terror probability %f,\tthreshold %d,\tprecision %f,\trecall %f%n",
						c.getSimpleName(), d, i, (double) r.truePositives
								/ (double) (r.truePositives + r.falsePositives),
						(double) r.truePositives / (double) (r.truePositives + r.falseNegatives));
				}
			}
		}
	}

	public static void main(String[] args)
	{
		toolEvaluation(false);
	}
}
