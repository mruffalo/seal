package generator;

import java.util.*;
import assembly.Fragment;
import assembly.FragmentPositionSource;

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
			f.setPosition(FragmentPositionSource.ORIGINAL_SEQUENCE, index);
			list.add(f);
		}
		return list;
	}
	
	/**
	 * TODO: Improve runtime from O(n^2) and generally make this code less stupid
	 * 
	 * @param fragments
	 * @return
	 */
	public static List<List<Fragment>> groupByLine(List<Fragment> fragments, FragmentPositionSource source)
	{
		List<List<Fragment>> groupedList = new LinkedList<List<Fragment>>();
		Set<Fragment> fragmentSet = new HashSet<Fragment>(fragments);
		fragmentSet.addAll(fragments);
		while (!fragmentSet.isEmpty())
		{
			Set<Fragment> possibleFragments = new HashSet<Fragment>(fragmentSet);
			List<Fragment> list = new LinkedList<Fragment>();
			int begin = 0;
			while (!possibleFragments.isEmpty())
			{
				Fragment earliestFinish = null;
				Iterator<Fragment> iterator = possibleFragments.iterator();
				while (iterator.hasNext())
				{
					Fragment fragment = iterator.next();
					if (fragment.getPosition(source) >= begin)
					{
						if (earliestFinish == null
								|| (fragment.getPosition(source) + fragment.string.length()) < (earliestFinish.getPosition(source) + earliestFinish.string.length()))
						{
							earliestFinish = fragment;
						}
					}
					else
					{
						iterator.remove();
					}
				}
				if (earliestFinish != null)
				{
					list.add(earliestFinish);
					/*
					 * +1 here ensures that successive fragments on one line are separated by a gap
					 * of at least one character.
					 */
					begin = earliestFinish.getPosition(source) + earliestFinish.string.length() + 1;
					fragmentSet.remove(earliestFinish);
				}
			}
			groupedList.add(list);
		}
		for (List<Fragment> list : groupedList)
		{
			int begin = 0;
			for (Fragment fragment : list)
			{
				for (int i = 0; i < fragment.getPosition(source) - begin; i++)
				{
					System.out.print(" ");
				}
				System.out.print(fragment.string);
				begin = fragment.getPosition(source) + fragment.string.length();
			}
			System.out.println();
		}
		return groupedList;
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
	 * XXX: TEMPORARY method to output a <code>String</code> and its
	 * <code>List&lt;Fragment&gt;</code> to <code>System.out</code>. Example output:
	 * 
	 * <pre>
	 * 
	 * </pre>
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args.length < 4)
		{
			System.err.printf("*** Usage: %s string n k kTolerance", Fragmentizer.class.getCanonicalName());
			System.exit(1);
		}
		String string = args[0];
		int n = Integer.parseInt(args[1]);
		int k = Integer.parseInt(args[2]);
		int kTolerance = Integer.parseInt(args[3]);
		List<Fragment> fragments = fragmentizeForShotgun(string, n, k, kTolerance);
		for (Fragment fragment : fragments)
		{
			System.out.printf("%5d: %s%n", fragment.getPosition(FragmentPositionSource.ORIGINAL_SEQUENCE),
				fragment.string);
		}
		System.out.println();
		System.out.println(string);
		groupByLine(fragments, FragmentPositionSource.ORIGINAL_SEQUENCE);
	}
}
