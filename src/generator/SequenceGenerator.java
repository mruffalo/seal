package generator;

import java.util.*;

public abstract class SequenceGenerator
{
	public static final String NUCLEOTIDES = "ACGT";
	public static final String NUCLEIC_ACID_ALLOWED_CHARACTERS = "ACGTURYKMSWBDHVNX-";
	public static final String AMINO_ACID_ALLOWED_CHARACTERS = "ABCDEFGHIKLMNOPQRSTUVWYZX*-";

	protected boolean debugOutput;

	public static class Options
	{
		/**
		 * Characters in the generated sequence come from here
		 */
		public String characters = NUCLEOTIDES;
		public int length;
		public int repeatCount;
		public int repeatLength;
		/**
		 * Each character in each repeat will be substituted with a random
		 * choice from {@link #characters} at this probability
		 */
		public double errorProbability;
	}

	protected static CharSequence generateSequence(String sample, int m)
	{
		Random random = new Random();
		StringBuilder sb = new StringBuilder(m);
		for (int i = 0; i < m; i++)
		{
			int index = random.nextInt(sample.length());
			sb.append(sample.substring(index, index + 1));
		}
		return sb;
	}

	/**
	 * Controls whether debugging information will be printed to
	 * <code>System.out</code>. TODO: Maybe allow a separate
	 * {@link java.io.OutputStream} to be specified.
	 * 
	 * @param debugOutput_
	 */
	public void setDebugOutput(boolean debugOutput_)
	{
		debugOutput = debugOutput_;
	}

	/**
	 * @param m
	 *            length of sequence
	 * @param r
	 *            number of repeats
	 * @param l
	 *            length of repeats
	 * @return
	 */
	public CharSequence generateSequence(int m, int r, int l)
	{
		Options o = new Options();
		o.length = m;
		o.repeatCount = r;
		o.repeatLength = l;
		return generateSequence(o);
	}

	/**
	 * @param m
	 *            length of sequence
	 * @param r
	 *            number of repeats
	 * @param l
	 *            length of repeats
	 * @return
	 */
	public abstract CharSequence generateSequence(Options o);
}
