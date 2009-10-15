package generator;

import static org.junit.Assert.*;
import org.junit.Test;

public class SeqGenSingleSequenceMultipleRepeatsTest
{
	@Test
	public void testGenerateSequenceIntIntInt()
	{
		SequenceGenerator sg = new SeqGenSingleSequenceMultipleRepeats();
		String string = sg.generateSequence(100, 4, 10);
		assertEquals(100, string.length());
		System.out.println(string);
		string = sg.generateSequence(10, 2, 5);
		assertEquals(10, string.length());
		System.out.println(string);
	}
}
