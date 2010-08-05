package generator;

import java.util.Arrays;
import java.util.Random;

public class SeqGenSingleSequenceMultipleRepeats extends SequenceGenerator
{
	@Override
	public CharSequence generateSequence(Options o)
	{
		Random random = new Random();
		FragmentErrorGenerator eg = new UniformErrorGenerator(o.characters, o.errorProbability);
		final CharSequence repeatedSequence = generateSequence(o.characters, o.repeatLength);
		StringBuilder sb = new StringBuilder(o.length);
		int[] repeatedSequenceIndices = new int[o.repeatCount];
		int nonRepeatedLength = o.length - o.repeatCount * o.repeatLength;
		if (nonRepeatedLength > 0)
		{
			for (int i = 0; i < o.repeatCount; i++)
			{
				repeatedSequenceIndices[i] = random.nextInt(nonRepeatedLength);
			}
			Arrays.sort(repeatedSequenceIndices);
			sb.append(generateSequence(o.characters, nonRepeatedLength));
		}
		int repeatStart = 0;
		for (int i = 0; i < o.repeatCount; i++)
		{
			if (verbose)
			{
				for (int j = 0; j < repeatedSequenceIndices[i] - repeatStart; j++)
				{
					System.out.print(" ");
				}
				System.out.print(repeatedSequence);
				repeatStart = repeatedSequenceIndices[i];
			}
			CharSequence currentRepeatedSequence;
			if (o.errorProbability > 0.0)
			{
				currentRepeatedSequence = eg.generateErrors(repeatedSequence);
			}
			else
			{
				currentRepeatedSequence = repeatedSequence;
			}
			sb.insert(i * o.repeatLength + repeatedSequenceIndices[i], currentRepeatedSequence);
		}
		String string = sb.toString();
		if (verbose)
		{
			System.out.println();
			System.out.println(string);
		}
		return string;
	}

	/**
	 * Standalone debug method
	 * 
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		if (args.length < 3)
		{
			System.err.printf("*** Usage: %s m r l",
				SeqGenSingleSequenceMultipleRepeats.class.getCanonicalName());
			System.exit(1);
		}
		Options o = new Options();
		o.length = Integer.parseInt(args[0]);
		o.repeatCount = Integer.parseInt(args[1]);
		o.repeatLength = Integer.parseInt(args[2]);
		SequenceGenerator generator = new SeqGenSingleSequenceMultipleRepeats();
		generator.setVerboseOutput(true);
		CharSequence generated = generator.generateSequence(o);
	}
}
