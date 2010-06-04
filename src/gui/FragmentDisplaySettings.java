package gui;

import java.awt.Color;
import java.util.EnumMap;

public class FragmentDisplaySettings implements Cloneable
{
	public EnumMap<FragmentDisplayColor, Color> colors;
	public int scale;
	
	public FragmentDisplaySettings()
	{
		colors = new EnumMap<FragmentDisplayColor, Color>(FragmentDisplayColor.class);
		colors.put(FragmentDisplayColor.BACKGROUND, Color.BLACK);
		colors.put(FragmentDisplayColor.SEQUENCE, Color.RED);
		colors.put(FragmentDisplayColor.FRAGMENT, Color.DARK_GRAY);
		colors.put(FragmentDisplayColor.SELECTED, Color.GREEN);
		scale = 2;
	}
	
	@Override
	public FragmentDisplaySettings clone()
	{
		try
		{
			FragmentDisplaySettings fds = (FragmentDisplaySettings) super.clone();
			fds.colors = colors.clone();
			return fds;
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
