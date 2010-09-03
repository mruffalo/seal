package external;

import generator.Fragmentizer;
import generator.SequenceGenerator;
import generator.UniformErrorGenerator;
import io.FastaReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import assembly.Fragment;
import external.AlignmentToolInterface.AlignmentOperation;
import external.AlignmentToolInterface.AlignmentResults;
import external.AlignmentToolInterface.Options;

public class AlignmentToolService
{
	/**
	 * The number of CPUs in your system is a good value for this
	 */
	private static final int NUMBER_OF_CONCURRENT_THREADS = 4;
	protected static final double[] ERROR_PROBABILITIES = { 0.0, 0.001, 0.002, 0.004, 0.01, 0.015,
			0.02, 0.03, 0.05, 0.1 };
	protected static final int[] PHRED_THRESHOLDS = { 0, 10, 20, 30 };

	private ExecutorService pool;

	public AlignmentToolService()
	{
		pool = Executors.newFixedThreadPool(NUMBER_OF_CONCURRENT_THREADS);
	}

	public void toolEvaluation(boolean paired_end)
	{
		/*
		 * SequenceGenerator g = new SeqGenSingleSequenceMultipleRepeats();
		 * SequenceGenerator.Options sgo = new SequenceGenerator.Options();
		 * sgo.length = 100000; sgo.repeatCount = 10; sgo.repeatLength = 200;
		 * sgo.repeatErrorProbability = 0.01;
		 * System.out.print("Generating sequence..."); CharSequence sequence =
		 * g.generateSequence(sgo); System.out.println("done.");
		 */
		final File path = new File("data");
		final File chr22 = new File(path, "chr22.fa");
		System.out.print("Reading genome...");
		CharSequence sequence = null;
		try
		{
			/*
			 * Don't worry about casting file size to an int: we can't have
			 * strings longer than Integer.MAX_VALUE anyway
			 */
			sequence = FastaReader.getSequence(chr22, (int) chr22.length());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("done.");
		System.out.printf("Genome length: %d%n", sequence.length());
		Fragmentizer.Options fo = new Fragmentizer.Options();
		fo.k = 50;
		fo.n = 50000;
		fo.ksd = 1;

		System.out.print("Reading fragments...");
		List<? extends Fragment> list = Fragmentizer.fragmentize(sequence, fo);
		System.out.println("done.");

		path.mkdirs();

		List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();
		Map<Double, Map<Integer, Map<Class<? extends AlignmentToolInterface>, AlignmentResults>>> m = Collections.synchronizedMap(new TreeMap<Double, Map<Integer, Map<Class<? extends AlignmentToolInterface>, AlignmentResults>>>());

		int index = 0;
		for (double errorProbability : ERROR_PROBABILITIES)
		{
			Map<Integer, Map<Class<? extends AlignmentToolInterface>, AlignmentResults>> m_ep = Collections.synchronizedMap(new TreeMap<Integer, Map<Class<? extends AlignmentToolInterface>, AlignmentResults>>());
			m.put(errorProbability, m_ep);
			System.out.print("Introducing fragment read errors...");
			UniformErrorGenerator eg = new UniformErrorGenerator(SequenceGenerator.NUCLEOTIDES,
				errorProbability);
			List<? extends Fragment> errored_list = eg.generateErrors(list);
			System.out.println("done.");
			for (int phredThreshold : PHRED_THRESHOLDS)
			{
				Map<Class<? extends AlignmentToolInterface>, AlignmentResults> m_pt = Collections.synchronizedMap(new HashMap<Class<? extends AlignmentToolInterface>, AlignmentResults>());
				m_ep.put(phredThreshold, m_pt);

				/*
				 * alignmentInterfaceList.add(new MaqInterface(sequence, list,
				 * genome, binary_genome, reads, binary_reads, binary_output,
				 * sam_output));
				 */

				alignmentInterfaceList.add(new MrFastInterface(++index, sequence, errored_list,
					new Options(paired_end, phredThreshold, errorProbability), m_pt));
				alignmentInterfaceList.add(new MrsFastInterface(++index, sequence, errored_list,
					new Options(paired_end, phredThreshold, errorProbability), m_pt));
				alignmentInterfaceList.add(new SoapInterface(++index, sequence, errored_list,
					new Options(paired_end, phredThreshold, errorProbability), m_pt));
				alignmentInterfaceList.add(new BwaInterface(++index, sequence, errored_list,
					new Options(paired_end, phredThreshold, errorProbability), m_pt));

			}
		}
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			alignmentInterfaceList.size());
		for (AlignmentToolInterface ati : alignmentInterfaceList)
		{
			File tool_path = new File(path, String.format("%03d-%s", ati.index,
				ati.getClass().getSimpleName()));
			tool_path.mkdirs();
			ati.o.genome = new File(tool_path, "genome.fasta");
			ati.o.binary_genome = new File(tool_path, "genome.bfa");

			int read_count = paired_end ? 2 : 1;
			for (int i = 1; i <= read_count; i++)
			{
				Options.Reads r = new Options.Reads(i);
				r.reads = new File(tool_path, String.format("fragments%d.fastq", i));
				r.binary_reads = new File(tool_path, String.format("fragments%d.bfq", i));
				r.aligned_reads = new File(tool_path, String.format("alignment%d.sai", i));
				ati.o.reads.add(r);
			}
			ati.o.raw_output = new File(tool_path, "out.raw");
			ati.o.sam_output = new File(tool_path, "alignment.sam");
			ati.o.converted_output = new File(tool_path, "out.txt");

			System.out.printf("*** %s: %d, %f%n", ati.getClass().getSimpleName(),
				ati.o.phred_match_threshold, ati.o.error_probability);

			futureList.add(pool.submit(ati));
		}
		pool.shutdown();
		for (Future<AlignmentResults> f : futureList)
		{
			try
			{
				f.get();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for (Double d : m.keySet())
		{
			for (Integer i : m.get(d).keySet())
			{
				for (Class<? extends AlignmentToolInterface> c : m.get(d).get(i).keySet())
				{
					AlignmentResults r = m.get(d).get(i).get(c);
					System.out.printf("%s,%f,%d,%f,%f,%d%n", c.getSimpleName(), d, i,
						(double) r.truePositives / (double) (r.truePositives + r.falsePositives),
						(double) r.truePositives / (double) (r.truePositives + r.falseNegatives),
						r.timeMap.get(AlignmentOperation.TOTAL));
				}
			}
		}
	}

	public static void main(String[] args)
	{
		new AlignmentToolService().toolEvaluation(false);
	}
}
