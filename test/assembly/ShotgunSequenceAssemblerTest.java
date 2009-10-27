package assembly;

import static org.junit.Assert.*;
import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
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
		List<Fragment> list = Fragmentizer.fragmentizeForShotgun(string, 1000, 50, 5);
		assertEquals(1000, list.size());
		String assembled = sa.assembleSequence(list);
		for (Fragment fragment : list)
		{
			assertTrue(assembled.contains(fragment.string));
		}
	}
}
