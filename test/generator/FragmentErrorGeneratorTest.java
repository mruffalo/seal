package generator;

import static org.junit.Assert.*;
import org.junit.Test;

public class FragmentErrorGeneratorTest
{
	@Test
	public void testAssignReplacementCharacters()
	{
		FragmentErrorGenerator eg = new UniformErrorGenerator(SequenceGenerator.NUCLEOTIDES, 0.0);
		for (int i = 0; i < SequenceGenerator.NUCLEOTIDES.length(); i++)
		{
			char c = SequenceGenerator.NUCLEOTIDES.charAt(i);
			System.out.printf("%s: %s%n", c, eg.replacements.get(c));
		}
		System.out.println();
	}

	@Test
	public void testChooseRandomCharacter()
	{
		fail("Not yet implemented");
	}
}
