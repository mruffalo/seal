package gui;

import java.awt.Color;
import java.util.EnumMap;
import java.util.Map;

public class FragmentDisplaySettings implements Cloneable
{
	public Map<FragmentDisplayColor, Color> colors;
	public int scale;
	
	public FragmentDisplaySettings()
	{
		colors = new EnumMap<FragmentDisplayColor, Color>(FragmentDisplayColor.class);
		colors.put(FragmentDisplayColor.BACKGROUND, Color.WHITE);
		colors.put(FragmentDisplayColor.SEQUENCE, Color.RED);
		colors.put(FragmentDisplayColor.FRAGMENT, Color.BLACK);
		colors.put(FragmentDisplayColor.SELECTED, Color.GREEN);
		scale = 2;
	}
	
	@Override
	public FragmentDisplaySettings clone()
	{
		try
		{
			return (FragmentDisplaySettings) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
