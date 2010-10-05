package external;

import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import generator.UniformErrorGenerator;
import io.FastaReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
	 * The number of CPUs in your system (maybe - 1) is a good value for this
	 */
	private static final int NUMBER_OF_CONCURRENT_THREADS = 2;
	protected static final double[] ERROR_PROBABILITIES = { 0.0, 0.001, 0.004, 0.01, 0.025, 0.05,
			0.1 };
	protected static final List<Integer> PHRED_THRESHOLDS = Collections.unmodifiableList(Arrays.asList(
		0, 1, 2, 3, 4, 5, 7, 10, 14, 20, 25, 30, 35, 40));
	protected static final List<Integer> RUNTIME_THRESHOLDS = Collections.unmodifiableList(Arrays.asList(0));
	protected static final int[] COVERAGES = { 3, 7, 10, 13, 16, 20 };

	private ExecutorService pool;

	public AlignmentToolService()
	{
		pool = Executors.newFixedThreadPool(NUMBER_OF_CONCURRENT_THREADS);
	}

	public void errorRateEvaluation(boolean paired_end)
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

		final int alignmentToolCount = ERROR_PROBABILITIES.length * PHRED_THRESHOLDS.size() * 7;
		List<AlignmentToolInterface> atiList = new ArrayList<AlignmentToolInterface>(
			alignmentToolCount);

		Map<Double, Map<String, Map<Integer, AlignmentResults>>> m = Collections.synchronizedMap(new TreeMap<Double, Map<String, Map<Integer, AlignmentResults>>>());
		List<Future<Map<Integer, AlignmentResults>>> futureList = new ArrayList<Future<Map<Integer, AlignmentResults>>>(
			alignmentToolCount);

		int index = 0;
		for (double errorProbability : ERROR_PROBABILITIES)
		{
			Map<String, Map<Integer, AlignmentResults>> m_ep = Collections.synchronizedMap(new TreeMap<String, Map<Integer, AlignmentResults>>());
			m.put(errorProbability, m_ep);
			System.out.print("Introducing fragment read errors...");
			UniformErrorGenerator eg = new UniformErrorGenerator(SequenceGenerator.NUCLEOTIDES,
				errorProbability);
			List<? extends Fragment> errored_list = eg.generateErrors(list);
			System.out.println("done.");
			List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

			Options o = new Options(paired_end, errorProbability);
			o.penalize_duplicate_mappings = false;
			alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-R", PHRED_THRESHOLDS,
				sequence, errored_list, o, m_ep));
			alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-S", PHRED_THRESHOLDS,
				sequence, errored_list, new Options(paired_end, errorProbability), m_ep));
			o = new Options(paired_end, errorProbability);
			o.penalize_duplicate_mappings = false;
			alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-R", PHRED_THRESHOLDS,
				sequence, errored_list, o, m_ep));
			alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-S", PHRED_THRESHOLDS,
				sequence, errored_list, new Options(paired_end, errorProbability), m_ep));
			alignmentInterfaceList.add(new SoapInterface(++index, "SOAP", PHRED_THRESHOLDS,
				sequence, errored_list, new Options(paired_end, errorProbability), m_ep));
			alignmentInterfaceList.add(new BwaInterface(++index, "BWA", PHRED_THRESHOLDS, sequence,
				errored_list, new Options(paired_end, errorProbability), m_ep));
			o = new Options(paired_end, errorProbability);
			o.penalize_duplicate_mappings = false;
			alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP-R", PHRED_THRESHOLDS,
				sequence, errored_list, o, m_ep));
			alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP-S", PHRED_THRESHOLDS,
				sequence, errored_list, new Options(paired_end, errorProbability), m_ep));

			for (AlignmentToolInterface ati : alignmentInterfaceList)
			{
				File tool_path = new File(path,
					String.format("%03d-%s", ati.index, ati.description));
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

				System.out.printf("*** %03d %s: %f%n", ati.index, ati.description,
					ati.o.error_probability);

				atiList.add(ati);
			}
		}
		for (AlignmentToolInterface ati : atiList)
		{
			futureList.add(pool.submit(ati));
		}

		pool.shutdown();
		for (Future<Map<Integer, AlignmentResults>> f : futureList)
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
		System.out.printf("%s,%s,%s,%s,%s,%s%n", "Tool", "ErrorRate", "Threshold", "Precision",
			"Recall", "Time");
		for (Double d : m.keySet())
		{
			for (String s : m.get(d).keySet())
			{
				for (Integer i : m.get(d).get(s).keySet())
				{
					AlignmentResults r = m.get(d).get(s).get(i);
					System.out.printf("%s,%f,%d,%f,%f,%d%n", s, d, i, (double) r.truePositives
							/ (double) (r.truePositives + r.falsePositives),
						(double) r.truePositives / (double) (r.truePositives + r.falseNegatives),
						r.timeMap.get(AlignmentOperation.TOTAL));
				}
			}
		}
	}

	/**
	 * TODO: Don't duplicate code
	 */
	public void runtimeEvaluation()
	{
		final boolean paired_end = false;
		final double errorProbability = 0.05;
		final int phredThreshold = 0;
		final File path = new File("data");

		SequenceGenerator g = new SeqGenSingleSequenceMultipleRepeats();
		SequenceGenerator.Options sgo = new SequenceGenerator.Options();
		sgo.length = 500000;
		System.out.print("Generating sequence...");
		CharSequence sequence = g.generateSequence(sgo);
		System.out.println("done.");
		System.out.printf("Genome length: %d%n", sequence.length());

		path.mkdirs();

		Map<String, Map<Integer, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<String, Map<Integer, AlignmentResults>>());
		List<Future<Map<Integer, AlignmentResults>>> futureList = new ArrayList<Future<Map<Integer, AlignmentResults>>>(
			COVERAGES.length * 5);

		int index = 0;
		for (int coverage : COVERAGES)
		{
			Fragmentizer.Options fo = new Fragmentizer.Options();
			fo.k = 50;
			/*
			 * Integer truncation is okay here
			 */
			fo.n = (coverage * sequence.length()) / fo.k;
			fo.ksd = 1;

			System.out.printf("Reading %d fragments...", fo.n);
			List<? extends Fragment> list = Fragmentizer.fragmentize(sequence, fo);
			System.out.println("done.");

			System.out.print("Introducing fragment read errors...");
			UniformErrorGenerator eg = new UniformErrorGenerator(SequenceGenerator.NUCLEOTIDES,
				errorProbability);
			List<? extends Fragment> errored_list = eg.generateErrors(list);
			System.out.println("done.");

			List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

			alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast", RUNTIME_THRESHOLDS,
				sequence, errored_list, new Options(paired_end, errorProbability), m));
			alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast", RUNTIME_THRESHOLDS,
				sequence, errored_list, new Options(paired_end, errorProbability), m));
			alignmentInterfaceList.add(new SoapInterface(++index, "SOAP", RUNTIME_THRESHOLDS,
				sequence, errored_list, new Options(paired_end, errorProbability), m));
			alignmentInterfaceList.add(new BwaInterface(++index, "BWA", RUNTIME_THRESHOLDS,
				sequence, errored_list, new Options(paired_end, errorProbability), m));
			alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP", RUNTIME_THRESHOLDS,
				sequence, errored_list, new Options(paired_end, errorProbability), m));

			for (AlignmentToolInterface ati : alignmentInterfaceList)
			{
				File tool_path = new File(path,
					String.format("%03d-%s", ati.index, ati.description));
				tool_path.mkdirs();
				ati.o.genome = new File(tool_path, "genome.fasta");
				ati.o.binary_genome = new File(tool_path, "genome.bfa");

				Options.Reads r = new Options.Reads(1);
				r.reads = new File(tool_path, String.format("fragments%d.fastq", 1));
				r.binary_reads = new File(tool_path, String.format("fragments%d.bfq", 1));
				r.aligned_reads = new File(tool_path, String.format("alignment%d.sai", 1));
				ati.o.reads.add(r);

				ati.o.raw_output = new File(tool_path, "out.raw");
				ati.o.sam_output = new File(tool_path, "alignment.sam");
				ati.o.converted_output = new File(tool_path, "out.txt");

				System.out.printf("*** %03d %s: %d%n", ati.index, ati.description, coverage);

				futureList.add(pool.submit(ati));
			}
		}

		pool.shutdown();
		for (Future<Map<Integer, AlignmentResults>> f : futureList)
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
		System.out.printf("%s,%s,%s,%s,%s,%s,%s%n", "Tool", "ErrorRate", "Threshold", "Precision",
			"Recall", "PreprocessingTime", "AlignmentTime", "PostprocessingTime", "TotalTime");
		for (String s : m.keySet())
		{
			for (Integer i : m.get(s).keySet())
			{
				AlignmentResults r = m.get(s).get(i);
				System.out.printf("%s,%d,%f,%f,%d,%d,%d%n", s, i, (double) r.truePositives
						/ (double) (r.truePositives + r.falsePositives), (double) r.truePositives
						/ (double) (r.truePositives + r.falseNegatives),
					r.timeMap.get(AlignmentOperation.PREPROCESSING),
					r.timeMap.get(AlignmentOperation.ALIGNMENT),
					r.timeMap.get(AlignmentOperation.POSTPROCESSING),
					r.timeMap.get(AlignmentOperation.TOTAL));
			}
		}
	}

	public static void main(String[] args)
	{
		new AlignmentToolService().errorRateEvaluation(false);
	}
}
