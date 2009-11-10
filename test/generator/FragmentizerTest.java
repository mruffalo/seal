package generator;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import assembly.Fragment;

public class FragmentizerTest
{
	private final String[] stringsWithSubstrings = { "ABCDEFG", "BCDEF", "DEFGH", "EFG", "FGHIJK", "K" };
	private Set<String> stringsWithoutSubstrings;
	private List<Fragment> listWithSubstrings;
	
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
	}
	
	/**
	 * TODO
	 */
	@Test
	public void testFragmentizeForShotgun()
	{
		fail("Not yet implemented");
	}
	
	/**
	 * TODO
	 */
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
	
	/**
	 * TODO
	 */
	@Test
	public void testGroupByLine()
	{
		fail("Not yet implemented");
	}
}
