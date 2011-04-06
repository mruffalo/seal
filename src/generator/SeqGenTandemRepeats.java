package generator;

import generator.errors.FragmentErrorGenerator;
import generator.errors.UniformErrorGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SeqGenTandemRepeats extends SequenceGenerator
{
	public static class GeneratedSequence
	{
		public final CharSequence sequence;
		public final List<Integer> tandemRepeatPositions;

		public GeneratedSequence(CharSequence sequence_, List<Integer> positions_)
		{
			sequence = sequence_;
			tandemRepeatPositions = Collections.unmodifiableList(positions_);
		}
	}

	Random random = new Random();

	@Override
	public CharSequence generateSequence(Options o)
	{
		return generateSequenceWithPositions(o).sequence;
	}

	/**
	 * This is a <b>huge</b> hack and I feel bad about it.
	 * 
	 * @param o
	 * @return
	 */
	public GeneratedSequence generateSequenceWithPositions(Options o)
	{
		final FragmentErrorGenerator eg = new UniformErrorGenerator(o.characters,
			o.repeatErrorProbability);
		StringBuilder sb = new StringBuilder(o.length);
		int[] repeatedSequenceIndices = new int[o.repeatCount];
		int nonRepeatedLength = o.length - o.repeatCount * o.repeatLength;
		if (nonRepeatedLength > 0)
		{
			for (int i = 0; i < o.repeatCount; i++)
			{
				repeatedSequenceIndices[i] = random.nextInt(nonRepeatedLength - o.repeatLength)
						+ o.repeatLength;
			}
			Arrays.sort(repeatedSequenceIndices);
			sb.append(generateSequence(o.characters, nonRepeatedLength));
		}
		int repeatStart = 0;
		for (int i = 0; i < o.repeatCount; i++)
		{
			int begin = repeatedSequenceIndices[i] + (i - 1) * o.repeatLength;
			int end = repeatedSequenceIndices[i] + i * o.repeatLength;
			CharSequence repeatedSequence = sb.subSequence(begin, end);
			if (o.repeatErrorProbability > 0.0)
			{
				repeatedSequence = eg.generateErrors(repeatedSequence);
			}
			if (verbose)
			{
				for (int j = 0; j < repeatedSequenceIndices[i] - repeatStart - o.repeatLength; j++)
				{
					System.out.print(" ");
				}
				for (int j = 0; j < o.repeatLength; j++)
				{
					System.out.print("-");
				}
				System.out.print(repeatedSequence);
				repeatStart = repeatedSequenceIndices[i];
			}
			sb.insert(i * o.repeatLength + repeatedSequenceIndices[i], repeatedSequence);
		}
		String string = sb.toString();
		if (verbose)
		{
			System.out.println();
			System.out.println(string);
		}
		return new GeneratedSequence(string, new ArrayList<Integer>());
	}

	/**
	 * Standalone debug method
	 * 
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		Options o = new Options();
		o.length = 100;
		o.repeatCount = 2;
		o.repeatLength = 10;
		SequenceGenerator generator = new SeqGenTandemRepeats();
		generator.setVerboseOutput(true);
		CharSequence generated = generator.generateSequence(o);
	}
}
