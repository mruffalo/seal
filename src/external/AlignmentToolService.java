package external;

import external.AlignmentToolInterface.AlignmentOperation;
import external.AlignmentToolInterface.Options;
import external.tool.BowtieInterface;
import external.tool.BwaInterface;
import external.tool.MrFastInterface;
import external.tool.MrsFastInterface;
import external.tool.NovoalignInterface;
import external.tool.ShrimpInterface;
import external.tool.SoapInterface;
import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import generator.errors.FragmentErrorGenerator;
import generator.errors.IndelGenerator;
import generator.errors.LinearIncreasingErrorGenerator;
import generator.errors.UniformErrorGenerator;
import io.FastaReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import assembly.Fragment;

/**
 * Very large TODO: Refactor to avoid this ridiculous code duplication
 * 
 * @author mruffalo
 */
public class AlignmentToolService
{
	/**
	 * The number of CPUs in your system (maybe - 1) is a good value for this
	 */
	private static final int NUMBER_OF_CONCURRENT_THREADS = 2;
	private static final int EVAL_RUN_COUNT = 5;
	protected static final List<Double> ERROR_PROBABILITIES = Collections.unmodifiableList(Arrays.asList(
		0.0, 0.001, 0.004, 0.01, 0.025, 0.05, 0.1));
	protected static final List<Integer> PHRED_THRESHOLDS = Collections.unmodifiableList(Arrays.asList(
		0, 1, 2, 3, 4, 5, 7, 10, 14, 20, 25, 30, 35, 40));
	protected static final List<Integer> RUNTIME_THRESHOLDS = Collections.unmodifiableList(Arrays.asList(0));
	protected static final List<Integer> RUNTIME_GENOME_SIZES = Collections.unmodifiableList(Arrays.asList(
		5000, 20000, 100000, 500000, 1000000));
	protected static final List<Integer> RUNTIME_COVERAGES = Collections.unmodifiableList(Arrays.asList(
		3, 7, 10, 13, 16, 20));
	protected static final List<Integer> INDEL_SIZES = Collections.unmodifiableList(Arrays.asList(
		2, 4, 7, 10, 16));
	protected static final List<Double> INDEL_FREQUENCIES = Collections.unmodifiableList(Arrays.asList(
		1e-5, 3e-5, 1e-4, 3e-4, 1e-3, 3e-3, 1e-2));

	private final ExecutorService pool;

	public AlignmentToolService()
	{
		pool = Executors.newFixedThreadPool(NUMBER_OF_CONCURRENT_THREADS);
	}

	private static enum Genome
	{
		HUMAN_CHR22,
		RANDOM_EASY,
		RANDOM_HARD,
	}

	public void errorRateEvaluation(boolean paired_end, Genome genome)
	{
		final String testDescription = "error_rate";
		final int generated_genome_length = 1000000;
		CharSequence sequence = null;
		SequenceGenerator g = null;
		SequenceGenerator.Options sgo = null;
		final File path = new File("data");

		System.out.print("Reading/creating genome...");
		switch (genome)
		{
			case HUMAN_CHR22:
				final File chr22 = new File(path, "chr22.fa");
				try
				{
					/*
					 * Don't worry about casting file size to an int: we can't
					 * have strings longer than Integer.MAX_VALUE anyway
					 */
					sequence = FastaReader.getSequence(chr22, (int) chr22.length());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				break;
			case RANDOM_EASY:
				g = new SeqGenSingleSequenceMultipleRepeats();
				sgo = new SequenceGenerator.Options();
				sgo.length = generated_genome_length;
				sgo.repeatCount = 0;
				System.out.print("Generating sequence...");
				sequence = g.generateSequence(sgo);
				System.out.println("done.");
				break;
			case RANDOM_HARD:
				g = new SeqGenSingleSequenceMultipleRepeats();
				sgo = new SequenceGenerator.Options();
				sgo.length = generated_genome_length;
				sgo.repeatCount = 100;
				sgo.repeatLength = 500;
				sgo.repeatErrorProbability = 0.03;
				System.out.print("Generating sequence...");
				sequence = g.generateSequence(sgo);
				System.out.println("done.");
				break;
			default:
				break;
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

		final int alignmentToolCount = ERROR_PROBABILITIES.size() * PHRED_THRESHOLDS.size() * 7;
		List<AlignmentToolInterface> atiList = new ArrayList<AlignmentToolInterface>(
			alignmentToolCount);

		Map<Double, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Double, Map<String, AlignmentResults>>());
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			alignmentToolCount);

		int index = 0;
		for (double errorProbability : ERROR_PROBABILITIES)
		{
			Map<String, AlignmentResults> m_ep = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
			m.put(errorProbability, m_ep);
			System.out.print("Introducing fragment read errors...");
			FragmentErrorGenerator base_call_eg = new LinearIncreasingErrorGenerator(
				SequenceGenerator.NUCLEOTIDES, errorProbability / 2.0, errorProbability);
			IndelGenerator.Options igo = new IndelGenerator.Options();
			igo.deleteLengthMean = 2;
			igo.deleteLengthStdDev = 0.7;
			igo.deleteProbability = errorProbability / 40.0;
			igo.insertLengthMean = 2;
			igo.insertLengthStdDev = 0.7;
			igo.insertProbability = errorProbability / 40.0;
			FragmentErrorGenerator indel_eg = new IndelGenerator(SequenceGenerator.NUCLEOTIDES, igo);
			List<? extends Fragment> errored_list = indel_eg.generateErrors(base_call_eg.generateErrors(list));
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
			alignmentInterfaceList.add(new BowtieInterface(++index, "Bowtie", PHRED_THRESHOLDS,
				sequence, errored_list, new Options(paired_end, errorProbability), m_ep));
			alignmentInterfaceList.add(new NovoalignInterface(++index, "Novoalign",
				PHRED_THRESHOLDS, sequence, errored_list,
				new Options(paired_end, errorProbability), m_ep));

			for (AlignmentToolInterface ati : alignmentInterfaceList)
			{
				File tool_path = new File(path, String.format("%03d-%s-%s", ati.index,
					ati.description, genome.toString().toLowerCase()));
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
				ati.o.roc_output = new File(tool_path, "roc.csv");

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
		for (Future<AlignmentResults> f : futureList)
		{
			try
			{
				f.get();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}

		String filename = String.format("%s_%s.csv", testDescription,
			genome.toString().toLowerCase());
		String roc_filename = String.format("%s_%s_roc.csv", testDescription,
			genome.toString().toLowerCase());
		try
		{
			System.out.printf("Writing results to %s%n", filename);
			FileWriter w = new FileWriter(new File(path, filename));
			w.write(String.format("%s,%s,%s,%s,%s,%s%n", "Tool", "ErrorRate", "Threshold",
				"Precision", "Recall", "Time"));
			for (Double d : m.keySet())
			{
				for (String s : m.get(d).keySet())
				{
					for (Integer i : PHRED_THRESHOLDS)
					{
						AlignmentResults ar = m.get(d).get(s);
						FilteredAlignmentResults r = ar.filter(i);
						w.write(String.format("%s,%f,%d,%f,%f,%d%n", s, d, i, r.getPrecision(),
							r.getRecall(), ar.timeMap.get(AlignmentOperation.TOTAL)));
					}
				}
			}
			w.close();

			System.out.printf("Writing overall ROC data to %s%n", roc_filename);
			w = new FileWriter(new File(path, roc_filename));
			w.write(String.format("%s,%s,%s,%s%n", "Tool", "ErrorRate", "Score", "Label"));
			for (Double d : m.keySet())
			{
				for (String s : m.get(d).keySet())
				{
					// TODO: Don't duplicate code here
					AlignmentResults r = m.get(d).get(s);
					for (int p : r.positives)
					{
						w.write(String.format("%s,%f,%d,%d%n", s, d, p, 1));
					}
					for (int n : r.negatives)
					{
						w.write(String.format("%s,%f,%d,%d%n", s, d, n, 0));
					}
				}
			}
			w.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void indelSizeEvaluation(boolean paired_end, Genome genome)
	{
		final String testDescription = "indel_size";
		final int generated_genome_length = 1000000;
		CharSequence sequence = null;
		SequenceGenerator g = null;
		SequenceGenerator.Options sgo = null;
		final File path = new File("data");

		System.out.print("Reading/creating genome...");
		switch (genome)
		{
			case HUMAN_CHR22:
				final File chr22 = new File(path, "chr22.fa");
				try
				{
					/*
					 * Don't worry about casting file size to an int: we can't
					 * have strings longer than Integer.MAX_VALUE anyway
					 */
					sequence = FastaReader.getSequence(chr22, (int) chr22.length());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				break;
			case RANDOM_EASY:
				g = new SeqGenSingleSequenceMultipleRepeats();
				sgo = new SequenceGenerator.Options();
				sgo.length = generated_genome_length;
				sgo.repeatCount = 0;
				System.out.print("Generating sequence...");
				sequence = g.generateSequence(sgo);
				System.out.println("done.");
				break;
			case RANDOM_HARD:
				g = new SeqGenSingleSequenceMultipleRepeats();
				sgo = new SequenceGenerator.Options();
				sgo.length = generated_genome_length;
				sgo.repeatCount = 100;
				sgo.repeatLength = 500;
				sgo.repeatErrorProbability = 0.03;
				System.out.print("Generating sequence...");
				sequence = g.generateSequence(sgo);
				System.out.println("done.");
				break;
			default:
				break;
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

		final int alignmentToolCount = ERROR_PROBABILITIES.size() * PHRED_THRESHOLDS.size() * 7;
		List<AlignmentToolInterface> atiList = new ArrayList<AlignmentToolInterface>(
			alignmentToolCount);

		Map<Integer, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Integer, Map<String, AlignmentResults>>());
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			alignmentToolCount);

		final double errorProbability = 0.0;
		final double indelLengthStdDev = 0.2;
		final double indelFrequency = 1e-3;

		int index = 0;
		for (int run = 0; run < EVAL_RUN_COUNT; run++)
		{
			for (int indelSize : INDEL_SIZES)
			{
				Map<String, AlignmentResults> m_ep = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
				m.put(indelSize, m_ep);
				System.out.print("Introducing fragment read errors...");
				IndelGenerator.Options igo = new IndelGenerator.Options();
				igo.deleteLengthMean = indelSize;
				igo.deleteLengthStdDev = indelLengthStdDev;
				igo.deleteProbability = indelFrequency;
				igo.insertLengthMean = indelSize;
				igo.insertLengthStdDev = indelLengthStdDev;
				igo.insertProbability = indelFrequency;
				FragmentErrorGenerator indel_eg = new IndelGenerator(SequenceGenerator.NUCLEOTIDES,
					igo);
				List<? extends Fragment> errored_list = indel_eg.generateErrors(list);
				System.out.println("done.");
				List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

				Options o = new Options(paired_end, errorProbability);
				o.penalize_duplicate_mappings = false;
				alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-R-" + run,
					PHRED_THRESHOLDS, sequence, errored_list, o, m_ep));
				alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-S-" + run,
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_ep));
				o = new Options(paired_end, errorProbability);
				o.penalize_duplicate_mappings = false;
				alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-R-" + run,
					PHRED_THRESHOLDS, sequence, errored_list, o, m_ep));
				alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-S-" + run,
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_ep));
				alignmentInterfaceList.add(new SoapInterface(++index, "SOAP-" + run,
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_ep));
				alignmentInterfaceList.add(new BwaInterface(++index, "BWA-" + run,
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_ep));
				o = new Options(paired_end, errorProbability);
				o.penalize_duplicate_mappings = false;
				alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP-R-" + run,
					PHRED_THRESHOLDS, sequence, errored_list, o, m_ep));
				alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP-S-" + run,
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_ep));
				alignmentInterfaceList.add(new BowtieInterface(++index, "Bowtie-" + run,
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_ep));
				alignmentInterfaceList.add(new NovoalignInterface(++index, "Novoalign",
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_ep));

				for (AlignmentToolInterface ati : alignmentInterfaceList)
				{
					File tool_path = new File(path, String.format("%03d-%s-%s", ati.index,
						ati.description, genome.toString().toLowerCase()));
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
					ati.o.roc_output = new File(tool_path, "roc.csv");

					System.out.printf("*** %03d %s: %f%n", ati.index, ati.description,
						ati.o.error_probability);

					atiList.add(ati);
				}
			}
		}
		for (AlignmentToolInterface ati : atiList)
		{
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
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}

		String filename = String.format("%s_%s.csv", testDescription,
			genome.toString().toLowerCase());
		String roc_filename = String.format("%s_%s_roc.csv", testDescription,
			genome.toString().toLowerCase());
		try
		{
			System.out.printf("Writing results to %s%n", filename);
			FileWriter w = new FileWriter(new File(path, filename));
			w.write(String.format("%s,%s,%s,%s,%s,%s%n", "Tool", "IndelSize", "Threshold",
				"Precision", "Recall", "Time"));
			for (Integer indelSize : m.keySet())
			{
				for (String toolName : m.get(indelSize).keySet())
				{
					for (Integer i : PHRED_THRESHOLDS)
					{
						AlignmentResults ar = m.get(indelSize).get(toolName);
						FilteredAlignmentResults r = ar.filter(i);
						w.write(String.format("%s,%d,%d,%f,%f,%d%n", toolName, indelSize, i,
							r.getPrecision(), r.getRecall(),
							ar.timeMap.get(AlignmentOperation.TOTAL)));
					}
				}
			}
			w.close();

			System.out.printf("Writing overall ROC data to %s%n", roc_filename);
			w = new FileWriter(new File(path, roc_filename));
			w.write(String.format("%s,%s,%s,%s%n", "Tool", "IndelSize", "Score", "Label"));
			for (Integer indelSize : m.keySet())
			{
				for (String toolName : m.get(indelSize).keySet())
				{
					// TODO: Don't duplicate code here
					AlignmentResults r = m.get(indelSize).get(toolName);
					for (int p : r.positives)
					{
						w.write(String.format("%s,%d,%d,%d%n", toolName, indelSize, p, 1));
					}
					for (int n : r.negatives)
					{
						w.write(String.format("%s,%d,%d,%d%n", toolName, indelSize, n, 0));
					}
				}
			}
			w.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void indelFrequencyEvaluation(boolean paired_end, Genome genome)
	{
		final String testDescription = "indel_freq";
		final int generated_genome_length = 1000000;
		CharSequence sequence = null;
		SequenceGenerator g = null;
		SequenceGenerator.Options sgo = null;
		final File path = new File("data");

		System.out.print("Reading/creating genome...");
		switch (genome)
		{
			case HUMAN_CHR22:
				final File chr22 = new File(path, "chr22.fa");
				try
				{
					/*
					 * Don't worry about casting file size to an int: we can't
					 * have strings longer than Integer.MAX_VALUE anyway
					 */
					sequence = FastaReader.getSequence(chr22, (int) chr22.length());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				break;
			case RANDOM_EASY:
				g = new SeqGenSingleSequenceMultipleRepeats();
				sgo = new SequenceGenerator.Options();
				sgo.length = generated_genome_length;
				sgo.repeatCount = 0;
				System.out.print("Generating sequence...");
				sequence = g.generateSequence(sgo);
				System.out.println("done.");
				break;
			case RANDOM_HARD:
				g = new SeqGenSingleSequenceMultipleRepeats();
				sgo = new SequenceGenerator.Options();
				sgo.length = generated_genome_length;
				sgo.repeatCount = 100;
				sgo.repeatLength = 500;
				sgo.repeatErrorProbability = 0.03;
				System.out.print("Generating sequence...");
				sequence = g.generateSequence(sgo);
				System.out.println("done.");
				break;
			default:
				break;
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

		final int alignmentToolCount = ERROR_PROBABILITIES.size() * PHRED_THRESHOLDS.size() * 7;
		List<AlignmentToolInterface> atiList = new ArrayList<AlignmentToolInterface>(
			alignmentToolCount);

		Map<Double, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Double, Map<String, AlignmentResults>>());
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			alignmentToolCount);

		final int indelLengthMean = 2;
		final double indelLengthStdDev = 0.2;

		int index = 0;
		for (int run = 0; run < EVAL_RUN_COUNT; run++)
		{
			for (double indelFrequency : INDEL_FREQUENCIES)
			{
				Map<String, AlignmentResults> m_ep = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
				m.put(indelFrequency, m_ep);
				System.out.print("Introducing fragment read errors...");
				IndelGenerator.Options igo = new IndelGenerator.Options();
				igo.deleteLengthMean = indelLengthMean;
				igo.deleteLengthStdDev = indelLengthStdDev;
				igo.deleteProbability = indelFrequency;
				igo.insertLengthMean = indelLengthMean;
				igo.insertLengthStdDev = indelLengthStdDev;
				igo.insertProbability = indelFrequency;
				FragmentErrorGenerator indel_eg = new IndelGenerator(SequenceGenerator.NUCLEOTIDES,
					igo);
				List<? extends Fragment> errored_list = indel_eg.generateErrors(list);
				System.out.println("done.");
				List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

				Options o = new Options(paired_end, indelFrequency);
				o.penalize_duplicate_mappings = false;
				alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-R",
					PHRED_THRESHOLDS, sequence, errored_list, o, m_ep));
				alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-S",
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						indelFrequency), m_ep));
				o = new Options(paired_end, indelFrequency);
				o.penalize_duplicate_mappings = false;
				alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-R",
					PHRED_THRESHOLDS, sequence, errored_list, o, m_ep));
				alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-S",
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						indelFrequency), m_ep));
				alignmentInterfaceList.add(new SoapInterface(++index, "SOAP", PHRED_THRESHOLDS,
					sequence, errored_list, new Options(paired_end, indelFrequency), m_ep));
				alignmentInterfaceList.add(new BwaInterface(++index, "BWA", PHRED_THRESHOLDS,
					sequence, errored_list, new Options(paired_end, indelFrequency), m_ep));
				o = new Options(paired_end, indelFrequency);
				o.penalize_duplicate_mappings = false;
				alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP-R",
					PHRED_THRESHOLDS, sequence, errored_list, o, m_ep));
				alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP-S",
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						indelFrequency), m_ep));
				alignmentInterfaceList.add(new BowtieInterface(++index, "Bowtie", PHRED_THRESHOLDS,
					sequence, errored_list, new Options(paired_end, indelFrequency), m_ep));
				alignmentInterfaceList.add(new NovoalignInterface(++index, "Novoalign",
					PHRED_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						indelFrequency), m_ep));

				for (AlignmentToolInterface ati : alignmentInterfaceList)
				{
					File tool_path = new File(path, String.format("%03d-%s-%s", ati.index,
						ati.description, genome.toString().toLowerCase()));
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
					ati.o.roc_output = new File(tool_path, "roc.csv");

					System.out.printf("*** %03d %s: %f%n", ati.index, ati.description,
						ati.o.error_probability);

					atiList.add(ati);
				}
			}
		}
		for (AlignmentToolInterface ati : atiList)
		{
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
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}

		String filename = String.format("%s_%s.csv", testDescription,
			genome.toString().toLowerCase());
		String roc_filename = String.format("%s_%s_roc.csv", testDescription,
			genome.toString().toLowerCase());
		try
		{
			System.out.printf("Writing results to %s%n", filename);
			FileWriter w = new FileWriter(new File(path, filename));
			w.write(String.format("%s,%s,%s,%s,%s,%s%n", "Tool", "IndelFrequency", "Threshold",
				"Precision", "Recall", "Time"));
			for (Double indelFrequency : m.keySet())
			{
				for (String toolName : m.get(indelFrequency).keySet())
				{
					for (Integer threshold : PHRED_THRESHOLDS)
					{
						AlignmentResults ar = m.get(indelFrequency).get(toolName);
						FilteredAlignmentResults r = ar.filter(threshold);
						w.write(String.format("%s,%f,%d,%f,%f,%d%n", toolName, indelFrequency,
							threshold, r.getPrecision(), r.getRecall(),
							ar.timeMap.get(AlignmentOperation.TOTAL)));
					}
				}
			}
			w.close();

			System.out.printf("Writing overall ROC data to %s%n", roc_filename);
			w = new FileWriter(new File(path, roc_filename));
			w.write(String.format("%s,%s,%s,%s%n", "Tool", "IndelFrequency", "Score", "Label"));
			for (Double indelFrequency : m.keySet())
			{
				for (String toolName : m.get(indelFrequency).keySet())
				{
					// TODO: Don't duplicate code here
					AlignmentResults r = m.get(indelFrequency).get(toolName);
					for (int p : r.positives)
					{
						w.write(String.format("%s,%f,%d,%d%n", toolName, indelFrequency, p, 1));
					}
					for (int n : r.negatives)
					{
						w.write(String.format("%s,%f,%d,%d%n", toolName, indelFrequency, n, 0));
					}
				}
			}
			w.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * TODO: Don't duplicate code
	 */
	public void runtimeCoverageEvaluation()
	{
		final String testDescription = "runtime_coverage";
		List<Map<Integer, Map<String, AlignmentResults>>> l = new ArrayList<Map<Integer, Map<String, AlignmentResults>>>(
			EVAL_RUN_COUNT);
		final boolean paired_end = false;
		final double errorProbability = 0.05;
		final File path = new File("data");

		SequenceGenerator g = new SeqGenSingleSequenceMultipleRepeats();
		SequenceGenerator.Options sgo = new SequenceGenerator.Options();
		sgo.length = 500000;
		System.out.print("Generating sequence...");
		CharSequence sequence = g.generateSequence(sgo);
		System.out.println("done.");
		System.out.printf("Genome length: %d%n", sequence.length());

		path.mkdirs();
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			RUNTIME_COVERAGES.size() * EVAL_RUN_COUNT * 7);
		int index = 0;
		for (int which_run = 0; which_run < EVAL_RUN_COUNT; which_run++)
		{
			Map<Integer, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Integer, Map<String, AlignmentResults>>());
			l.add(m);

			for (int coverage : RUNTIME_COVERAGES)
			{
				Map<String, AlignmentResults> m_c = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
				m.put(coverage, m_c);
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

				alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast",
					RUNTIME_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_c));
				alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast",
					RUNTIME_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_c));
				alignmentInterfaceList.add(new SoapInterface(++index, "SOAP", RUNTIME_THRESHOLDS,
					sequence, errored_list, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new BwaInterface(++index, "BWA", RUNTIME_THRESHOLDS,
					sequence, errored_list, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP",
					RUNTIME_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_c));
				alignmentInterfaceList.add(new BowtieInterface(++index, "Bowtie",
					RUNTIME_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_c));
				alignmentInterfaceList.add(new NovoalignInterface(++index, "Novoalign",
					RUNTIME_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_c));

				for (AlignmentToolInterface ati : alignmentInterfaceList)
				{
					File tool_path = new File(path, String.format("%03d-%s", ati.index,
						ati.description));
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
					ati.o.roc_output = new File(tool_path, "roc.csv");

					System.out.printf("*** %03d %s: %d%n", ati.index, ati.description, coverage);

					futureList.add(pool.submit(ati));
				}
			}

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
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}
		String roc_filename = testDescription + "_data.csv";
		System.out.printf("Writing time data to %s%n", roc_filename);
		try
		{
			FileWriter w = new FileWriter(new File(path, roc_filename));
			w.write(String.format("Tool,Coverage,PreprocessingTime,AlignmentTime,PostprocessingTime,TotalTime%n"));
			for (Map<Integer, Map<String, AlignmentResults>> m : l)
			{
				for (Integer c : m.keySet())
				{
					for (String s : m.get(c).keySet())
					{
						AlignmentResults r = m.get(c).get(s);
						w.write(String.format("%s,%d,%d,%d,%d,%d%n", s, c,
							r.timeMap.get(AlignmentOperation.PREPROCESSING),
							r.timeMap.get(AlignmentOperation.ALIGNMENT),
							r.timeMap.get(AlignmentOperation.POSTPROCESSING),
							r.timeMap.get(AlignmentOperation.TOTAL)));
					}
				}
			}
			w.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * TODO: Don't duplicate code
	 */
	public void runtimeGenomeSizeEvaluation()
	{
		final String testDescription = "runtime_genome_size";
		List<Map<Integer, Map<String, AlignmentResults>>> l = new ArrayList<Map<Integer, Map<String, AlignmentResults>>>(
			EVAL_RUN_COUNT);
		final boolean paired_end = false;
		final double errorProbability = 0.05;
		final int coverage = 3;
		final File path = new File("data");

		path.mkdirs();
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			RUNTIME_COVERAGES.size() * EVAL_RUN_COUNT * 7);
		int index = 0;
		for (int which_run = 0; which_run < EVAL_RUN_COUNT; which_run++)
		{
			Map<Integer, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Integer, Map<String, AlignmentResults>>());
			l.add(m);

			for (int genome_size : RUNTIME_GENOME_SIZES)
			{
				SequenceGenerator g = new SeqGenSingleSequenceMultipleRepeats();
				SequenceGenerator.Options sgo = new SequenceGenerator.Options();
				sgo.length = genome_size;
				System.out.print("Generating sequence...");
				CharSequence sequence = g.generateSequence(sgo);
				System.out.println("done.");
				System.out.printf("Genome length: %d%n", sequence.length());
				Map<String, AlignmentResults> m_c = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
				m.put(genome_size, m_c);
				Fragmentizer.Options fo = new Fragmentizer.Options();
				fo.k = 50;
				/*
				 * Integer truncation is okay here
				 */
				fo.n = (genome_size * coverage) / fo.k;
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

				alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast",
					RUNTIME_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_c));
				alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast",
					RUNTIME_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_c));
				alignmentInterfaceList.add(new SoapInterface(++index, "SOAP", RUNTIME_THRESHOLDS,
					sequence, errored_list, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new BwaInterface(++index, "BWA", RUNTIME_THRESHOLDS,
					sequence, errored_list, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP",
					RUNTIME_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_c));
				alignmentInterfaceList.add(new BowtieInterface(++index, "Bowtie",
					RUNTIME_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_c));
				alignmentInterfaceList.add(new NovoalignInterface(++index, "Novoalign",
					RUNTIME_THRESHOLDS, sequence, errored_list, new Options(paired_end,
						errorProbability), m_c));

				for (AlignmentToolInterface ati : alignmentInterfaceList)
				{
					File tool_path = new File(path, String.format("%03d-%s", ati.index,
						ati.description));
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
					ati.o.roc_output = new File(tool_path, "roc.csv");

					System.out.printf("*** %03d %s: %d%n", ati.index, ati.description, genome_size);

					futureList.add(pool.submit(ati));
				}
			}

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
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}
		String roc_filename = testDescription + "_data.csv";
		System.out.printf("Writing time data to %s%n", roc_filename);
		try
		{
			FileWriter w = new FileWriter(new File(path, roc_filename));
			w.write(String.format("Tool,GenomeLength,PreprocessingTime,AlignmentTime,PostprocessingTime,TotalTime%n"));
			for (Map<Integer, Map<String, AlignmentResults>> m : l)
			{
				for (Integer c : m.keySet())
				{
					for (String s : m.get(c).keySet())
					{
						AlignmentResults r = m.get(c).get(s);
						w.write(String.format("%s,%d,%d,%d,%d,%d%n", s, c,
							r.timeMap.get(AlignmentOperation.PREPROCESSING),
							r.timeMap.get(AlignmentOperation.ALIGNMENT),
							r.timeMap.get(AlignmentOperation.POSTPROCESSING),
							r.timeMap.get(AlignmentOperation.TOTAL)));
					}
				}
			}
			w.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args)
	{
		new AlignmentToolService().indelFrequencyEvaluation(false, Genome.RANDOM_EASY);
		new AlignmentToolService().indelFrequencyEvaluation(false, Genome.RANDOM_HARD);
		new AlignmentToolService().indelFrequencyEvaluation(false, Genome.HUMAN_CHR22);
		new AlignmentToolService().indelSizeEvaluation(false, Genome.RANDOM_EASY);
		new AlignmentToolService().indelSizeEvaluation(false, Genome.RANDOM_HARD);
		new AlignmentToolService().indelSizeEvaluation(false, Genome.HUMAN_CHR22);
	}
}
