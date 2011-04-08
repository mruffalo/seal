package generator;

import generator.errors.FragmentErrorGenerator;
import generator.errors.UniformErrorGenerator;
import io.FastaReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SeqGenTandemRepeats extends SequenceGenerator
{
	public static class TandemRepeatDescriptor
	{
		public final int position;
		public final int length;

		public TandemRepeatDescriptor(int position_, int length_)
		{
			position = position_;
			length = length_;
		}
	}

	public static class GeneratedSequence
	{
		public final CharSequence sequence;
		public final List<TandemRepeatDescriptor> repeats;

		public GeneratedSequence(CharSequence sequence_, List<TandemRepeatDescriptor> repeats_)
		{
			sequence = sequence_;
			repeats = Collections.unmodifiableList(repeats_);
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
		List<TandemRepeatDescriptor> positions = new ArrayList<TandemRepeatDescriptor>(
			o.repeatCount);
		StringBuilder sb = new StringBuilder(o.length);
		int[] repeatedSequenceIndices = new int[o.repeatCount];
		int nonRepeatedLength = o.length - o.repeatCount * o.repeatLength;
		if (verbose)
		{
			System.out.printf("Non-repeated sequence length: %d%n", nonRepeatedLength);
		}
		if (nonRepeatedLength > 0)
		{
			for (int i = 0; i < o.repeatCount; i++)
			{
				repeatedSequenceIndices[i] = random.nextInt(nonRepeatedLength - o.repeatLength)
						+ o.repeatLength;
			}
			Arrays.sort(repeatedSequenceIndices);
			if (verbose)
			{
				System.out.print("Raw repeat positions: ");
				for (int position : repeatedSequenceIndices)
				{
					System.out.print(position);
					System.out.print(' ');
				}
				System.out.println();
			}
			sb.append(generateSequence(o.characters, nonRepeatedLength));
		}
		if (verbose)
		{
			for (double l = Math.log10(o.length); l > 0; l--)
			{
				int p = (int) Math.pow(10, (int) l);
				for (int j = 0; j < o.length; j++)
				{
					if (j % p == 0)
					{
						System.out.print((j / p) % 10);
					}
					else
					{
						System.out.print(' ');
					}
				}
				System.out.println();
			}
		}
		int repeatStart = 0;
		for (int i = 0; i < o.repeatCount; i++)
		{
			int begin = repeatedSequenceIndices[i] + (i - 1) * o.repeatLength;
			int end = repeatedSequenceIndices[i] + i * o.repeatLength;
			positions.add(new TandemRepeatDescriptor(begin, o.repeatLength - 1));
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
		return new GeneratedSequence(string, positions);
	}

	/**
	 * TODO: Clean this up a lot -- don't duplicate code from above. Eventual
	 * improvement: separate the generation or reading of a sequence from
	 * inserting tandem duplications or other repeats
	 */
	public GeneratedSequence insertRepeatsWithPositions(Options o, CharSequence s)
	{
		final FragmentErrorGenerator eg = new UniformErrorGenerator(o.characters,
			o.repeatErrorProbability);
		List<TandemRepeatDescriptor> positions = new ArrayList<TandemRepeatDescriptor>(
			o.repeatCount);
		StringBuilder sb = new StringBuilder(s);
		int[] repeatedSequenceIndices = new int[o.repeatCount];
		int nonRepeatedLength = o.length - o.repeatCount * o.repeatLength;
		if (verbose)
		{
			System.out.printf("Non-repeated sequence length: %d%n", nonRepeatedLength);
		}
		if (nonRepeatedLength > 0)
		{
			for (int i = 0; i < o.repeatCount; i++)
			{
				repeatedSequenceIndices[i] = random.nextInt(nonRepeatedLength - o.repeatLength)
						+ o.repeatLength;
			}
			Arrays.sort(repeatedSequenceIndices);
			if (verbose)
			{
				System.out.print("Raw repeat positions: ");
				for (int position : repeatedSequenceIndices)
				{
					System.out.print(position);
					System.out.print(' ');
				}
				System.out.println();
			}
			o.length = sb.length();
		}
		if (verbose)
		{
			for (double l = Math.log10(o.length); l > 0; l--)
			{
				int p = (int) Math.pow(10, (int) l);
				for (int j = 0; j < o.length; j++)
				{
					if (j % p == 0)
					{
						System.out.print((j / p) % 10);
					}
					else
					{
						System.out.print(' ');
					}
				}
				System.out.println();
			}
		}
		int repeatStart = 0;
		for (int i = 0; i < o.repeatCount; i++)
		{
			int begin = repeatedSequenceIndices[i] + (i - 1) * o.repeatLength;
			int end = repeatedSequenceIndices[i] + i * o.repeatLength;
			positions.add(new TandemRepeatDescriptor(begin, o.repeatLength - 1));
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
		return new GeneratedSequence(string, positions);
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
		o.length = 101;
		o.repeatCount = 4;
		o.repeatLength = 5;
		SeqGenTandemRepeats generator = new SeqGenTandemRepeats();
		generator.setVerboseOutput(true);
		CharSequence s = SequenceGenerator.generateSequence(NUCLEOTIDES, 100);
		System.out.println("Original sequence:");
		System.out.println(s);
		GeneratedSequence generated = generator.insertRepeatsWithPositions(o, s);
		for (TandemRepeatDescriptor repeat : generated.repeats)
		{
			System.out.printf("Repeat: %04d - %04d%n", repeat.position, repeat.position
					+ repeat.length);
		}
	}
}
