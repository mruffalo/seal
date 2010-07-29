package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import assembly.Fragment;
import assembly.FragmentPositionSource;

public class FastqWriter
{
	private static char getQualityCharacter(int offset, int value)
	{
		return Constants.FASTQ_QUALITY_CHARACTERS.charAt(offset + value);
	}

	/**
	 * TODO: Improve this API in the same style as the unused methods in
	 * {@link FastaWriter}
	 * 
	 * @param fragments
	 * @param file
	 * @throws IOException
	 */
	public static void writeFragments(List<? extends Fragment> fragments, File file)
		throws IOException
	{
		BufferedWriter output = null;
		try
		{
			output = new BufferedWriter(new FileWriter(file));
			int i = 0;
			for (Fragment fragment : fragments)
			{
				StringBuilder fragmentIdentifier = new StringBuilder();
				fragmentIdentifier.append(i++);
				Integer originalPosition = fragment.getPosition(FragmentPositionSource.ORIGINAL_SEQUENCE);
				if (originalPosition != null)
				{
					fragmentIdentifier.append(":READ_POS=");
					fragmentIdentifier.append(originalPosition);
				}
				Integer assembledPosition = fragment.getPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE);
				if (assembledPosition != null)
				{
					fragmentIdentifier.append(",");
					fragmentIdentifier.append(assembledPosition);
				}
				output.write(String.format("@%s%n", fragmentIdentifier.toString()));
				// TODO: Improve this to be Rope-smart
				output.write(fragment.toString());
				output.write(String.format("%n"));
				output.write(String.format("+%s%n", fragmentIdentifier.toString()));
				StringBuilder sb = new StringBuilder();
				for (int j = 0; j < fragment.toString().length(); j++)
				{
					// TODO: Un-hardcode Illumina 1.3 settings
					sb.append(getQualityCharacter(Constants.ILLUMINA_1_3_OFFSET,
						fragment.getReadQuality(j)));
				}
				output.write(sb.toString());
				output.write(String.format("%n"));
			}
		}
		finally
		{
			if (output != null)
			{
				output.close();
			}
		}
	}
}
