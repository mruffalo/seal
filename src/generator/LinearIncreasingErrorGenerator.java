package generator;

public class LinearIncreasingErrorGenerator extends FragmentErrorGenerator
{
	private double beginErrorProbability;
	private double endErrorProbability;

	public LinearIncreasingErrorGenerator(String allowedCharacters_, double beginErrorProbability_,
		double endErrorProbability_)
	{
		super(allowedCharacters_);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double getErrorProbability(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
