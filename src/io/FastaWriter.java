package io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import assembly.Fragment;
import assembly.FragmentPositionSource;

public class FastaWriter
{
	public static void writeSequence(CharSequence sequence, File file) throws IOException
	{
		BufferedWriter output = null;
		try
		{
			output = new BufferedWriter(new FileWriter(file));
			output.write(">COMPLETED_SEQUENCE");
			output.write(System.getProperty("line.separator"));
			// TODO: Improve this
			output.write(sequence.toString());
			output.write(System.getProperty("line.separator"));
		}
		finally
		{
			if (output != null)
			{
				output.close();
			}
		}
	}

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
				output.write(String.format(">%d", i++));
				Integer originalPosition = fragment.getPosition(FragmentPositionSource.ORIGINAL_SEQUENCE);
				if (originalPosition != null)
				{
					output.write(String.format(":READ_POS=%d", originalPosition));
				}
				Integer assembledPosition = fragment.getPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE);
				if (assembledPosition != null)
				{
					output.write(String.format(",%d", assembledPosition));
				}
				output.write(String.format("%n"));
				// TODO: Improve this to be Rope-smart
				output.write(fragment.getSequence().toString());
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
