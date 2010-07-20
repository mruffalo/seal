package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import assembly.Fragment;
import assembly.FragmentPositionSource;
import util.ropes.*;

public class FastaReader
{
	private final BufferedReader source;
	private Integer prevReadPosition = null;
	private Integer prevAssembledPosition = null;
	
	public FastaReader(Reader in)
	{
		source = new BufferedReader(in);
	}
	
	public Fragment readFragment()
	{
		Fragment f = null;
		try
		{
			StringBuilder sb = new StringBuilder();
			String line = null;
			Integer readPosition = null;
			Integer assembledPosition = null;
			while ((line = source.readLine()) != null)
			{
				Matcher m = Constants.READ_POSITION_HEADER.matcher(line);
				if (m.matches())
				{
					//
				}
				if (line.startsWith(">"))
				{
					break;
				}
				sb.append(line.trim());
			}
			f = new Fragment(sb.toString());
		}
		catch (IOException e)
		{
			System.err.println("Caught IOException:");
			e.printStackTrace();
		}
		return f;
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
	
	public static CharSequence getLargeSequence(File file) throws IOException
	{
		RopeBuilder rb = new RopeBuilder();
		Rope sequence = rb.build("");
		Scanner input = null;
		try
		{
			input = new Scanner(new BufferedReader(new FileReader(file)));
			while (input.hasNext())
			{
				String temp = input.next();
				if (temp.startsWith(">"))
				{
					continue;
				}
				sequence = sequence.append(rb.build(temp));
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
	
	/**
	 * Does not actually operate on FASTA files due to position annotation. TODO: fix this by moving
	 * position annotation into fragment header here and in
	 * {@link FastaWriter#writeFragmentsWithPositions(List, File)}
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static List<Fragment> getFragmentsWithPositions(File file) throws IOException
	{
		BufferedReader input = null;
		List<Fragment> list = new LinkedList<Fragment>();
		try
		{
			String line;
			input = new BufferedReader(new FileReader(file));
			while ((line = input.readLine()) != null)
			{
				if (!line.startsWith(">"))
				{
					String[] pieces = line.split(Constants.FIELD_SEPARATOR);
					Fragment f = new Fragment(pieces[0]);
					if (pieces.length > 1)
					{
						try
						{
							Integer origPos = Integer.parseInt(pieces[1]);
							f.setPosition(FragmentPositionSource.ORIGINAL_SEQUENCE, origPos);
						}
						catch (NumberFormatException e)
						{
							// don't care
						}
						if (pieces.length > 2)
						{
							try
							{
								Integer assembledPos = Integer.parseInt(pieces[2]);
								f.setPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE,
									assembledPos);
							}
							catch (NumberFormatException e)
							{
								// don't care
							}
						}
					}
					list.add(f);
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
}
