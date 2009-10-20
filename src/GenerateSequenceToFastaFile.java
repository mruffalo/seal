import java.io.*;
import java.util.*;
import assembly.*;
import generator.*;

public class GenerateSequenceToFastaFile
{
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args.length < 7)
		{
			System.err.printf(
				"Usage: %s OutputFastaFile%n\tSequenceLength%n\tNumberOfRepeats%n\tRepeatLength%n\tNumberOfFragments%n\tFragmentSize%n\tFragmentSizeTolerance%n",
				GenerateSequenceToFastaFile.class.getCanonicalName());
			return;
		}
		int m = Integer.parseInt(args[1]);
		int r = Integer.parseInt(args[2]);
		int l = Integer.parseInt(args[3]);
		int n = Integer.parseInt(args[4]);
		int k = Integer.parseInt(args[5]);
		int kTolerance = Integer.parseInt(args[6]);
		SequenceGenerator sg = new SeqGenSingleSequenceMultipleRepeats();
		String string = sg.generateSequence(m, r, l);
		List<Fragment> fragments = Fragmentizer.fragmentizeForShotgun(string, n, k, kTolerance);
		try
		{
			FastaHandler.writeFragments(fragments, args[0]);
		}
		catch (IOException e)
		{
			System.err.println("Caught IOException:");
			e.printStackTrace();
		}
	}
}
