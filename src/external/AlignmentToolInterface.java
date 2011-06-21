package external;

import io.SamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public abstract class AlignmentToolInterface implements Callable<AlignmentResults>
{
	protected final List<Integer> thresholds;
	protected Options o;
	/**
	 * Not a Set of Fragments since we're getting this from the output of the
	 * alignment tool instead of the internal data structures. There's no reason
	 * to build Fragments out of the data that we read.
	 */
	protected Set<String> correctlyMappedFragments;
	/**
	 * Used to instantiate various internal data structures to appropriate
	 * capacities
	 */
	protected int fragmentCount;
	protected Set<String> totalMappedFragments;

	protected Map<String, AlignmentResults> m;

	public final int index;
	protected final String description;

	private static final String LINK_COMMAND = "ln";
	private static final String LINK_ARGUMENT_FORCE = "-f";

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
			/**
			 * The original reads that will be linked into this tool's directory
			 */
			public File orig_reads;
			/**
			 * The read file that is used by this tool
			 */
			public File reads;
			/**
			 * Not used for every tool
			 */
			public File binary_reads;
			public File aligned_reads;
		}

		public Options(boolean is_paired_end_, double error_rate_)
		{
			is_paired_end = is_paired_end_;
			error_rate = error_rate_;
		}

		public final boolean is_paired_end;
		/**
		 * This might be the base call error rate, or the indel size or indel
		 * frequency. Always specified as a double even though indel size is an
		 * integer -- don't want any NumberFormatExceptions while printing this
		 */
		public final double error_rate;
		/**
		 * This is the original genome file that is hardlinked to each tool's
		 * directory
		 */
		public File orig_genome;
		/**
		 * This is the genome file that is used by each tool
		 */
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

		/**
		 * Only used for paired-end. XXX Move this
		 */
		public int readLength;
		/**
		 * Only used for paired-end. XXX move this
		 */
		public double readLengthSd;
	}

	public AlignmentToolInterface(int index_, String description_, List<Integer> thresholds_,
		Options o_, Map<String, AlignmentResults> m_)
	{
		index = index_;
		description = description_;
		thresholds = thresholds_;
		o = o_;
		m = m_;
	}

	public enum AlignmentOperation
	{
		PREPROCESSING,
		ALIGNMENT,
		POSTPROCESSING,
		TOTAL,
	}

	public void linkGenome()
	{
		ProcessBuilder pb = null;
		System.out.printf("%03d: Linking%n", index);
		System.out.printf("%03d:   %s to%n", index, o.orig_genome.getAbsolutePath());
		System.out.printf("%03d:   %s%n", index, o.genome.getAbsolutePath());
		pb = new ProcessBuilder(LINK_COMMAND, LINK_ARGUMENT_FORCE, o.orig_genome.getAbsolutePath(),
			o.genome.getAbsolutePath());
		pb.directory(o.genome.getParentFile());
		try
		{
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
			{
				System.out.printf("%03d: %s%n", index, line);
			}
			while ((line = stderr.readLine()) != null)
			{
				System.err.printf("%03d: %s%n", index, line);
			}
			p.waitFor();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public void linkFragments()
	{
		for (int i = 0; i < o.reads.size(); i++)
		{
			Options.Reads r = o.reads.get(i);
			System.out.printf("%03d: Linking%n", index);
			System.out.printf("%03d:   %s to%n", index, r.orig_reads.getAbsolutePath());
			System.out.printf("%03d:   %s%n", index, r.reads.getAbsolutePath());
			ProcessBuilder pb = new ProcessBuilder(LINK_COMMAND, LINK_ARGUMENT_FORCE,
				r.orig_reads.getAbsolutePath(), r.reads.getAbsolutePath());
			pb.directory(o.genome.getParentFile());
			try
			{
				Process p = pb.start();
				BufferedReader stdout = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
				BufferedReader stderr = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));
				String line = null;
				while ((line = stdout.readLine()) != null)
				{
					System.out.printf("%03d: %s%n", index, line);
				}
				while ((line = stderr.readLine()) != null)
				{
					System.err.printf("%03d: %s%n", index, line);
				}
				p.waitFor();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void commonPreAlignmentProcessing()
	{
		linkGenome();
		linkFragments();
	}

	public abstract void preAlignmentProcessing();

	public abstract void align();

	public abstract void postAlignmentProcessing();

	/**
	 * Default implementation reads SAM output. Can be overridden if the tool
	 * has a different output format.
	 *
	 * @return results
	 */
	public AlignmentResults readAlignment()
	{
		return SamReader.readAlignment(index, o, fragmentCount, correctlyMappedFragments);
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
			for (Map.Entry<Integer, Integer> p : r.positives.entrySet())
			{
				int score = p.getKey();
				int count = p.getValue();
				for (int i = 0; i < count; i++)
				{
					w.write(String.format("%d,%d%n", score, 1));
				}
			}
			for (Map.Entry<Integer, Integer> n : r.negatives.entrySet())
			{
				int score = n.getKey();
				int count = n.getValue();
				for (int i = 0; i < count; i++)
				{
					w.write(String.format("%d,%d%n", score, 0));
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

	public void cleanup()
	{
		/*
		 * o.binary_genome.delete(); for (Options.Reads r : o.reads) {
		 * r.aligned_reads.delete(); r.binary_reads.delete(); r.reads.delete();
		 * } o.raw_output.delete(); o.sam_output.delete(); if (o.unmapped_output
		 * != null) { o.unmapped_output.delete(); }
		 */
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
