package external;

import com.beust.jcommander.Parameter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates common information in ErrorRateEvaluation, etc.
 */
public class EvaluationProgram
{
	@Parameter(names = {"-h", "--help"}, hidden = true)
	protected boolean showHelp = false;

	@Parameter(names = {"--tools"})
	protected String toolNames = "Bowtie,BWA,GSNAP,mrFAST,mrsFAST,Novoalign,SHRiMP,SOAP";

	protected List<String> getToolNames()
	{
		return Arrays.asList(toolNames.split(","));
	}
}
