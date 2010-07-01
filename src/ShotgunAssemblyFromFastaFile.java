import generator.Fragmentizer;
import io.FastaReader;
import io.FastaWriter;
import java.io.*;
import java.util.*;
import utils.LicenseUtil;
import assembly.*;

public class ShotgunAssemblyFromFastaFile
{
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		LicenseUtil.printLicense();
		if (args.length < 2)
		{
			System.err.printf("Usage: %s FastaFileContainingFragments OutputFastaFile%n",
				ShotgunAssemblyFromFastaFile.class.getCanonicalName());
			return;
		}
		try
		{
			List<Fragment> fragments = Fragmentizer.removeSubstrings(FastaReader.getFragments(new File(args[0])));
			System.out.printf("Fragments read from FASTA file at %s%n", args[0]);
			SequenceAssembler sa = new ShotgunSequenceAssembler();
			long begin = System.nanoTime();
			String assembled = sa.assembleSequence(fragments);
			long end = System.nanoTime();
			FastaWriter.writeSequence(assembled, new File(args[1]));
			System.out.printf("Assembled sequence (length %d) written to %s%n", assembled.length(), args[1]);
			System.out.printf("Total time: %dns%n", end - begin);
		}
		catch (IOException e)
		{
			System.err.println("Caught IOException:");
			e.printStackTrace();
		}
	}
}
