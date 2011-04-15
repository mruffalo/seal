/**
 * 
 */
package external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import external.AlignmentToolInterface.AlignmentOperation;

public class AlignmentResults
{
	public AlignmentResults()
	{
		positives = new ArrayList<Integer>();
		negatives = new ArrayList<Integer>();
	}

	public AlignmentResults(int size)
	{
		positives = new ArrayList<Integer>(size);
		negatives = new ArrayList<Integer>(size);
	}

	/**
	 * Quality values of alignments that are at the correct location in the
	 * genome
	 */
	public List<Integer> positives;
	/**
	 * Quality values of alignments that are at an incorrect location in the
	 * genome.
	 */
	public List<Integer> negatives;
	/**
	 * Number of fragments that were missing from the tool's output. Always
	 * added to false negative counts.
	 */
	public int missingFragments = 0;
	/**
	 * Stores time for each operation
	 */
	public Map<AlignmentOperation, Long> timeMap;

	public FilteredAlignmentResults filter(int threshold)
	{
		int tp = 0;
		int fp = 0;
		int fn = missingFragments;
		for (Integer p : positives)
		{
			if (p >= threshold)
			{
				tp++;
			}
			else
			{
				fn++;
			}
		}
		for (Integer n : negatives)
		{
			if (n >= threshold)
			{
				fp++;
			}
		}
		return new FilteredAlignmentResults(tp, fp, fn);
	}

}
