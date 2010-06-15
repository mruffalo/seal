package assembly;

import static org.junit.Assert.*;
import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ShotgunSequenceAssemblerTest
{
	/**
	 * ACTGACCTGCATTTCA
	 */
	private final String[] biggerStrings = { "ACTGAC", "ACCTG", "CTGCA", "GCATT", "ATTT", "TTCA" };
	private List<Fragment> biggerList;
	/**
	 * AABBCC + XXYYZZ
	 */
	private final String[] disconnectedStrings = { "AABB", "BBCC", "XXYY", "YYZZ" };
	private List<Fragment> disconnectedList;
	
	/**
	 * AA + BB + CC + DD
	 */
	private final String[] disjointStrings = { "AA", "BB", "CC", "DD" };
	private List<Fragment> disjointList;
	
	private final String[] badStrings = { "AAGGGGTATT", "ACACATTACG", "ATGACGGGTA", "CATTACGTGA", "CGGCAAACGT" };
	private List<Fragment> badList;
	
	@Before
	public void setUp()
	{
		List<Fragment> temp = new LinkedList<Fragment>();
		for (String string : biggerStrings)
		{
			temp.add(new Fragment(string));
		}
		biggerList = Collections.unmodifiableList(temp);
		temp = new LinkedList<Fragment>();
		for (String string : disconnectedStrings)
		{
			temp.add(new Fragment(string));
		}
		disconnectedList = Collections.unmodifiableList(temp);
		temp = new LinkedList<Fragment>();
		for (String string : disjointStrings)
		{
			temp.add(new Fragment(string));
		}
		disjointList = Collections.unmodifiableList(temp);
		temp = new LinkedList<Fragment>();
		for (String string : badStrings)
		{
			temp.add(new Fragment(string));
		}
		badList = Collections.unmodifiableList(temp);
	}
	
	@Test
	public void testAssembleSequence()
	{
		SequenceAssembler sa = new ShotgunSequenceAssembler();
		String assembled = sa.assembleSequence(biggerList);
		System.out.println(assembled);
		for (Fragment fragment : biggerList)
		{
			assertTrue(String.format("Fragment %s was not contained in assembled sequence %s", fragment.string,
				assembled), assembled.contains(fragment.string));
		}
	}
	
	@Test
	public void testAssembleDisconnectedSequence()
	{
		SequenceAssembler sa = new ShotgunSequenceAssembler();
		String assembled = sa.assembleSequence(disconnectedList);
		System.out.println(assembled);
		for (Fragment fragment : disconnectedList)
		{
			assertTrue(String.format("Fragment %s was not contained in assembled sequence %s", fragment.string,
				assembled), assembled.contains(fragment.string));
		}
	}
	
	@Test
	public void testPrintBiggerSequence()
	{
		testAndPrintAssembly(biggerList);
	}
	
	@Test
	public void testPrintDisconnectedSequence()
	{
		testAndPrintAssembly(disconnectedList);
	}
	
	@Test
	public void testPrintDisjointSequence()
	{
		testAndPrintAssembly(disjointList);
	}
	
	@Test
	public void testPrintBadSequence()
	{
		testAndPrintAssembly(badList);
	}
	
	public void testAndPrintAssembly(List<Fragment> list)
	{
		SequenceAssembler sa = new ShotgunSequenceAssembler();
		String assembled = sa.assembleSequence(list);
		for (Fragment fragment : list)
		{
			assertTrue(String.format("Fragment %s was not contained in assembled sequence %s", fragment.string,
				assembled), assembled.contains(fragment.string));
			assertNotNull(fragment.getPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE));
		}
		System.out.println();
		System.out.println(assembled);
		FragmentPositionSource source = FragmentPositionSource.ASSEMBLED_SEQUENCE;
		List<List<Fragment>> grouped = Fragmentizer.groupByLine(list, source);
		for (List<Fragment> subList : grouped)
		{
			int begin = 0;
			for (Fragment fragment : subList)
			{
				int position = fragment.getPosition(source);
				String assembledSubstring = assembled.substring(position, fragment.string.length() + position);
				assertEquals(fragment.string, assembledSubstring);
				for (int i = 0; i < fragment.getPosition(source) - begin; i++)
				{
					System.out.print(" ");
				}
				System.out.print(fragment.string);
				begin = fragment.getPosition(source) + fragment.string.length();
			}
			System.out.println();
		}
	}
	
	@Test
	public void testWithGeneratedSequence()
	{
		SequenceAssembler sa = new ShotgunSequenceAssembler();
		SequenceGenerator sg = new SeqGenSingleSequenceMultipleRepeats();
		String string = sg.generateSequence(10000, 50, 10);
		assertEquals(10000, string.length());
		Fragmentizer.Options o = new Fragmentizer.Options();
		o.n = 1000;
		o.k = 50;
		o.ksd = 5;
		List<Fragment> list = Fragmentizer.fragmentizeForShotgun(string, o);
		assertTrue(list.size() <= 1000);
		String assembled = sa.assembleSequence(list);
		for (Fragment fragment : list)
		{
			assertTrue(assembled.contains(fragment.string));
		}
	}
	
	/**
	 * Ensure that every character in the assembled string comes from at least one fragment.
	 */
	@Test
	public void testNoExtraCharacters()
	{
		SequenceAssembler sa = new ShotgunSequenceAssembler();
		SequenceGenerator sg = new SeqGenSingleSequenceMultipleRepeats();
		String string = sg.generateSequence(100, 0, 0);
		Fragmentizer.Options o = new Fragmentizer.Options();
		o.n = 50;
		o.k = 10;
		o.ksd = 0;
		List<Fragment> list = Fragmentizer.fragmentizeForShotgun(string, o);
		String assembled = sa.assembleSequence(list);
		ArrayList<ArrayList<Fragment>> fragmentCounts = new ArrayList<ArrayList<Fragment>>(assembled.length());
		for (int i = 0; i < assembled.length(); i++)
		{
			fragmentCounts.add(new ArrayList<Fragment>());
		}
		for (Fragment fragment : list)
		{
			Integer position = fragment.getPosition(FragmentPositionSource.ASSEMBLED_SEQUENCE);
			for (int i = 0; i < fragment.string.length(); i++)
			{
				fragmentCounts.get(i + position).add(fragment);
			}
		}
		for (int i = 0; i < fragmentCounts.size(); i++)
		{
			System.out.printf("%s : ", assembled.substring(i, i + 1));
			ArrayList<Fragment> fragmentCount = fragmentCounts.get(i);
			for (Fragment fragment : fragmentCount)
			{
				System.out.printf("%s ", fragment.string);
			}
			System.out.println();
			assertTrue(fragmentCount.size() > 0);
		}
	}
}
