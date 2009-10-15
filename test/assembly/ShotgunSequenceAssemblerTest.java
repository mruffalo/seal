package assembly;

import static org.junit.Assert.*;
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
	private List<String> biggerList;
	/**
	 * AABBCC + XXYYZZ
	 */
	private final String[] disconnectedStrings = { "AABB", "BBCC", "XXYY", "YYZZ" };
	private List<String> disconnectedList;
	
	@Before
	public void setUp()
	{
		List<String> temp = new LinkedList<String>();
		for (String string : biggerStrings)
		{
			temp.add(string);
		}
		biggerList = Collections.unmodifiableList(temp);
		temp = new LinkedList<String>();
		for (String string : disconnectedStrings)
		{
			temp.add(string);
		}
		disconnectedList = Collections.unmodifiableList(temp);
	}
	
	@Test
	public void testAssembleSequence()
	{
		SequenceAssembler sa = new ShotgunSequenceAssembler();
		String assembled = sa.assembleSequence(biggerList);
		System.out.println(assembled);
		for (String string : biggerList)
		{
			assertTrue(String.format("Fragment %s was not contained in assembled sequence %s", string, assembled),
				assembled.contains(string));
		}
	}
	
	@Test
	public void testAssembleDisconnectedSequence()
	{
		SequenceAssembler sa = new ShotgunSequenceAssembler();
		String assembled = sa.assembleSequence(disconnectedList);
		System.out.println(assembled);
		for (String string : disconnectedList)
		{
			assertTrue(String.format("Fragment %s was not contained in assembled sequence %s", string, assembled),
				assembled.contains(string));
		}
	}
	
	@Test
	public void testWithGeneratedSequence()
	{
		SequenceAssembler sa = new ShotgunSequenceAssembler();
		SequenceGenerator sg = new SeqGenSingleSequenceMultipleRepeats();
		String string = sg.generateSequence(10000, 50, 10);
		assertEquals(10000, string.length());
		List<String> list = SequenceGenerator.fragmentizeForShotgun(string, 1000, 50, 5);
		assertEquals(1000, list.size());
		String assembled = sa.assembleSequence(list);
		for (String fragment : list)
		{
			assertTrue(assembled.contains(fragment));
		}
	}
}
