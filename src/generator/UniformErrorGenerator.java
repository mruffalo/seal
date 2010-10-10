package generator;

public class UniformErrorGenerator extends FragmentErrorGenerator
{
	private double errorProbability;

	public UniformErrorGenerator(String allowedCharacters_, double errorProbability_)
	{
		super(allowedCharacters_);
		setErrorProbability(errorProbability_);
	}

	@Override
	protected double getErrorProbability(int position, int length)
	{
		return errorProbability;
	}

	public void setErrorProbability(double errorProbability_)
	{
		if (errorProbability_ <= 1.0 && errorProbability_ >= 0.0)
		{
			errorProbability = errorProbability_;
		}
		else
		{
			// TODO: be nicer about this :)
			throw new IllegalArgumentException("error probability must be >= 0.0 and <= 1.0");
		}
	}

	public double getErrorProbability()
	{
		return errorProbability;
	}

	/**
	 * Don't need to use the nice {@link #getErrorProbability(int)} method,
	 * since we know that it's the same for every character position in this
	 * class.
	 */
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
			if (r.nextDouble() <= errorProbability)
			{
				sb.append(chooseRandomCharacter(allowedCharacters));
				errorIndicator.append("X");
			}
			else
			{
				sb.append(sequence.charAt(i));
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
}
