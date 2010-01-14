package generator;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import assembly.Fragment;
import assembly.FragmentPositionSource;

public class FragmentizerTest
{
	private final String[] stringsWithSubstrings = { "ABCDEFG", "BCDEF", "DEFGH", "EFG", "ABCDEFG", "FGHIJK", "K" };
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
		fail("Not yet implemented");
	}
	
	@Test
	public void testRemoveSubstrings()
	{
		Set<String> strings = new HashSet<String>();
		List<Fragment> fragments = Fragmentizer.removeSubstrings(listWithSubstrings);
		for (Fragment f : fragments)
		{
			strings.add(f.string);
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
