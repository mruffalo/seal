package assembly;

public class PairedEndFragment extends Fragment
{
	/**
	 * Whether this paired end read at the beginning of the original fragment (true) or at the end
	 * (false)
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
	public String getString()
	{
		if (atBeginningOfFragment)
		{
			return string.substring(0, length);
		}
		else
		{
			return string.substring(string.length() - length);
		}
	}
}
