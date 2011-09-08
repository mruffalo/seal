package external;

import com.beust.jcommander.Parameter;

/**
 * Encapsulates common information in ErrorRateEvaluation, etc.
 */
public class EvaluationProgram
{
	@Parameter(names = {"-h", "--help"}, hidden = true)
	protected boolean showHelp = false;
}
