package io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

public class SamReader
{
	private final BufferedReader source;

	public SamReader(Reader in)
	{
		source = new BufferedReader(in);
	}

	public static Set<String> readMappedFragmentSet(File sam_output, int size)
	{
		Set<String> correctlyMappedFragments = new HashSet<String>(size);
		try
		{
			BufferedReader r = new BufferedReader(new FileReader(sam_output));
			String line = null;
			while ((line = r.readLine()) != null)
			{
				if (line.startsWith("@") || line.startsWith("#"))
				{
					continue;
				}
				String[] pieces = line.split("\\s+");
				if (pieces.length <= 3)
				{
					continue;
				}
				String fragmentIdentifier = pieces[0];
				int readPosition = -1;
				Matcher m = Constants.READ_POSITION_HEADER.matcher(pieces[0]);
				if (m.matches())
				{
					readPosition = Integer.parseInt(m.group(2));
				}
				int alignedPosition = Integer.parseInt(pieces[3]) - 1;
				if (readPosition == alignedPosition)
				{
					correctlyMappedFragments.add(fragmentIdentifier);
				}
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return correctlyMappedFragments;
	}
}
