/**
 * 
 */
package external;

import java.util.Map;
import external.AlignmentToolInterface.AlignmentOperation;

public class AlignmentResults
{
	/**
	 * Mapped to correct location in target genome, and passes quality threshold
	 */
	public int truePositives;
	/**
	 * Mapped to incorrect location in target genome, and passes quality
	 * threshold
	 */
	public int falsePositives;
	/**
	 * <ul>
	 * <li>Mapped to correct location in target genome and does not pass quality
	 * threshold</li>
	 * <li>or not present at all in tool output</li>
	 * </ul>
	 */
	public int falseNegatives;
	/**
	 * Stores time for each operation
	 */
	public Map<AlignmentOperation, Long> timeMap;

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
