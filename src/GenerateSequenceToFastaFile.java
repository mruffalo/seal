import io.FastaWriter;
import java.io.*;
import generator.*;

public class GenerateSequenceToFastaFile
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args.length < 4)
		{
			System.err.printf(
				"Usage: %s OutputFastaFile%n\tSequenceLength%n\tNumberOfRepeats%n\tRepeatLength%n",
				GenerateSequenceToFastaFile.class.getCanonicalName());
			return;
		}
		int m = Integer.parseInt(args[1]);
		int r = Integer.parseInt(args[2]);
		int l = Integer.parseInt(args[3]);
		SequenceGenerator sg = new SeqGenSingleSequenceMultipleRepeats();
		CharSequence string = sg.generateSequence(m, r, l);
		try
		{
			FastaWriter.writeSequence(string, new File(args[0]));
		}
		catch (IOException e)
		{
			System.err.println("Caught IOException:");
			e.printStackTrace();
		}
	}
}
