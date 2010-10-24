package external;

/**
 * Represents an {@link AlignmentResults} that has been filtered by a mapping
 * quality threshold. This threshold allows true positives, false positives and
 * false negatives to be calculated.
 * 
 * @author mruffalo
 */
public class FilteredAlignmentResults
{
	public FilteredAlignmentResults(int tp, int fp, int fn)
	{
		truePositives = tp;
		falsePositives = fp;
		falseNegatives = fn;
	}

	/**
	 * Number of fragments that are mapped to the correct location in the target
	 * genome and pass the quality threshold
	 */
	public final int truePositives;
	/**
	 * Number of fragments that are mapped to an incorrect location in the
	 * target genome and pass the quality threshold
	 */
	public final int falsePositives;
	/**
	 * Number fo fragments that are:
	 * <ul>
	 * <li>Mapped to correct location in target genome and do not pass quality
	 * threshold</li>
	 * <li>or not present at all in tool output</li>
	 * </ul>
	 */
	public final int falseNegatives;

	/**
	 * @return The precision score described by this object: TP / (TP + FP)
	 */
	public double getPrecision()
	{
		return (double) truePositives / (double) (truePositives + falsePositives);
	}

	/**
	 * @return The recall score described by this object: TP / (TP + FN)
	 */
	public double getRecall()
	{
		return (double) truePositives / (double) (truePositives + falseNegatives);
	}
}
