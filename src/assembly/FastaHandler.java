package assembly;

import java.util.*;
import java.io.*;

public class FastaHandler
{
	public static final String FIELD_SEPARATOR = ",";
	
	public static List<Fragment> getFragmentsWithPositions(File file) throws IOException
	{
		BufferedReader input = null;
		List<Fragment> list = new LinkedList<Fragment>();
		try
		{
			String line;
			StringBuilder sb = null;
			input = new BufferedReader(new FileReader(file));
			while ((line = input.readLine()) != null)
			{
				if (line.startsWith(">"))
				{
					if (sb != null)
					{
						String lines = sb.toString();
						String[] pieces = lines.split(FastaHandler.FIELD_SEPARATOR);
						Fragment f = new Fragment(pieces[0]);
						try
						{
							Integer origPos = Integer.parseInt(pieces[1]);
							f.setPosition(FragmentPositionSource.ORIGINAL_SEQUENCE, origPos);
						}
						catch (NumberFormatException e)
						{
							// don't care
						}
						try
						{
							Integer assembledPos = Integer.parseInt(pieces[2]);
							f.setPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE, assembledPos);
						}
						catch (NumberFormatException e)
						{
							// don't care
						}
					}
					sb = new StringBuilder();
				}
				else
				{
					sb.append(line.trim());
				}
			}
		}
		finally
		{
			if (input != null)
			{
				input.close();
			}
		}
		return list;
	}
	
	public static List<Fragment> getFragments(File file) throws IOException
	{
		BufferedReader input = null;
		List<Fragment> list = new LinkedList<Fragment>();
		try
		{
			String line;
			StringBuilder sb = null;
			input = new BufferedReader(new FileReader(file));
			while ((line = input.readLine()) != null)
			{
				if (line.startsWith(">"))
				{
					if (sb != null)
					{
						list.add(new Fragment(sb.toString()));
					}
					sb = new StringBuilder();
				}
				else
				{
					sb.append(line.trim());
				}
			}
		}
		finally
		{
			if (input != null)
			{
				input.close();
			}
		}
		return list;
	}
	
	public static String getSequence(File file) throws IOException
	{
		String sequence = null, temp = null;
		Scanner input = null;
		try
		{
			input = new Scanner(new BufferedReader(new FileReader(file)));
			while (input.hasNext())
			{
				temp = input.next();
				if (temp.startsWith(">"))
				{
					continue;
				}
				sequence = temp;
			}
		}
		finally
		{
			if (input != null)
			{
				input.close();
			}
		}
		return sequence;
	}
	
	public static void writeSequence(String sequence, File file) throws IOException
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
	
	public static void writeFragments(List<Fragment> fragments, File file) throws IOException
	{
		BufferedWriter output = null;
		try
		{
			output = new BufferedWriter(new FileWriter(file));
			int i = 0;
			for (Fragment fragment : fragments)
			{
				output.write(String.format(">FRAGMENT_%d%n", i++));
				output.write(fragment.string);
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
	
	public static void writeFragmentsWithPositions(List<Fragment> fragments, File file) throws IOException
	{
		BufferedWriter output = null;
		try
		{
			output = new BufferedWriter(new FileWriter(file));
			output.write(String.format(">Data format:%n"));
			output.write(String.format(">fragment%soriginal_position:%sassembled_position%n", FIELD_SEPARATOR,
				FIELD_SEPARATOR));
			int i = 0;
			for (Fragment fragment : fragments)
			{
				output.write(String.format(">FRAGMENT_%d%n", i++));
				output.write(fragment.string);
				output.write(",");
				Integer origPos = fragment.getPosition(FragmentPositionSource.ORIGINAL_SEQUENCE);
				if (origPos != null)
				{
					output.write(origPos);
				}
				output.write(",");
				Integer assembledPos = fragment.getPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE);
				if (assembledPos != null)
				{
					output.write(assembledPos);
				}
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
