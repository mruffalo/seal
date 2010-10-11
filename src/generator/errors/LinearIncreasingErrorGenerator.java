package generator.errors;

public class LinearIncreasingErrorGenerator extends SubstitutionErrorGenerator
{
	private double beginErrorProbability;
	private double endErrorProbability;

	public LinearIncreasingErrorGenerator(String allowedCharacters_, double beginErrorProbability_,
		double endErrorProbability_)
	{
		super(allowedCharacters_);
		setBeginErrorProbability(beginErrorProbability_);
		setEndErrorProbability(endErrorProbability_);
	}

	public double getBeginErrorProbability()
	{
		return beginErrorProbability;
	}

	public void setBeginErrorProbability(double beginErrorProbability_)
	{
		if (beginErrorProbability_ <= 1.0 && beginErrorProbability_ >= 0.0)
		{
			beginErrorProbability = beginErrorProbability_;
		}
		else
		{
			// TODO: be nicer about this :)
			throw new IllegalArgumentException("error probability must be >= 0.0 and <= 1.0");
		}
	}

	public double getEndErrorProbability()
	{
		return endErrorProbability;
	}

	public void setEndErrorProbability(double endErrorProbability_)
	{
		if (endErrorProbability_ <= 1.0 && endErrorProbability_ >= 0.0)
		{
			endErrorProbability = endErrorProbability_;
		}
		else
		{
			// TODO: be nicer about this :)
			throw new IllegalArgumentException("error probability must be >= 0.0 and <= 1.0");
		}
	}

	@Override
	public CharSequence generateErrors(CharSequence sequence)
	{
		if (verbose)
		{
			System.err.println();
			System.err.printf("Original sequence: %s%n", sequence);
			System.err.print("                   ");
		}
		StringBuilder sb = new StringBuilder(sequence.length());
		StringBuilder errorIndicator = new StringBuilder(sequence.length());
		for (int i = 0; i < sequence.length(); i++)
		{
			char orig = sequence.charAt(i);
			if (random.nextDouble() <= getSubstitutionProbability(i, sequence.length()))
			{
				sb.append(chooseRandomReplacementCharacter(orig));
				errorIndicator.append("X");
			}
			else
			{
				sb.append(orig);
				errorIndicator.append(" ");
			}
		}
		if (verbose)
		{
			System.err.println(errorIndicator.toString());
			System.err.printf("New sequence:      %s%n%n", sb.toString());
		}
		return sb;
	}

	@Override
	protected double getSubstitutionProbability(int position, int length)
	{
		return beginErrorProbability + (endErrorProbability - beginErrorProbability)
				* ((double) position / (double) length);
	}
}
