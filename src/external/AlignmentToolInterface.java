package external;

import io.FastaWriter;
import io.FastqWriter;
import io.SamReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import assembly.Fragment;

public abstract class AlignmentToolInterface implements Callable<AlignmentResults>
{
	protected final List<Integer> thresholds;
	protected CharSequence sequence;
	protected List<? extends Fragment> fragments;
	protected List<List<? extends Fragment>> pairedEndFragments;
	protected Options o;
	/**
	 * Not a Set of Fragments since we're getting this from the output of the
	 * alignment tool instead of the internal data structures. There's no reason
	 * to build Fragments out of the data that we read.
	 */
	protected Set<String> correctlyMappedFragments;
	protected Set<String> totalMappedFragments;

	protected Map<String, AlignmentResults> m;

	public final int index;
	protected final String description;

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

		public Options(boolean is_paired_end_, double error_probability_)
		{
			is_paired_end = is_paired_end_;
			error_probability = error_probability_;
		}

		public final boolean is_paired_end;
		public final double error_probability;
		public File genome;
		public File binary_genome;
		public List<Reads> reads = new ArrayList<Reads>(2);
		public File raw_output;
		public File sam_output;
		public File index;
		/**
		 * Produced by this code, not the alignment tool
		 */
		public File converted_output;
		public File unmapped_output;
		public File roc_output;
		public boolean penalize_duplicate_mappings = true;
	}

	public AlignmentToolInterface(int index_, String description_, List<Integer> thresholds_,
		CharSequence sequence_, List<? extends Fragment> list_, Options o_,
		Map<String, AlignmentResults> m_)
	{
		index = index_;
		description = description_;
		thresholds = thresholds_;
		sequence = sequence_;
		fragments = list_;
		o = o_;
		m = m_;
		totalMappedFragments = new HashSet<String>(fragments.size());
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

	public enum AlignmentOperation
	{
		PREPROCESSING,
		ALIGNMENT,
		POSTPROCESSING,
		TOTAL,
	}

	public void writeGenome()
	{
		try
		{
			FastaWriter.writeSequence(sequence, o.genome);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void writeFragments()
	{
		for (int i = 0; i < o.reads.size(); i++)
		{
			try
			{
				FastqWriter.writeFragments(pairedEndFragments.get(i), o.reads.get(i).reads,
					o.reads.get(i).index);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void commonPreAlignmentProcessing()
	{
		writeGenome();
		writeFragments();
	}

	public abstract void preAlignmentProcessing();

	public abstract void align();

	public abstract void postAlignmentProcessing();

	/**
	 * Default implementation reads SAM output. Can be overridden if the tool
	 * has a different output format.
	 * 
	 * @param qualityThreshold
	 * @return
	 */
	public AlignmentResults readAlignment()
	{
		return SamReader.readAlignment(index, o, fragments.size(), correctlyMappedFragments);
	}

	public void writeRocData(AlignmentResults r)
	{
		System.out.printf("%03d: %s%n", index, String.format("Writing ROC data to %s...%n",
			o.roc_output.getAbsolutePath()));
		FileWriter w;
		try
		{
			w = new FileWriter(o.roc_output);
			w.write(String.format("predictions,labels%n"));
			for (int p : r.positives)
			{
				w.write(String.format("%d,%d%n", p, 1));
			}
			for (int n : r.negatives)
			{
				w.write(String.format("%d,%d%n", n, 0));
			}
			w.close();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void cleanup()
	{
		o.binary_genome.delete();
		for (Options.Reads r : o.reads)
		{
			r.aligned_reads.delete();
			r.binary_reads.delete();
			r.reads.delete();
		}
		o.raw_output.delete();
		o.sam_output.delete();
		if (o.unmapped_output != null)
		{
			o.unmapped_output.delete();
		}
		if (o.index != null)
		{
			o.index.delete();
		}
	}

	@Override
	public AlignmentResults call() throws Exception
	{
		Map<AlignmentOperation, Long> timeMap = new EnumMap<AlignmentOperation, Long>(
			AlignmentOperation.class);
		long start, preprocessing, alignment, postprocessing;
		commonPreAlignmentProcessing();
		start = System.nanoTime();
		preAlignmentProcessing();
		preprocessing = System.nanoTime();
		align();
		alignment = System.nanoTime();
		postAlignmentProcessing();
		postprocessing = System.nanoTime();

		timeMap.put(AlignmentOperation.PREPROCESSING, preprocessing - start);
		timeMap.put(AlignmentOperation.ALIGNMENT, alignment - preprocessing);
		timeMap.put(AlignmentOperation.POSTPROCESSING, postprocessing - alignment);
		timeMap.put(AlignmentOperation.TOTAL, postprocessing - start);

		AlignmentResults results = readAlignment();
		results.timeMap = timeMap;

		writeRocData(results);

		m.put(description, results);
		cleanup();
		return results;
	}
}
