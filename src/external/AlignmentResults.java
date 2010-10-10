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
}
