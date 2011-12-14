package generator.errors;

public class LinearIncreasingErrorGenerator extends SubstitutionErrorGenerator
{
	private double beginErrorProbabilityMin;
	private double beginErrorProbabilityMax;
	private double endErrorProbabilityMin;
	private double endErrorProbabilityMax;
	private double errorProbabilityStdDev;

	public LinearIncreasingErrorGenerator(String allowedCharacters_, double beginErrorProbability_,
			double endErrorProbability_, double errorProbabilityStdDev_)
	{
		super(allowedCharacters_);
		setBeginErrorProbabilityMin(beginErrorProbability_);
		setEndErrorProbabilityMax(endErrorProbability_);
	}

	public double getBeginErrorProbabilityMin()
	{
		return beginErrorProbabilityMin;
	}

	public void setBeginErrorProbabilityMin(double beginErrorProbability_)
	{
		if (beginErrorProbability_ <= 1.0 && beginErrorProbability_ >= 0.0)
		{
			beginErrorProbabilityMin = beginErrorProbability_;
		}
		else
		{
			// TODO: be nicer about this :)
			throw new IllegalArgumentException("error probability must be >= 0.0 and <= 1.0");
		}
	}

	public double getBeginErrorProbabilityMax()
	{
		return beginErrorProbabilityMax;
	}

	public void setBeginErrorProbabilityMax(double beginErrorProbabilityMax_)
	{
		if (beginErrorProbabilityMax_ <= 1.0 && beginErrorProbabilityMax_ >= 0.0)
		{
			beginErrorProbabilityMax = beginErrorProbabilityMax_;
		}
		else
		{
			// TODO: be nicer about this :)
			throw new IllegalArgumentException("error probability must be >= 0.0 and <= 1.0");
		}
	}

	public double getEndErrorProbabilityMin()
	{
		return endErrorProbabilityMin;
	}

	public void setEndErrorProbabilityMin(double endErrorProbabilityMin_)
	{
		if (endErrorProbabilityMin_ <= 1.0 && endErrorProbabilityMin_ >= 0.0)
		{
			endErrorProbabilityMin = endErrorProbabilityMin_;
		}
		else
		{
			// TODO: be nicer about this :)
			throw new IllegalArgumentException("error probability must be >= 0.0 and <= 1.0");
		}
	}

	public double getEndErrorProbabilityMax()
	{
		return endErrorProbabilityMax;
	}

	public void setEndErrorProbabilityMax(double endErrorProbability_)
	{
		if (endErrorProbability_ <= 1.0 && endErrorProbability_ >= 0.0)
		{
			endErrorProbabilityMax = endErrorProbability_;
		}
		else
		{
			// TODO: be nicer about this :)
			throw new IllegalArgumentException("error probability must be >= 0.0 and <= 1.0");
		}
	}

	public double getErrorProbabilityStdDev()
	{
		return errorProbabilityStdDev;
	}

	public void setErrorProbabilityStdDev(double errorProbabilityStdDev_)
	{
		if (errorProbabilityStdDev_ <= 1.0 && errorProbabilityStdDev_ >= 0.0)
		{
			errorProbabilityStdDev = errorProbabilityStdDev_;
		}
		else
		{
			// TODO: be nicer about this :)
			throw new IllegalArgumentException("error std.dev must be >= 0.0");
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
		return beginErrorProbabilityMin + (endErrorProbabilityMax - beginErrorProbabilityMin)
				* ((double) position / (double) length);
	}
}
