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
				fragmentIdentifier.append("FRAGMENT_");
				fragmentIdentifier.append(i++);
				Integer originalPosition = fragment.getPosition(FragmentPositionSource.ORIGINAL_SEQUENCE);
				if (originalPosition != null)
				{
					fragmentIdentifier.append(":");
					fragmentIdentifier.append(originalPosition);
				}
				Integer assembledPosition = fragment.getPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE);
				if (assembledPosition != null)
				{
					fragmentIdentifier.append(":");
					fragmentIdentifier.append(assembledPosition);
				}
				output.write(String.format("@%s%n", fragmentIdentifier.toString()));
				// TODO: Improve this to be Rope-smart
				output.write(fragment.toString());
				output.write(String.format("%n"));
				output.write(String.format("+%s%n", fragmentIdentifier.toString()));
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
