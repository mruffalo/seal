package gui;

public enum FragmentDisplayColor
{
	BACKGROUND("Background"),
	SEQUENCE("Sequence"),
	FRAGMENT("Fragment"),
	SELECTED("Selected Fragment");
	
	private final String description;
	
	private FragmentDisplayColor(String description_)
	{
		description = description_;
	}
	
	public String getDescription()
	{
		return description;
	}
}
