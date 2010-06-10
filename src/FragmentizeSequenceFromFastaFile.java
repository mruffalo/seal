import java.io.*;
import java.util.*;
import assembly.*;
import generator.*;

public class FragmentizeSequenceFromFastaFile
{
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args.length < 5)
		{
			System.err.printf(
				"Usage: %s InputFastaFile OutputFastaFile%n\tSequenceLength%n\tNumberOfRepeats%n\tRepeatLength%n",
				FragmentizeSequenceFromFastaFile.class.getCanonicalName());
			return;
		}
		int n = Integer.parseInt(args[2]);
		int k = Integer.parseInt(args[3]);
		int kTolerance = Integer.parseInt(args[4]);
		String string = null;
		try
		{
			string = FastaHandler.getSequence(new File(args[0]));
			List<Fragment> fragments = Fragmentizer.fragmentizeForShotgun(string, n, k, kTolerance);
			FastaHandler.writeFragments(fragments, new File(args[1]));
		}
		catch (IOException e)
		{
			System.err.println("Caught IOException:");
			e.printStackTrace();
		}
	}
}
