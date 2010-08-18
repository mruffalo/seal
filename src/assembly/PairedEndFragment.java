package assembly;

public class PairedEndFragment extends Fragment
{
	/**
	 * Whether this paired end read at the beginning of the original fragment
	 * (true) or at the end (false)
	 */
	protected boolean atBeginningOfFragment;
	protected int length;

	public PairedEndFragment(String string, boolean atBeginningOfFragment_, int length_)
	{
		super(string);
		atBeginningOfFragment = atBeginningOfFragment_;
		length_ = length;
	}

	@Override
	public CharSequence getSequence()
	{
		if (atBeginningOfFragment)
		{
			return string.subSequence(0, length);
		}
		else
		{
			// TODO: Test this
			return string.subSequence(string.length() - length, string.length());
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
			return rawPosition + (string.length() - length);
		}
	}
}
