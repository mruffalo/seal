package generator;

import java.util.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import assembly.Fragment;

public class SequenceGeneratorTest
{
	
	@Before
	public void setUp() throws Exception
	{
	}
	
	@Test
	public void testFragmentizeForHybridization()
	{
		String string = SequenceGenerator.generateSequence(SequenceGenerator.NUCLEOTIDES, 100);
		int k = 5;
		List<String> list = SequenceGenerator.fragmentizeForHybridization(string, k);
		assertEquals(string.length() - k + 1, list.size());
		for (String fragment : list)
		{
			assertEquals(k, fragment.length());
			assertTrue(string.contains(fragment));
		}
	}
	
	@Test
	public void testFragmentizeForShotgun()
	{
		String string = SequenceGenerator.generateSequence(SequenceGenerator.NUCLEOTIDES, 100);
		System.out.println(string);
		int tolerance = 2;
		int size = 10;
		int length = 50;
		List<Fragment> list = SequenceGenerator.fragmentizeForShotgun(string, length, size, tolerance);
		assertEquals(length, list.size());
		for (Fragment fragment : list)
		{
			System.out.println(fragment);
			assertTrue(Math.abs(fragment.string.length() - size) <= tolerance);
			assertTrue(string.contains(fragment.string));
		}
		tolerance = 4;
		size = 20;
		length = 20;
		list = SequenceGenerator.fragmentizeForShotgun(string, length, size, tolerance);
		assertEquals(length, list.size());
		for (Fragment fragment : list)
		{
			System.out.println(fragment);
			assertTrue(Math.abs(fragment.string.length() - size) <= tolerance);
			assertTrue(string.contains(fragment.string));
		}
	}
	
	@Test
	public void testGenerateSequenceStringInt()
	{
		String string = SequenceGenerator.generateSequence(SequenceGenerator.NUCLEOTIDES, 10);
		assertEquals(10, string.length());
	}
	
}
