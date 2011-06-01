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
import generator.SeqFilterSingleDeletion;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SeqFilterTandemRepeats;
import generator.SequenceGenerator;
import generator.errors.FragmentErrorGenerator;
import generator.errors.IndelGenerator;
import generator.errors.LinearIncreasingErrorGenerator;
import generator.errors.UniformErrorGenerator;
import io.FastaReader;
import io.FastaWriter;
import io.FastqWriter;
import java.io.BufferedWriter;
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
import assembly.FragmentPositionSource;

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
	private static final int EVAL_RUN_COUNT = 1;
	protected static final List<Double> ERROR_PROBABILITIES = Collections.unmodifiableList(Arrays.asList(
		0.0, 0.001, 0.004, 0.01, 0.025, 0.05, 0.1));
	protected static final List<Integer> PHRED_THRESHOLDS = Collections.unmodifiableList(Arrays.asList(
		0, 1, 2, 3, 4, 5, 7, 10, 14, 20, 25, 30, 35, 40));
	protected static final List<Integer> RUNTIME_THRESHOLDS = Collections.unmodifiableList(Arrays.asList(0));
	protected static final List<Double> RUNTIME_GENOME_SIZES = Collections.unmodifiableList(Arrays.asList(
		5000.0, 20000.0, 100000.0, 500000.0, 1000000.0));
	protected static final List<Double> RUNTIME_COVERAGES = Collections.unmodifiableList(Arrays.asList(
		3.0, 7.0, 10.0, 13.0, 16.0, 20.0));
	protected static final List<Double> INDEL_SIZES = Collections.unmodifiableList(Arrays.asList(
		2.0, 4.0, 7.0, 10.0, 16.0));
	protected static final List<Double> INDEL_FREQUENCIES = Collections.unmodifiableList(Arrays.asList(
		1e-5, 3e-5, 1e-4, 3e-4, 1e-3, 3e-3, 1e-2));
	protected static final List<Integer> TANDEM_GENOME_REPEAT_COUNTS = Collections.unmodifiableList(Arrays.asList(
		0, 10, 20, 50, 100, 250));

	private final ExecutorService pool;

	private static final File DATA_PATH = new File("data");

	public AlignmentToolService()
	{
		pool = Executors.newFixedThreadPool(NUMBER_OF_CONCURRENT_THREADS);
	}

	private static enum Genome
	{
		HUMAN_CHR22,
		HUMAN_2GB,
		RANDOM_EASY,
		RANDOM_HARD,
		RUNTIME_COV_RANDOM,
		RUNTIME_SIZE_RANDOM,
	}

	/**
	 * I <b>really</b> wish I could just return a tuple instead of making
	 * classes like this :(
	 * 
	 * @author mruffalo
	 */
	private static class ProcessedGenome
	{
		public ProcessedGenome(File file_, List<? extends Fragment> fragments_)
		{
			file = file_;
			fragments = fragments_;
		}

		/**
		 * The file in which this genome has been written to disk -- not where
		 * it was read from if applicable
		 */
		public final File file;
		public final List<? extends Fragment> fragments;
	}

	/**
	 * TODO: Figure out if I should refactor this
	 * 
	 * @author mruffalo
	 */
	private static class SimulationParameters
	{
		public SimulationParameters(List<Double> errorRates_, boolean paired_end_,
			String testDescription_, Genome genome_, File genomeFile_,
			Map<Double, File> fragmentsByError_)
		{
			errorRates = errorRates_;
			paired_end = paired_end_;
			testDescription = testDescription_;
			genome = genome_;
			genomeFile = genomeFile_;
			fragmentsByError = fragmentsByError_;
		}

		public final List<Double> errorRates;
		public final boolean paired_end;
		public final String testDescription;
		public final Genome genome;
		public final File genomeFile;
		public final Map<Double, File> fragmentsByError;
	}

	private ProcessedGenome getGenomeAndCleanFragments(Genome genome)
	{
		DATA_PATH.mkdirs();
		// TODO: Don't hardcode this
		final int generated_genome_length = 100000;

		File genomeFile = null;
		CharSequence sequence = null;
		SequenceGenerator g = null;
		SequenceGenerator.Options sgo = null;
		System.out.print("Reading/creating genome ... ");
		switch (genome)
		{
			case HUMAN_CHR22:
				final File chr22 = new File(DATA_PATH, "chr22.fa");
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
				genomeFile = new File(DATA_PATH, "chr22-contig.fa");
				break;
			case HUMAN_2GB:
				final File hg19_2gb = new File(DATA_PATH, "hg19_2gb.fa");
				try
				{
					/*
					 * Don't worry about casting file size to an int: we can't
					 * have strings longer than Integer.MAX_VALUE anyway
					 */
					sequence = FastaReader.getSequence(hg19_2gb, (int) hg19_2gb.length());
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				genomeFile = new File(DATA_PATH, "hg19_2gb-contig.fa");
				break;
			case RANDOM_EASY:
				genomeFile = new File(DATA_PATH, "random_easy.fa");
				g = new SeqGenSingleSequenceMultipleRepeats();
				sgo = new SequenceGenerator.Options();
				sgo.length = generated_genome_length;
				sgo.repeatCount = 0;
				sequence = g.generateSequence(sgo);
				break;
			case RANDOM_HARD:
				genomeFile = new File(DATA_PATH, "random_hard.fa");
				g = new SeqGenSingleSequenceMultipleRepeats();
				sgo = new SequenceGenerator.Options();
				sgo.length = generated_genome_length;
				sgo.repeatCount = 100;
				sgo.repeatLength = 500;
				sgo.repeatErrorProbability = 0.03;
				sequence = g.generateSequence(sgo);
				break;
			default:
				break;
		}
		System.out.println("done.");
		System.out.printf("Genome length: %d%n", sequence.length());

		System.out.printf("Writing genome to %s  ... ", genomeFile.getAbsolutePath());
		writeGenome(sequence, genomeFile);
		System.out.println("done.");

		Fragmentizer.Options fo = new Fragmentizer.Options();
		// TODO: Don't hardcode these
		fo.fragmentLength = 50;
		fo.fragmentCount = 50000;
		fo.fragmentLengthSd = 1;

		System.out.print("Reading fragments ... ");
		final List<? extends Fragment> list = Fragmentizer.fragmentize(sequence, fo);
		System.out.println("done.");
		return new ProcessedGenome(genomeFile, list);
	}

	private Map<Double, Map<String, AlignmentResults>> runAccuracySimulation(SimulationParameters p)
	{
		ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_CONCURRENT_THREADS);
		List<AlignmentToolInterface> atiList = new ArrayList<AlignmentToolInterface>();

		Map<Double, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Double, Map<String, AlignmentResults>>());
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>();

		int index = 0;
		for (double errorProbability : p.errorRates)
		{
			Map<String, AlignmentResults> m_ep = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
			m.put(errorProbability, m_ep);

			List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

			Options o = new Options(p.paired_end, errorProbability);
			o.penalize_duplicate_mappings = false;
			alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-R", PHRED_THRESHOLDS,
				o, m_ep));
			alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-S", PHRED_THRESHOLDS,
				new Options(p.paired_end, errorProbability), m_ep));
			o = new Options(p.paired_end, errorProbability);
			o.penalize_duplicate_mappings = false;
			alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-R", PHRED_THRESHOLDS,
				o, m_ep));
			alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-S", PHRED_THRESHOLDS,
				new Options(p.paired_end, errorProbability), m_ep));
			alignmentInterfaceList.add(new SoapInterface(++index, "SOAP", PHRED_THRESHOLDS,
				new Options(p.paired_end, errorProbability), m_ep));
			alignmentInterfaceList.add(new BwaInterface(++index, "BWA", PHRED_THRESHOLDS,
				new Options(p.paired_end, errorProbability), m_ep));
			o = new Options(p.paired_end, errorProbability);
			o.penalize_duplicate_mappings = false;
			alignmentInterfaceList.add(new BowtieInterface(++index, "Bowtie", PHRED_THRESHOLDS,
				new Options(p.paired_end, errorProbability), m_ep));
			alignmentInterfaceList.add(new NovoalignInterface(++index, "Novoalign",
				PHRED_THRESHOLDS, new Options(p.paired_end, errorProbability), m_ep));

			for (AlignmentToolInterface ati : alignmentInterfaceList)
			{
				File tool_path = new File(DATA_PATH, String.format("%03d-%s-%s-%s", ati.index,
					ati.description, p.testDescription, p.genome.toString().toLowerCase()));
				tool_path.mkdirs();
				ati.o.genome = new File(tool_path, "genome.fasta");
				ati.o.binary_genome = new File(tool_path, "genome.bfa");
				ati.o.orig_genome = p.genomeFile;

				int read_count = p.paired_end ? 2 : 1;
				for (int i = 1; i <= read_count; i++)
				{
					Options.Reads r = new Options.Reads(i);
					r.reads = new File(tool_path, String.format("fragments%d.fastq", i));
					r.binary_reads = new File(tool_path, String.format("fragments%d.bfq", i));
					r.aligned_reads = new File(tool_path, String.format("alignment%d.sai", i));
					r.orig_reads = p.fragmentsByError.get(errorProbability);
					ati.o.reads.add(r);
				}
				ati.o.raw_output = new File(tool_path, "out.raw");
				ati.o.sam_output = new File(tool_path, "alignment.sam");
				ati.o.converted_output = new File(tool_path, "out.txt");
				ati.o.roc_output = new File(tool_path, "roc.csv");

				System.out.printf("*** %03d %s: %f%n", ati.index, ati.description, ati.o.error_rate);

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
		return m;
	}

	private void writeAccuracyResults(SimulationParameters pa,
		Map<Double, Map<String, AlignmentResults>> m, String parameterName)
	{
		String filename = String.format("%s_%s.csv", pa.testDescription,
			pa.genome.toString().toLowerCase());
		String roc_filename = String.format("%s_%s_roc.csv", pa.testDescription,
			pa.genome.toString().toLowerCase());
		try
		{
			System.out.printf("Writing results to %s%n", filename);
			FileWriter w = new FileWriter(new File(DATA_PATH, filename));
			w.write(String.format("%s,%s,%s,%s,%s,%s,%s%n", "Tool", parameterName, "Threshold",
				"Precision", "Recall", "Time", "UsedReadRatio"));
			for (Double d : m.keySet())
			{
				for (String s : m.get(d).keySet())
				{
					for (Integer i : PHRED_THRESHOLDS)
					{
						AlignmentResults ar = m.get(d).get(s);
						FilteredAlignmentResults r = ar.filter(i);
						w.write(String.format("%s,%f,%d,%f,%f,%d,%f%n", s, d, i, r.getPrecision(),
							r.getRecall(), ar.timeMap.get(AlignmentOperation.TOTAL),
							r.getUsedReadRatio()));
					}
				}
			}
			w.close();

			System.out.printf("Writing overall ROC data to %s%n", roc_filename);
			w = new FileWriter(new File(DATA_PATH, roc_filename));
			w.write(String.format("%s,%s,%s,%s%n", "Tool", parameterName, "Score", "Label"));
			for (Double d : m.keySet())
			{
				for (String s : m.get(d).keySet())
				{
					// TODO: Don't duplicate code here
					AlignmentResults r = m.get(d).get(s);
					for (Map.Entry<Integer, Integer> p : r.positives.entrySet())
					{
						int score = p.getKey();
						int count = p.getValue();
						for (int i = 0; i < count; i++)
						{
							w.write(String.format("%s,%f,%d,%d%n", s, d, score, 1));
						}
					}
					for (Map.Entry<Integer, Integer> n : r.negatives.entrySet())
					{
						int score = n.getKey();
						int count = n.getValue();
						for (int i = 0; i < count; i++)
						{
							w.write(String.format("%s,%f,%d,%d%n", s, d, score, 0));
						}
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
	 * TODO: Don't duplicate code from {@link #writeRuntimeResults}
	 * 
	 * @param pa
	 * @param m
	 * @param parameterName
	 */
	private void writeRuntimeResults(SimulationParameters pa,
		List<Map<Double, Map<String, AlignmentResults>>> l, String parameterName)
	{
		String filename = pa.testDescription + "_data.csv";
		System.out.printf("Writing time data to %s%n", filename);
		try
		{
			FileWriter w = new FileWriter(new File(DATA_PATH, filename));
			w.write(String.format(
				"Tool,%s,PreprocessingTime,AlignmentTime,PostprocessingTime,TotalTime%n",
				parameterName));
			for (Map<Double, Map<String, AlignmentResults>> m : l)
			{
				for (Double c : m.keySet())
				{
					for (String s : m.get(c).keySet())
					{
						AlignmentResults r = m.get(c).get(s);
						w.write(String.format("%s,%f,%d,%d,%d,%d%n", s, c,
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

	public static void writeGenome(CharSequence genome, File file)
	{
		try
		{
			FastaWriter.writeSequence(genome, file);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void writeFragments(File file, List<? extends Fragment> fragments)
	{
		try
		{
			FastqWriter.writeFragments(fragments, file, 0);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void errorRateEvaluation(boolean paired_end, Genome genome)
	{
		final String testDescription = "error_rate";

		ProcessedGenome pg = getGenomeAndCleanFragments(genome);

		Map<Double, File> fragmentsByError = new TreeMap<Double, File>();
		for (double errorProbability : ERROR_PROBABILITIES)
		{
			String error_identifier = Double.toString(errorProbability).replace('.', '_');
			String filename = String.format("fragments-%s-%s.fastq", testDescription,
				error_identifier);
			File fragments = new File(DATA_PATH, filename);
			fragmentsByError.put(errorProbability, fragments);

			System.out.printf("Introducing fragment read errors for error rate %f ... ",
				errorProbability);
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
			List<FragmentErrorGenerator> generatorList = new ArrayList<FragmentErrorGenerator>();
			generatorList.add(base_call_eg);
			generatorList.add(indel_eg);
			FragmentErrorGenerator.generateErrorsToFile(generatorList, pg.fragments, fragments);
			System.out.println("done.");
		}

		SimulationParameters pa = new SimulationParameters(ERROR_PROBABILITIES, paired_end,
			testDescription, genome, pg.file, fragmentsByError);

		Map<Double, Map<String, AlignmentResults>> m = runAccuracySimulation(pa);

		writeAccuracyResults(pa, m, "ErrorRate");
	}

	public void indelSizeEvaluation(boolean paired_end, Genome genome)
	{
		final String testDescription = "indel_size";

		ProcessedGenome pg = getGenomeAndCleanFragments(genome);

		final double indelLengthStdDev = 0.2;
		final double indelFrequency = 5e-2;
		Map<Double, File> fragmentsByIndelSize = new TreeMap<Double, File>();
		for (double indelSize : INDEL_SIZES)
		{
			String error_identifier = String.format("%.0f", indelSize);
			String filename = String.format("fragments-%s-%s.fastq", testDescription,
				error_identifier);
			File fragments = new File(DATA_PATH, filename);
			fragmentsByIndelSize.put(indelSize, fragments);

			System.out.printf("Introducing fragment read errors for indel size %.0f ... ",
				indelSize);
			IndelGenerator.Options igo = new IndelGenerator.Options();
			igo.deleteLengthMean = indelSize;
			igo.deleteLengthStdDev = indelLengthStdDev;
			igo.deleteProbability = indelFrequency;
			igo.insertLengthMean = indelSize;
			igo.insertLengthStdDev = indelLengthStdDev;
			igo.insertProbability = indelFrequency;
			FragmentErrorGenerator indel_eg = new IndelGenerator(SequenceGenerator.NUCLEOTIDES, igo);
			List<FragmentErrorGenerator> generatorList = new ArrayList<FragmentErrorGenerator>();
			generatorList.add(indel_eg);
			FragmentErrorGenerator.generateErrorsToFile(generatorList, pg.fragments, fragments);
			System.out.println("done.");
		}

		SimulationParameters pa = new SimulationParameters(INDEL_SIZES, paired_end,
			testDescription, genome, pg.file, fragmentsByIndelSize);
		Map<Double, Map<String, AlignmentResults>> m = runAccuracySimulation(pa);

		writeAccuracyResults(pa, m, "IndelSize");
	}

	public void indelFrequencyEvaluation(boolean paired_end, Genome genome)
	{
		final String testDescription = "indel_freq";

		ProcessedGenome pg = getGenomeAndCleanFragments(genome);

		final int indelLengthMean = 2;
		final double indelLengthStdDev = 0.2;
		Map<Double, File> fragmentsByIndelFreq = new TreeMap<Double, File>();
		for (double indelFrequency : INDEL_FREQUENCIES)
		{
			String error_identifier = Double.toString(indelFrequency).replace('.', '_');
			String filename = String.format("fragments-%s-%s.fastq", testDescription,
				error_identifier);
			File fragments = new File(DATA_PATH, filename);
			fragmentsByIndelFreq.put(indelFrequency, fragments);

			System.out.printf("Introducing fragment read errors for indel frequency %f ... ",
				indelFrequency);
			IndelGenerator.Options igo = new IndelGenerator.Options();
			igo.deleteLengthMean = indelLengthMean;
			igo.deleteLengthStdDev = indelLengthStdDev;
			igo.deleteProbability = indelFrequency;
			igo.insertLengthMean = indelLengthMean;
			igo.insertLengthStdDev = indelLengthStdDev;
			igo.insertProbability = indelFrequency;
			FragmentErrorGenerator indel_eg = new IndelGenerator(SequenceGenerator.NUCLEOTIDES, igo);
			List<FragmentErrorGenerator> generatorList = new ArrayList<FragmentErrorGenerator>();
			generatorList.add(indel_eg);
			FragmentErrorGenerator.generateErrorsToFile(generatorList, pg.fragments, fragments);
			System.out.println("done.");
		}

		SimulationParameters pa = new SimulationParameters(INDEL_FREQUENCIES, paired_end,
			testDescription, genome, pg.file, fragmentsByIndelFreq);
		Map<Double, Map<String, AlignmentResults>> m = runAccuracySimulation(pa);

		writeAccuracyResults(pa, m, "IndelFrequency");
	}

	/**
	 * XXX Fix this after recent memory-reducing changes
	 * 
	 * @param paired_end
	 */
	public void tandemIndelFrequencyEvaluation(boolean paired_end)
	{
		final String testDescription = "indel_freq_tandem";
		final int generated_genome_length = 1000000;
		CharSequence origSequence = null;
		SeqFilterTandemRepeats g = null;
		DATA_PATH.mkdirs();

		final int alignmentToolCount = ERROR_PROBABILITIES.size() * PHRED_THRESHOLDS.size() * 7;
		List<AlignmentToolInterface> atiList = new ArrayList<AlignmentToolInterface>(
			alignmentToolCount);

		Map<Integer, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Integer, Map<String, AlignmentResults>>());
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			alignmentToolCount);

		System.out.print("Generating clean sequence...");
		origSequence = SequenceGenerator.generateSequence(SequenceGenerator.NUCLEOTIDES,
			generated_genome_length);
		System.out.println("done.");

		int index = 0;
		for (int repeatCount : TANDEM_GENOME_REPEAT_COUNTS)
		{
			double dRepeatCount = repeatCount;
			System.out.print("Creating genome...");
			SeqFilterTandemRepeats.Options sgo = new SeqFilterTandemRepeats.Options();
			sgo.repeatCount = repeatCount;
			sgo.repeatLength = 500;
			sgo.repeatErrorProbability = 0.0;
			g = new SeqFilterTandemRepeats(sgo);
			System.out.print("Inserting repeats into generated sequence...");
			CharSequence repeated = g.filter(origSequence);

			System.out.println("done.");
			System.out.printf("Genome length: %d%n", repeated.length());
			Fragmentizer.Options fo = new Fragmentizer.Options();
			fo.fragmentLength = 50;
			fo.fragmentCount = 500000;
			fo.fragmentLengthSd = 1;

			System.out.print("Reading fragments...");
			List<? extends Fragment> list = Fragmentizer.fragmentize(repeated, fo);
			System.out.println("done.");

			Map<String, AlignmentResults> m_ep = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
			m.put(repeatCount, m_ep);
			for (int run = 0; run < EVAL_RUN_COUNT; run++)
			{
				List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

				Options o = new Options(paired_end, dRepeatCount);
				o.penalize_duplicate_mappings = false;
				alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-R-" + run,
					PHRED_THRESHOLDS, o, m_ep));
				alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-S-" + run,
					PHRED_THRESHOLDS, new Options(paired_end, dRepeatCount), m_ep));
				o = new Options(paired_end, dRepeatCount);
				o.penalize_duplicate_mappings = false;
				alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-R-" + run,
					PHRED_THRESHOLDS, o, m_ep));
				alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-S-" + run,
					PHRED_THRESHOLDS, new Options(paired_end, dRepeatCount), m_ep));
				alignmentInterfaceList.add(new SoapInterface(++index, "SOAP-" + run,
					PHRED_THRESHOLDS, new Options(paired_end, dRepeatCount), m_ep));
				alignmentInterfaceList.add(new BwaInterface(++index, "BWA-" + run,
					PHRED_THRESHOLDS, new Options(paired_end, dRepeatCount), m_ep));
				alignmentInterfaceList.add(new BowtieInterface(++index, "Bowtie-" + run,
					PHRED_THRESHOLDS, new Options(paired_end, dRepeatCount), m_ep));

				for (AlignmentToolInterface ati : alignmentInterfaceList)
				{
					File tool_path = new File(DATA_PATH, String.format("%03d-%s-%s", ati.index,
						ati.description, "tandem"));
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
						ati.o.error_rate);

					atiList.add(ati);
				}
			}
		}
		for (AlignmentToolInterface ati : atiList)
		{
			futureList.add(pool.submit(ati));
		}
		System.out.printf("Running %d total tool evaluations%n", futureList.size());

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

		String filename = String.format("%s_%s.csv", testDescription, "tandem");
		String roc_filename = String.format("%s_%s_roc.csv", testDescription, "tandem");
		try
		{
			System.out.printf("Writing results to %s%n", filename);
			FileWriter w = new FileWriter(new File(DATA_PATH, filename));
			w.write(String.format("%s,%s,%s,%s,%s,%s,%s%n", "Tool", "GenomeRepeatCount",
				"Threshold", "Precision", "Recall", "Time", "UsedReadRatio"));
			for (int repeatCount : m.keySet())
			{
				for (String toolName : m.get(repeatCount).keySet())
				{
					for (Integer threshold : PHRED_THRESHOLDS)
					{
						AlignmentResults ar = m.get(repeatCount).get(toolName);
						FilteredAlignmentResults r = ar.filter(threshold);
						w.write(String.format("%s,%d,%d,%f,%f,%d,%f%n", toolName, repeatCount,
							threshold, r.getPrecision(), r.getRecall(),
							ar.timeMap.get(AlignmentOperation.TOTAL), r.getUsedReadRatio()));
					}
				}
			}
			w.close();

			System.out.printf("Writing overall ROC data to %s%n", roc_filename);
			w = new FileWriter(new File(DATA_PATH, roc_filename));
			w.write(String.format("%s,%s,%s,%s%n", "Tool", "IndelFrequency", "Score", "Label"));
			for (int repeatCount : m.keySet())
			{
				for (String toolName : m.get(repeatCount).keySet())
				{
					// TODO: Don't duplicate code here
					AlignmentResults r = m.get(repeatCount).get(toolName);
					for (Map.Entry<Integer, Integer> p : r.positives.entrySet())
					{
						int score = p.getKey();
						int count = p.getValue();
						for (int i = 0; i < count; i++)
						{
							w.write(String.format("%s,%f,%d,%d%n", toolName, repeatCount, score, 1));
						}
					}
					for (Map.Entry<Integer, Integer> n : r.negatives.entrySet())
					{
						int score = n.getKey();
						int count = n.getValue();
						for (int i = 0; i < count; i++)
						{
							w.write(String.format("%s,%f,%d,%d%n", toolName, repeatCount, score, 0));
						}
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
	 * This really is the same code as {@link tandemIndelFrequencyEvaluation},
	 * just with a different sequence filter. This is a promising way to clean
	 * this up, which I really should do sooner or later
	 * 
	 * @param paired_end
	 */
	public void bigDeletionEvaluation(boolean paired_end)
	{
		final String testDescription = "big_deletion_200";
		final int generated_genome_length = 1000000;
		CharSequence origSequence = null;
		DATA_PATH.mkdirs();

		final int alignmentToolCount = ERROR_PROBABILITIES.size() * PHRED_THRESHOLDS.size() * 7;
		List<AlignmentToolInterface> atiList = new ArrayList<AlignmentToolInterface>(
			alignmentToolCount);

		Map<Integer, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Integer, Map<String, AlignmentResults>>());
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			alignmentToolCount);

		System.out.print("Generating clean sequence...");
		origSequence = SequenceGenerator.generateSequence(SequenceGenerator.NUCLEOTIDES,
			generated_genome_length);
		System.out.println("done.");

		int index = 0;
		double dRepeatCount = 0;

		System.out.print("Deleting part of sequence...");
		SeqFilterSingleDeletion.Options so = new SeqFilterSingleDeletion.Options();
		so.length = 10000;
		SeqFilterSingleDeletion sfsd = new SeqFilterSingleDeletion(so);
		CharSequence filtered = sfsd.filter(origSequence);
		System.out.printf("%d characters removed at position %d%n", so.length,
			sfsd.getDeletePosition());

		System.out.println("done.");
		System.out.printf("Genome length: %d%n", filtered.length());
		Fragmentizer.Options fo = new Fragmentizer.Options();
		fo.fragmentLength = 200;
		fo.fragmentCount = 500000;
		fo.fragmentLengthSd = 1;

		System.out.print("Reading fragments...");
		List<? extends Fragment> list = Fragmentizer.fragmentize(filtered, fo);
		System.out.println("done.");

		int fragmentsAcrossBreakpoint = 0;
		for (Fragment f : list)
		{
			int distanceFromBreakpoint = sfsd.getDeletePosition()
					- f.getPosition(FragmentPositionSource.ORIGINAL_SEQUENCE);
			if (distanceFromBreakpoint > 0 && distanceFromBreakpoint < f.getSequence().length())
			{
				fragmentsAcrossBreakpoint++;
			}
		}
		System.out.printf("%d fragments straddle the deletion breakpoint%n",
			fragmentsAcrossBreakpoint);

		Map<String, AlignmentResults> m_ep = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
		m.put(0, m_ep);
		for (int run = 0; run < EVAL_RUN_COUNT; run++)
		{
			List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

			Options o = new Options(paired_end, dRepeatCount);
			o.penalize_duplicate_mappings = false;
			alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-R-" + run,
				PHRED_THRESHOLDS, o, m_ep));
			alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast-S-" + run,
				PHRED_THRESHOLDS, new Options(paired_end, dRepeatCount), m_ep));
			o = new Options(paired_end, dRepeatCount);
			o.penalize_duplicate_mappings = false;
			alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-R-" + run,
				PHRED_THRESHOLDS, o, m_ep));
			alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast-S-" + run,
				PHRED_THRESHOLDS, new Options(paired_end, dRepeatCount), m_ep));
			alignmentInterfaceList.add(new SoapInterface(++index, "SOAP-" + run, PHRED_THRESHOLDS,
				new Options(paired_end, dRepeatCount), m_ep));
			alignmentInterfaceList.add(new BwaInterface(++index, "BWA-" + run, PHRED_THRESHOLDS,
				new Options(paired_end, dRepeatCount), m_ep));
			alignmentInterfaceList.add(new BowtieInterface(++index, "Bowtie-" + run,
				PHRED_THRESHOLDS, new Options(paired_end, dRepeatCount), m_ep));

			for (AlignmentToolInterface ati : alignmentInterfaceList)
			{
				File tool_path = new File(DATA_PATH, String.format("%03d-%s-%s", ati.index,
					ati.description, "tandem"));
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

				System.out.printf("*** %03d %s: %f%n", ati.index, ati.description, ati.o.error_rate);

				atiList.add(ati);
			}
		}
		for (AlignmentToolInterface ati : atiList)
		{
			futureList.add(pool.submit(ati));
		}
		System.out.printf("Running %d total tool evaluations%n", futureList.size());

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

		String filename = String.format("%s.csv", testDescription);
		String roc_filename = String.format("%s_roc.csv", testDescription);
		String extra_filename = String.format("%s_extra.csv", testDescription);
		try
		{
			System.out.printf("Writing results to %s%n", filename);
			FileWriter w = new FileWriter(new File(DATA_PATH, filename));
			w.write(String.format("%s,%s,%s,%s,%s,%s,%s%n", "Tool", "GenomeRepeatCount",
				"Threshold", "Precision", "Recall", "Time", "UsedReadRatio"));
			for (int repeatCount : m.keySet())
			{
				for (String toolName : m.get(repeatCount).keySet())
				{
					for (Integer threshold : PHRED_THRESHOLDS)
					{
						AlignmentResults ar = m.get(repeatCount).get(toolName);
						FilteredAlignmentResults r = ar.filter(threshold);
						w.write(String.format("%s,%d,%d,%f,%f,%d,%f%n", toolName, repeatCount,
							threshold, r.getPrecision(), r.getRecall(),
							ar.timeMap.get(AlignmentOperation.TOTAL), r.getUsedReadRatio()));
					}
				}
			}
			w.close();

			System.out.printf("Writing overall ROC data to %s%n", roc_filename);
			FileWriter e = new FileWriter(new File(DATA_PATH, extra_filename));
			e.write(String.format("#Fragments straddling breakpoint: %d%n",
				fragmentsAcrossBreakpoint));
			e.write(String.format("#Breakpoint position: %d%n", sfsd.getDeletePosition()));
			e.write("ToolName,UnmappedFragments\n");
			w = new FileWriter(new File(DATA_PATH, roc_filename));
			w.write(String.format("%s,%s,%s,%s%n", "Tool", "IndelFrequency", "Score", "Label"));
			for (int repeatCount : m.keySet())
			{
				for (String toolName : m.get(repeatCount).keySet())
				{
					// TODO: Don't duplicate code here
					AlignmentResults r = m.get(repeatCount).get(toolName);
					e.write(String.format("%s,%d%n", toolName, r.missingFragments));
					for (Map.Entry<Integer, Integer> p : r.positives.entrySet())
					{
						int score = p.getKey();
						int count = p.getValue();
						for (int i = 0; i < count; i++)
						{
							w.write(String.format("%s,%f,%d,%d%n", toolName, repeatCount, score, 1));
						}
					}
					for (Map.Entry<Integer, Integer> n : r.negatives.entrySet())
					{
						int score = n.getKey();
						int count = n.getValue();
						for (int i = 0; i < count; i++)
						{
							w.write(String.format("%s,%f,%d,%d%n", toolName, repeatCount, score, 0));
						}
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * XXX Fix this after recent memory-reducing changes
	 * 
	 * @param paired_end
	 * @param genome
	 */
	public void runtimeCoverageEvaluation()
	{
		final String testDescription = "runtime_coverage";
		List<Map<Double, Map<String, AlignmentResults>>> l = new ArrayList<Map<Double, Map<String, AlignmentResults>>>(
			EVAL_RUN_COUNT);
		final boolean paired_end = false;
		final double errorProbability = 0.05;

		SequenceGenerator g = new SeqGenSingleSequenceMultipleRepeats();
		SequenceGenerator.Options sgo = new SequenceGenerator.Options();
		sgo.length = 500000;
		System.out.print("Generating sequence...");
		CharSequence sequence = g.generateSequence(sgo);
		System.out.println("done.");
		System.out.printf("Genome length: %d%n", sequence.length());

		final File genomeFile = new File(DATA_PATH, "runtime_cov.fa");
		writeGenome(sequence, genomeFile);

		DATA_PATH.mkdirs();
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			RUNTIME_COVERAGES.size() * EVAL_RUN_COUNT * 7);
		int index = 0;
		for (int which_run = 0; which_run < EVAL_RUN_COUNT; which_run++)
		{
			Map<Double, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Double, Map<String, AlignmentResults>>());
			l.add(m);

			for (double coverage : RUNTIME_COVERAGES)
			{
				Map<String, AlignmentResults> m_c = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
				m.put(coverage, m_c);
				Fragmentizer.Options fo = new Fragmentizer.Options();
				fo.fragmentLength = 50;
				/*
				 * Integer truncation is exactly what we want here
				 */
				fo.fragmentCount = (int) (coverage * sequence.length()) / fo.fragmentLength;
				fo.fragmentLengthSd = 1;

				// TODO: write fragments with fragmentizeToFile
				System.out.printf("Reading %d fragments...", fo.fragmentCount);
				List<? extends Fragment> list = Fragmentizer.fragmentize(sequence, fo);
				System.out.println("done.");

				System.out.print("Introducing fragment read errors...");
				UniformErrorGenerator eg = new UniformErrorGenerator(SequenceGenerator.NUCLEOTIDES,
					errorProbability);
				List<? extends Fragment> errored_list = eg.generateErrors(list);
				System.out.println("done.");

				List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

				alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast",
					RUNTIME_THRESHOLDS, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast",
					RUNTIME_THRESHOLDS, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new SoapInterface(++index, "SOAP", RUNTIME_THRESHOLDS,
					new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new BwaInterface(++index, "BWA", RUNTIME_THRESHOLDS,
					new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP",
					RUNTIME_THRESHOLDS, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new BowtieInterface(++index, "Bowtie",
					RUNTIME_THRESHOLDS, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new NovoalignInterface(++index, "Novoalign",
					RUNTIME_THRESHOLDS, new Options(paired_end, errorProbability), m_c));

				for (AlignmentToolInterface ati : alignmentInterfaceList)
				{
					File tool_path = new File(DATA_PATH, String.format("%03d-%s", ati.index,
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
		SimulationParameters pa = new SimulationParameters(RUNTIME_COVERAGES, false,
			testDescription, Genome.RUNTIME_COV_RANDOM, genomeFile, null);
		writeRuntimeResults(pa, l, "Coverage");
	}

	/**
	 * XXX Fix this after recent memory-reducing changes
	 * 
	 * @param paired_end
	 * @param genome
	 */
	public void runtimeGenomeSizeEvaluation()
	{
		final String testDescription = "runtime_genome_size";
		List<Map<Double, Map<String, AlignmentResults>>> l = new ArrayList<Map<Double, Map<String, AlignmentResults>>>(
			EVAL_RUN_COUNT);
		final boolean paired_end = false;
		final double errorProbability = 0.05;
		final int coverage = 3;

		DATA_PATH.mkdirs();
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			RUNTIME_COVERAGES.size() * EVAL_RUN_COUNT * 7);
		int index = 0;
		for (int which_run = 0; which_run < EVAL_RUN_COUNT; which_run++)
		{
			Map<Double, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Double, Map<String, AlignmentResults>>());
			l.add(m);

			for (double genome_size : RUNTIME_GENOME_SIZES)
			{
				SequenceGenerator g = new SeqGenSingleSequenceMultipleRepeats();
				SequenceGenerator.Options sgo = new SequenceGenerator.Options();
				sgo.length = (int) genome_size;
				System.out.print("Generating sequence...");
				CharSequence sequence = g.generateSequence(sgo);
				System.out.println("done.");
				System.out.printf("Genome length: %d%n", sequence.length());

				File genomeFile = new File(DATA_PATH, "runtime_size.fa");
				writeGenome(sequence, genomeFile);

				Map<String, AlignmentResults> m_c = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
				m.put(genome_size, m_c);
				Fragmentizer.Options fo = new Fragmentizer.Options();
				fo.fragmentLength = 50;
				/*
				 * Integer truncation is okay here
				 */
				fo.fragmentCount = (int) (genome_size * coverage) / fo.fragmentLength;
				fo.fragmentLengthSd = 1;

				System.out.printf("Reading %d fragments...", fo.fragmentCount);
				List<? extends Fragment> list = Fragmentizer.fragmentize(sequence, fo);
				System.out.println("done.");

				System.out.print("Introducing fragment read errors...");
				UniformErrorGenerator eg = new UniformErrorGenerator(SequenceGenerator.NUCLEOTIDES,
					errorProbability);
				List<? extends Fragment> errored_list = eg.generateErrors(list);
				System.out.println("done.");

				List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

				alignmentInterfaceList.add(new MrFastInterface(++index, "MrFast",
					RUNTIME_THRESHOLDS, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new MrsFastInterface(++index, "MrsFast",
					RUNTIME_THRESHOLDS, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new SoapInterface(++index, "SOAP", RUNTIME_THRESHOLDS,
					new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new BwaInterface(++index, "BWA", RUNTIME_THRESHOLDS,
					new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new ShrimpInterface(++index, "SHRiMP",
					RUNTIME_THRESHOLDS, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new BowtieInterface(++index, "Bowtie",
					RUNTIME_THRESHOLDS, new Options(paired_end, errorProbability), m_c));
				alignmentInterfaceList.add(new NovoalignInterface(++index, "Novoalign",
					RUNTIME_THRESHOLDS, new Options(paired_end, errorProbability), m_c));

				for (AlignmentToolInterface ati : alignmentInterfaceList)
				{
					File tool_path = new File(DATA_PATH, String.format("%03d-%s", ati.index,
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
		SimulationParameters pa = new SimulationParameters(RUNTIME_GENOME_SIZES, false,
			testDescription, Genome.RUNTIME_COV_RANDOM, null, null);
		writeRuntimeResults(pa, l, "GenomeSize");
	}

	/**
	 * TODO: Don't duplicate code, and make this method a lot less stupid. XXX:
	 * Fix this after recent memory reduction changes
	 */
	public void tandemRepeatEvaluation(String[] args)
	{
		final int requiredArgumentCount = 11;
		if (args.length < requiredArgumentCount)
		{
			System.err.printf("*** Usage: %s [action] genomeFilenameOrSize %n",
				AlignmentToolService.class.getCanonicalName());
			System.err.println("\trepeatPositionsFilename tandemRepeatCount tandemRepeatSize");
			System.err.println("\tfragmentLengthMean fragmentLengthSd readLengthMean readLengthSd");
			System.err.println("\tcoverage baseCallErrorProbability");
			System.err.println("Defined actions: read generate");
			System.exit(1);
		}
		String filename = null;
		// TODO: rename this
		boolean generate = false;
		int genome_size = 0;
		if (args[0].equals("read"))
		{
			filename = args[1];
		}
		else
		{
			generate = true;
			genome_size = Integer.parseInt(args[1]);
		}
		final String repeatPositionsFilename = args[2];
		final int tandemRepeatCount = Integer.parseInt(args[3]);
		final int tandemRepeatLength = Integer.parseInt(args[4]);
		final int fragmentLengthMean = Integer.parseInt(args[5]);
		final double fragmentLengthSd = Double.parseDouble(args[6]);
		final int readLengthMean = Integer.parseInt(args[7]);
		final double readLengthSd = Double.parseDouble(args[8]);
		final int coverage = Integer.parseInt(args[9]);
		final double baseCallErrorProbability = Double.parseDouble(args[10]);

		final String testDescription = "tandem_repeat";
		List<Map<Integer, Map<String, AlignmentResults>>> l = new ArrayList<Map<Integer, Map<String, AlignmentResults>>>(
			EVAL_RUN_COUNT);
		final boolean paired_end = true;
		// final double errorProbability = 0.05;

		DATA_PATH.mkdirs();
		List<Future<AlignmentResults>> futureList = new ArrayList<Future<AlignmentResults>>(
			RUNTIME_COVERAGES.size() * EVAL_RUN_COUNT * 7);
		int index = 0;
		for (int which_run = 0; which_run < EVAL_RUN_COUNT; which_run++)
		{
			Map<Integer, Map<String, AlignmentResults>> m = Collections.synchronizedMap(new TreeMap<Integer, Map<String, AlignmentResults>>());
			l.add(m);

			CharSequence sequence = null;
			if (generate)
			{
				System.out.print("Generating sequence...");
				sequence = SequenceGenerator.generateSequence(SequenceGenerator.NUCLEOTIDES,
					genome_size);
				System.out.println("done.");
			}
			else
			{
				try
				{
					File f = new File(filename);
					// System.err.println(f.getAbsolutePath());
					sequence = FastaReader.getSequence(f);
					// TODO: Fix this
					genome_size = sequence.length();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
			System.out.printf("Original genome length: %d%n", sequence.length());

			// Insert some repeats
			// g.setVerboseOutput(true);
			SeqFilterTandemRepeats.Options sgo = new SeqFilterTandemRepeats.Options();
			sgo.repeatCount = tandemRepeatCount;
			sgo.repeatLength = tandemRepeatLength;
			SeqFilterTandemRepeats g = new SeqFilterTandemRepeats(sgo);
			CharSequence repeated = g.filter(sequence);

			Map<String, AlignmentResults> m_c = Collections.synchronizedMap(new TreeMap<String, AlignmentResults>());
			m.put(genome_size, m_c);
			Fragmentizer.Options fo = new Fragmentizer.Options();
			fo.fragmentLength = fragmentLengthMean;
			fo.fragmentLengthSd = fragmentLengthSd;
			fo.readLength = readLengthMean;
			fo.readLengthSd = readLengthSd;
			/*
			 * Integer truncation is okay here
			 */
			fo.fragmentCount = (genome_size * coverage) / (2 * fo.readLength);

			System.out.printf("Reading %d fragments...", fo.fragmentCount);
			List<? extends Fragment> list = Fragmentizer.fragmentize(repeated, fo);
			System.out.println("done.");

			if (baseCallErrorProbability > 0.0)
			{
				System.out.print("Introducing fragment read errors...");
				UniformErrorGenerator eg = new UniformErrorGenerator(SequenceGenerator.NUCLEOTIDES,
					baseCallErrorProbability);
				list = eg.generateErrors(list);
				System.out.println("done.");
			}

			List<AlignmentToolInterface> alignmentInterfaceList = new ArrayList<AlignmentToolInterface>();

			Options o = new Options(paired_end, 0.0);
			o.readLength = readLengthMean;
			o.readLengthSd = readLengthSd;
			alignmentInterfaceList.add(new BwaInterface(++index, "BWA", RUNTIME_THRESHOLDS, o, m_c));

			for (AlignmentToolInterface ati : alignmentInterfaceList)
			{
				File tool_path = new File(DATA_PATH, String.format("%03d-%s", ati.index,
					ati.description));
				tool_path.mkdirs();

				// TODO: move this
				File repeatPositionsFile = new File(DATA_PATH, repeatPositionsFilename);
				try
				{
					BufferedWriter w = new BufferedWriter(new FileWriter(repeatPositionsFile));
					w.write("#position,length\n");
					for (SeqFilterTandemRepeats.TandemRepeatDescriptor t : g.getRepeats())
					{
						w.write(String.format("%d,%d%n", t.position, t.length));
					}
					// TODO: move this into a 'finally' block
					w.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

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

				System.out.printf("*** %03d %s: %d%n", ati.index, ati.description, genome_size);

				futureList.add(pool.submit(ati));
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

		/*
		 * Wow, I'd really like something along the lines of Python's "csv"
		 * module right now
		 */
		String results_filename = testDescription + "_data.csv";
		for (Map<Integer, Map<String, AlignmentResults>> m : l)
		{
			System.out.printf("Writing results to %s%n", results_filename);
			try
			{
				FileWriter w = new FileWriter(new File(DATA_PATH, results_filename));
				w.write(String.format("%s,%s,%s,%s,%s,%s,%s%n", "Tool", "ErrorRate", "Threshold",
					"Precision", "Recall", "Time", "UsedReadRatio"));
				for (Integer d : m.keySet())
				{
					for (String s : m.get(d).keySet())
					{
						for (Integer i : PHRED_THRESHOLDS)
						{
							AlignmentResults ar = m.get(d).get(s);
							FilteredAlignmentResults r = ar.filter(i);
							w.write(String.format("%s,%f,%d,%f,%f,%d,%f%n", s,
								baseCallErrorProbability, i, r.getPrecision(), r.getRecall(),
								ar.timeMap.get(AlignmentOperation.TOTAL), r.getUsedReadRatio()));
						}
					}
				}
				w.close();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args)
	{
		new AlignmentToolService().errorRateEvaluation(false, Genome.RANDOM_HARD);
		new AlignmentToolService().indelSizeEvaluation(false, Genome.RANDOM_HARD);
		new AlignmentToolService().indelFrequencyEvaluation(false, Genome.RANDOM_HARD);
	}
}
