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
		/**
		 * Probability of starting a random insert at any given base
		 */
		public double insertProbability;
		/**
		 * Average length of each insertion
		 */
		public double insertLengthMean;
		/**
		 * Standard deviation of insertion lengths
		 */
		public double insertLengthStdDev;
		/**
		 * Probability of starting a random deletion at any given base
		 */
		public double deleteProbability;
		/**
		 * Average length of each deletion
		 */
		public double deleteLengthMean;
		/**
		 * Standard deviation of deletion lengths
		 */
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
	public int getQuality(int position, int length)
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
