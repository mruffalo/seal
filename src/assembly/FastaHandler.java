package assembly;

import java.util.*;
import java.io.*;

public class FastaHandler
{
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
}
