package assembly;

import java.util.*;
import java.io.*;

public class FastaHandler
{
	public static List<Fragment> getFragments(String filename) throws IOException
	{
		BufferedReader input = null;
		List<Fragment> list = new LinkedList<Fragment>();
		try
		{
			String line;
			StringBuilder sb = null;
			input = new BufferedReader(new FileReader(filename));
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
	
	public static String getSequence(String filename) throws IOException
	{
		String sequence = null;
		Scanner input = null;
		String storage;
		try
		{
			input = new Scanner(new BufferedReader(new FileReader(filename)));
			while (input.hasNext())
			{
				storage = input.next();
				if (storage.contains(">"))
				{
					storage = input.next();
				}
				sequence = storage;
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
	
	public static void writeSequence(String sequence, String filename) throws IOException
	{
		BufferedWriter output = null;
		try
		{
			output = new BufferedWriter(new FileWriter(filename));
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
	
	public static void writeFragments(List<Fragment> fragments, String filename) throws IOException
	{
		BufferedWriter output = null;
		try
		{
			output = new BufferedWriter(new FileWriter(filename));
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
}
