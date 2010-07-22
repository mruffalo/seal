package generator;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import assembly.Fragment;
import assembly.FragmentPositionSource;

public class FragmentizerTest
{
	private final String[] stringsWithSubstrings = { "ABCDEFG", "BCDEF", "DEFGH", "EFG", "ABCDEFG",
			"FGHIJK", "K" };
	private Set<String> stringsWithoutSubstrings;
	private List<Fragment> listWithSubstrings;
	
	private final String duplicateString = "TCTCACTAAGACGGACAACC";
	private final int origPos = 148;
	private final int assembledPos = 247;
	private List<Fragment> duplicatesWithPositions;
	
	@Before
	public void setUp() throws Exception
	{
		List<Fragment> temp = new LinkedList<Fragment>();
		for (String string : stringsWithSubstrings)
		{
			temp.add(new Fragment(string));
		}
		listWithSubstrings = Collections.unmodifiableList(temp);
		
		Set<String> tempSet = new HashSet<String>();
		for (String string : new String[] { "ABCDEFG", "DEFGH", "FGHIJK" })
		{
			tempSet.add(string);
		}
		stringsWithoutSubstrings = Collections.unmodifiableSet(tempSet);
		
		temp = new LinkedList<Fragment>();
		Fragment f = new Fragment(duplicateString);
		f.setPosition(FragmentPositionSource.ORIGINAL_SEQUENCE, origPos);
		f.setPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE, origPos);
		temp.add(f);
		f = new Fragment(duplicateString);
		f.setPosition(FragmentPositionSource.ORIGINAL_SEQUENCE, origPos);
		f.setPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE, assembledPos);
		temp.add(f);
		duplicatesWithPositions = Collections.unmodifiableList(temp);
	}
	
	/**
	 * TODO
	 */
	@Test
	public void testFragmentizeForShotgun()
	{
		CharSequence string = SequenceGenerator.generateSequence(SequenceGenerator.NUCLEOTIDES, 100);
		System.out.println(string);
		Fragmentizer.Options o = new Fragmentizer.Options();
		o.n = 50;
		o.k = 10;
		o.ksd = 2;
		List<Fragment> list = Fragmentizer.fragmentizeForShotgun(string, o);
		assertTrue(o.n >= list.size());
		for (Fragment fragment : list)
		{
			System.out.println(fragment);
			assertTrue(string.toString().contains(fragment.getString()));
		}
		o.n = 20;
		o.k = 20;
		o.ksd = 4;
		list = Fragmentizer.fragmentizeForShotgun(string, o);
		assertTrue(o.n >= list.size());
		for (Fragment fragment : list)
		{
			System.out.println(fragment);
			assertTrue(Math.abs(fragment.getString().length() - o.k) <= o.ksd);
			assertTrue(string.toString().contains(fragment.getString()));
		}
	}
	
	@Test
	public void testFragmentizeForHybridization()
	{
		CharSequence string = SequenceGenerator.generateSequence(SequenceGenerator.NUCLEOTIDES, 100);
		int k = 5;
		List<Fragment> list = Fragmentizer.fragmentizeForHybridization(string, k);
		assertEquals(string.length() - k + 1, list.size());
		for (Fragment fragment : list)
		{
			assertEquals(k, fragment.getString().length());
			assertTrue(string.toString().contains(fragment.getString()));
		}
	}
	
	@Test
	public void testRemoveSubstrings()
	{
		Set<String> strings = new HashSet<String>();
		List<Fragment> fragments = Fragmentizer.removeSubstrings(listWithSubstrings);
		for (Fragment f : fragments)
		{
			strings.add(f.getString().toString());
		}
		// Sanity check: make sure that we don't get any duplicate strings
		assertEquals(strings.size(), fragments.size());
		for (String s : strings)
		{
			assertTrue(stringsWithoutSubstrings.contains(s));
		}
	}
	
	@Test
	public void testRemoveSubstringsWithPositions()
	{
		List<Fragment> fragments = Fragmentizer.removeSubstrings(duplicatesWithPositions);
		assertEquals(1, fragments.size());
	}
	
	/**
	 * TODO
	 */
	@Test
	public void testGroupByLine()
	{
		fail("Not yet implemented");
	}
}
