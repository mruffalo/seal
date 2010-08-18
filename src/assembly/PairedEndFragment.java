package assembly;

public class PairedEndFragment extends Fragment
{
	/**
	 * Whether this paired end read at the beginning of the original fragment
	 * (true) or at the end (false)
	 */
	protected boolean atBeginningOfFragment;
	protected int length;

	public PairedEndFragment(CharSequence sequence_, boolean atBeginningOfFragment_, int length_)
	{
		super(sequence_);
		atBeginningOfFragment = atBeginningOfFragment_;
		length = length_;
	}

	@Override
	public CharSequence getSequence()
	{
		if (atBeginningOfFragment)
		{
			return sequence.subSequence(0, length);
		}
		else
		{
			// TODO: Test this
			return sequence.subSequence(sequence.length() - length, sequence.length());
		}
	}

	@Override
	public Integer getPosition(FragmentPositionSource source)
	{
		int rawPosition = positions.get(source);
		if (atBeginningOfFragment)
		{
			return rawPosition;
		}
		else
		{
			return rawPosition + (sequence.length() - length);
		}
	}
}
