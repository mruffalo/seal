package assembly;

/**
 * 
 */
public enum FragmentPositionSource
{
	ORIGINAL_SEQUENCE("Original Sequence"),
	ASSEMBLED_SEQUENCE("Assembled Sequence");
	
	public final String guiDescription;
	
	private FragmentPositionSource(String guiDescription_)
	{
		guiDescription = guiDescription_;
	}
}
