package generator;

/**
 * TODO: Examine whether this should really subclass
 * {@link FragmentErrorGenerator}
 * 
 * @author mruffalo
 */
public class IndelGenerator extends FragmentErrorGenerator
{
	private Options o;

	public class Options
	{
		public double insertProbability;
		public double insertLengthMean;
		public double insertLengthStdDev;
		public double deleteProbability;
		public double deleteLengthMean;
		public double deleteLengthStdDev;
	}

	public IndelGenerator(String allowedCharacters, Options o_)
	{
		super(allowedCharacters);
		o = o_;
	}

	@Override
	public CharSequence generateErrors(CharSequence sequence)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected double getSubstitutionProbability(int position, int length)
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
