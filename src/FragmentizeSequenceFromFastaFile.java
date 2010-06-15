import java.io.*;
import java.util.*;
import utils.FastaHandler;
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
				"Usage: %s InputFastaFile OutputFastaFile%n\tNumberOfFragments%n\tFragmentSize%n\tFragmentSizeVariance%n",
				FragmentizeSequenceFromFastaFile.class.getCanonicalName());
			return;
		}
		Fragmentizer.Options options = new Fragmentizer.Options();
		options.n = Integer.parseInt(args[2]);
		options.k = Integer.parseInt(args[3]);
		options.ksd = Integer.parseInt(args[4]);
		String string = null;
		try
		{
			string = FastaHandler.getSequence(new File(args[0]));
			List<Fragment> fragments = Fragmentizer.fragmentizeForShotgun(string, options);
			FastaHandler.writeFragments(fragments, new File(args[1]));
		}
		catch (IOException e)
		{
			System.err.println("Caught IOException:");
			e.printStackTrace();
		}
	}
}
