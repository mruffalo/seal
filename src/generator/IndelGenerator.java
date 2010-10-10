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
	private SequenceGenerator sg;

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
		sg = new SeqGenSingleSequenceMultipleRepeats();
	}

	/**
	 * TODO: Test this
	 */
	@Override
	public CharSequence generateErrors(CharSequence sequence)
	{
		StringBuilder sb = new StringBuilder(sequence.length());
		for (int i = 0; i < sequence.length(); i++)
		{
			if (random.nextDouble() >= o.insertProbability)
			{
				int insertLength = (int) (o.insertLengthMean + o.insertLengthStdDev
						* random.nextGaussian());
				SequenceGenerator.Options sgo = new SequenceGenerator.Options();
				sgo.length = insertLength;
				sb.append(sg.generateSequence(sgo));
			}
			if (random.nextDouble() >= o.deleteProbability)
			{
				int deleteLength = (int) (o.insertLengthMean + o.insertLengthStdDev
						* random.nextGaussian());
				i += deleteLength;
			}
		}
		return sb;
	}

	@Override
	public int getQuality(int position, int length)
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
