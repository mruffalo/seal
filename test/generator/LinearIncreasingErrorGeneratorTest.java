package generator;

import org.junit.Test;

public class LinearIncreasingErrorGeneratorTest
{
	@Test
	@SuppressWarnings("unused")
	public void testGenerateErrors()
	{
		SeqGenSingleSequenceMultipleRepeats sg = new SeqGenSingleSequenceMultipleRepeats();
		SequenceGenerator.Options sgo = new SequenceGenerator.Options();
		sgo.length = 100;
		CharSequence orig = sg.generateSequence(sgo);
		FragmentErrorGenerator eg = new LinearIncreasingErrorGenerator(
			SequenceGenerator.NUCLEOTIDES, 0.0, 1.0);
		eg.setVerboseOutput(true);
		CharSequence errored = eg.generateErrors(orig);
	}
}
