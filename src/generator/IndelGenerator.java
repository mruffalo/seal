package generator;

/**
 * TODO: Examine whether this should really subclass
 * {@link FragmentErrorGenerator}
 * 
 * @author mruffalo
 */
public class IndelGenerator extends FragmentErrorGenerator
{
	public IndelGenerator(String allowedCharacters)
	{
		super(allowedCharacters);
		// TODO Auto-generated constructor stub
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
