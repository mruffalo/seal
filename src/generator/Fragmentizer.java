package generator;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import assembly.Fragment;

public class Fragmentizer
{
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
	public static List<Fragment> fragmentizeForShotgun(String string, double n, int k, int kTolerance)
	{
		Random random = new Random();
		LinkedList<Fragment> list = new LinkedList<Fragment>();
		for (int i = 0; i < n; i++)
		{
			int fragmentLength = k + random.nextInt(kTolerance * 2) - kTolerance;
			int index = random.nextInt(string.length() - fragmentLength);
			Fragment f = new Fragment(string.substring(index, index + fragmentLength));
			list.add(f);
		}
		return list;
	}
	
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
	 * XXX: TEMPORARY method to output a List&lt;Fragment&gt; to <code>System.out</code>. Example
	 * output:
	 * 
	 * <pre>
	 * 
	 * </pre>
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		
	}
}
