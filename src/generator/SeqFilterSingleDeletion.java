package generator;

import java.util.Random;
import util.ropes.Rope;
import util.ropes.RopeBuilder;

public class SeqFilterSingleDeletion implements SequenceFilter
{
	private Options o;
	/**
	 * This stores the position of the deletion after {@link filter} has been
	 * called.
	 */
	private int deletePosition;

	public static class Options
	{
		public int length;
	}

	public SeqFilterSingleDeletion(Options o_)
	{
		o = o_;
	}

	/**
	 * @return the position of the deletion (after {@link filter} has been
	 *         called)
	 */
	public int getDeletePosition()
	{
		return deletePosition;
	}

	@Override
	public CharSequence filter(CharSequence input)
	{
		int range = input.length() - o.length;
		Rope r = RopeBuilder.build(input);
		Random random = new Random();
		int deletePosition = random.nextInt(range);

		return null;
	}
}
