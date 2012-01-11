package generator.errors;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a FASTQ file's base qualities as the error model
 */
public class RealDataErrorGenerator extends SubstitutionErrorGenerator
{
	/**
	 * @param allowedCharacters
	 * @param fastqFile		 FASTQ file to sample base qualities from
	 */
	public RealDataErrorGenerator(String allowedCharacters, File fastqFile)
	{
		super(allowedCharacters);
	}

	/**
	 * @param allowedCharacters
	 * @param fastqFile		 FASTQ file to sample base qualities from
	 * @param limit			 The maximum number of base quality strings to read from the FASTQ file
	 */
	public RealDataErrorGenerator(String allowedCharacters, File fastqFile, int limit)
	{
		super(allowedCharacters);
	}

	private static int[] decodeQualityString(String string, int offset)
	{
		// TODO this
		return new int[]{};
	}
	
	/**
	 * TODO: refactor this; probably move it into FastqReader
	 *
	 * @param file
	 * @return
	 */
	private static List<int[]> readFastqFile(File file)
	{
		List<int[]> list = new ArrayList<int[]>();
		try
		{
			boolean isQualityString = false;
			BufferedReader r = new BufferedReader(new FileReader(file));
			String line;
			while ((line = r.readLine()) != null)
			{
				if (isQualityString) {
					list.add(decodeQualityString(line.trim()))
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return list;
	}

	@Override
	protected double getSubstitutionProbability(int position, int length)
	{
		return 0;
	}

	@Override
	public CharSequence generateErrors(CharSequence sequence)
	{
		return null;
	}
}
