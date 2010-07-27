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
			output.write(String.format(">COMPLETED_SEQUENCE%n%s%n", sequence));
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
				output.write(String.format(">FRAGMENT_%d", i++));
				Integer originalPosition = fragment.getPosition(FragmentPositionSource.ORIGINAL_SEQUENCE);
				if (originalPosition != null)
				{
					output.write(String.format(":%d", originalPosition));
				}
				output.write(String.format("%n"));
				// TODO: Improve this to be Rope-smart
				output.write(fragment.getString().toString());
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
	
	/**
	 * Doesn't actually write a valid FASTA file due to position annotations
	 * 
	 * @param fragments
	 * @param file
	 * @throws IOException
	 */
	public static void writeFragmentsWithPositions(List<Fragment> fragments, File file)
		throws IOException
	{
		BufferedWriter output = null;
		try
		{
			output = new BufferedWriter(new FileWriter(file));
			output.write(String.format(">Data format:%n"));
			output.write(String.format(">fragment%soriginal_position%sassembled_position%n",
				Constants.FIELD_SEPARATOR, Constants.FIELD_SEPARATOR));
			int i = 0;
			for (Fragment fragment : fragments)
			{
				output.write(String.format(">FRAGMENT_%d%n", i++));
				StringBuilder sb = new StringBuilder();
				sb.append(fragment.getString());
				sb.append(",");
				Integer origPos = fragment.getPosition(FragmentPositionSource.ORIGINAL_SEQUENCE);
				if (origPos != null)
				{
					sb.append(origPos);
				}
				sb.append(",");
				Integer assembledPos = fragment.getPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE);
				if (assembledPos != null)
				{
					sb.append(assembledPos);
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
