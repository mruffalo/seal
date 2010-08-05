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
		SequenceGenerator.Options o = new SequenceGenerator.Options();
		o.length = Integer.parseInt(args[1]);
		o.repeatCount = Integer.parseInt(args[2]);
		o.repeatLength = Integer.parseInt(args[3]);
		SequenceGenerator sg = new SeqGenSingleSequenceMultipleRepeats();
		CharSequence string = sg.generateSequence(o);
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
