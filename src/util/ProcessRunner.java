package util;

import java.io.OutputStream;
import java.util.List;

/**
 * Encapsulates a lot of boilerplate code
 * 
 * @author mruffalo
 */
public class ProcessRunner
{
	/**
	 * Runs a command. Dumps stdout and stderr of the subprocess to stdout and
	 * stderr (respectively) of this Java process.
	 * 
	 * @param commands
	 */
	public static void run(List<String> commands)
	{
		run(commands, System.out, System.err);
	}

	public static void run(List<String> commands, OutputStream out)
	{
		run(commands, out, System.err);
	}

	public static void run(List<String> commands, OutputStream out, OutputStream err)
	{
	}
}
