package io;

public class FastqReader
{
	private static enum State
	{
		READ_HEADER,
		READ_DATA,
		QUALITY_HEADER,
		QUALITY_DATA;
	}
}
