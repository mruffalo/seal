package generator;

import java.util.*;
import static org.junit.Assert.*;
import org.junit.Test;
import assembly.Fragment;

public class SequenceGeneratorTest
{
	@Test
	public void testFragmentizeForHybridization()
	{
		String string = SequenceGenerator.generateSequence(SequenceGenerator.NUCLEOTIDES, 100);
		int k = 5;
		List<String> list = Fragmentizer.fragmentizeForHybridization(string, k);
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
		Fragmentizer.Options o = new Fragmentizer.Options();
		o.n = 50;
		o.k = 10;
		o.kv = 2;
		List<Fragment> list = Fragmentizer.fragmentizeForShotgun(string, o);
		assertTrue(o.n >= list.size());
		for (Fragment fragment : list)
		{
			System.out.println(fragment);
			assertTrue(Math.abs(fragment.string.length() - o.k) <= o.kv);
			assertTrue(string.contains(fragment.string));
		}
		o.n = 20;
		o.k = 20;
		o.kv = 4;
		list = Fragmentizer.fragmentizeForShotgun(string, o);
		assertTrue(o.n >= list.size());
		for (Fragment fragment : list)
		{
			System.out.println(fragment);
			assertTrue(Math.abs(fragment.string.length() - o.k) <= o.kv);
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
