import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import external.AlignmentResults;
import external.AlignmentToolService;
import external.EvaluationProgram;
import generator.Fragmentizer;
import generator.SequenceGenerator;
import generator.errors.FragmentErrorGenerator;
import generator.errors.IndelGenerator;
import util.DoubleCsvConverter;
import util.GenomeConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * TODO: Clean up parameter duplication between these standalone files
 */
public class IndelSizeEvaluation extends EvaluationProgram
{
	@Parameter(names = "--indel_sizes")
	/**
	 * This is a String because JCommander keeps wanting to <b>append</b> to a collection
	 * instead of replacing it with command line arguments.
	 */
	protected String indelSizes = "2,4,7,10,16";

	@Parameter(names = "--length", description = "Length of genome (if generated)")
	protected int generatedGenomeLength = AlignmentToolService.DEFAULT_GENERATED_GENOME_LENGTH;

	@Parameter(names = "--genome", description = "Genome to use; HUMAN and HUMAN_CHR22 require FASTA files to read",
			converter = GenomeConverter.class)
	protected AlignmentToolService.Genome genome = AlignmentToolService.Genome.RANDOM_HARD;

	@Parameter(names = "--fragment-length", description = "Fragment length (mean)")
	protected int fragmentLength = AlignmentToolService.DEFAULT_FRAGMENT_LENGTH_MEAN;

	@Parameter(names = "--fragment-length-sd", description = "Fragment length (std.dev.)")
	protected double fragmentLengthSd = AlignmentToolService.DEFAULT_FRAGMENT_LENGTH_SD;

	@Parameter(names = "--fragment-count", description = "Fragment count")
	protected int fragmentCount = AlignmentToolService.DEFAULT_FRAGMENT_COUNT;

	public void indelSizeEvaluation()
	{
		List<Double> indelSizeValues = new DoubleCsvConverter().convert(indelSizes);
		final String testDescription = "indel_size";

		final double indelLengthStdDev = 0.2;
		final double indelFrequency = 5e-2;
		Map<Double, List<FragmentErrorGenerator>> fegs = new TreeMap<Double, List<FragmentErrorGenerator>>();
		for (double indelSize : indelSizeValues)
		{
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
			fegs.put(indelSize, generatorList);
		}

		AlignmentToolService ats = new AlignmentToolService();
		Fragmentizer.Options fo = new Fragmentizer.Options();
		fo.fragmentLength = fragmentLength;
		fo.fragmentCount = fragmentCount;
		fo.fragmentLengthSd = fragmentLengthSd;
		AlignmentToolService.ProcessedGenome pg = ats.getGenomeAndFragmentFiles(genome, generatedGenomeLength, fo, fegs,
				testDescription, "Introducing fragment read errors for indel size %.0f ... ");
		AlignmentToolService.SimulationParameters pa =
				new AlignmentToolService.SimulationParameters(indelSizeValues, false,
						testDescription, genome, pg.file, pg.fragmentsByParameter);
		Map<Double, Map<String, AlignmentResults>> m = ats.runAccuracySimulation(pa);
		ats.writeAccuracyResults(pa, m, "IndelSize");
	}

	public static void main(String[] args)
	{
		IndelSizeEvaluation ise = new IndelSizeEvaluation();
		JCommander jc = new JCommander(ise, args);
		if (ise.showHelp)
		{
			jc.usage();
		}
		else
		{
			ise.indelSizeEvaluation();
		}
	}
}
