package generator;

public class LinearIncreasingErrorGenerator extends FragmentErrorGenerator
{
	public LinearIncreasingErrorGenerator(String allowedCharacters)
	{
		super(allowedCharacters);
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
