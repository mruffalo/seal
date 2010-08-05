package generator;

import java.util.Arrays;
import java.util.Random;

public class SeqGenSingleSequenceMultipleRepeats extends SequenceGenerator
{
	@Override
	public CharSequence generateSequence(Options o)
	{
		CharSequence repeatedSequence = generateSequence(o.characters, o.repeatLength);
		Random random = new Random();
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
			if (debugOutput)
			{
				for (int j = 0; j < repeatedSequenceIndices[i] - repeatStart; j++)
				{
					System.out.print(" ");
				}
				System.out.print(repeatedSequence);
				repeatStart = repeatedSequenceIndices[i];
			}
			sb.insert(i * o.repeatLength + repeatedSequenceIndices[i], repeatedSequence);
		}
		String string = sb.toString();
		if (debugOutput)
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
		int m = Integer.parseInt(args[0]);
		int r = Integer.parseInt(args[1]);
		int l = Integer.parseInt(args[2]);
		SequenceGenerator generator = new SeqGenSingleSequenceMultipleRepeats();
		generator.setDebugOutput(true);
		CharSequence generated = generator.generateSequence(m, r, l);
	}
}
