package generator;

import java.util.*;

public abstract class SequenceGenerator
{
	public static final String NUCLEOTIDES = "ACGT";
	public static final String NUCLEIC_ACID_ALLOWED_CHARACTERS = "ACGTURYKMSWBDHVNX-";
	public static final String AMINO_ACID_ALLOWED_CHARACTERS = "ABCDEFGHIKLMNOPQRSTUVWYZX*-";
	
	/**
	 * @param string
	 *            String to fragmentize
	 * @param k
	 *            Length of each fragment
	 * @return A List of all substrings of length <code>k</code> contained in <code>string</code>
	 */
	public static List<String> fragmentizeForHybridization(String string, int k)
	{
		LinkedList<String> list = new LinkedList<String>();
		for (int i = 0; i <= string.length() - k; i++)
		{
			list.add(string.substring(i, i + k));
		}
		return list;
	}
	
	/**
	 * @param string
	 *            String to fragmentize
	 * @param n
	 *            Number of fragments to read from <code>string</code>
	 * @param k
	 *            Approximate length of each fragment
	 * @param kTolerance
	 *            Leeway in fragment size -- each fragment will be of length
	 *            <code>length ± lengthTolerance</code>
	 * @return
	 */
	public static List<String> fragmentizeForShotgun(String string, double n, int k, int kTolerance)
	{
		Random random = new Random();
		LinkedList<String> list = new LinkedList<String>();
		for (int i = 0; i < n; i++)
		{
			int fragmentLength = k + random.nextInt(kTolerance * 2) - kTolerance;
			int index = random.nextInt(string.length() - fragmentLength);
			list.add(string.substring(index, index + fragmentLength));
		}
		return list;
	}
	
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
