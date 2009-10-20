package generator;

import java.util.*;

public abstract class SequenceGenerator
{
	public static final String NUCLEOTIDES = "ACGT";
	public static final String NUCLEIC_ACID_ALLOWED_CHARACTERS = "ACGTURYKMSWBDHVNX-";
	public static final String AMINO_ACID_ALLOWED_CHARACTERS = "ABCDEFGHIKLMNOPQRSTUVWYZX*-";
	
	protected static String generateSequence(String sample, int m)
	{
		Random random = new Random();
		StringBuilder sb = new StringBuilder(m);
		for (int i = 0; i < m; i++)
		{
			int index = random.nextInt(sample.length());
			sb.append(sample.substring(index, index + 1));
		}
		return sb.toString();
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
	public abstract String generateSequence(int m, int r, int l);
}
