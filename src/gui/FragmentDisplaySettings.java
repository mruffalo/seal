package gui;

import java.awt.Color;

public class FragmentDisplaySettings implements Cloneable
{
	public Color backgroundColor = Color.WHITE;
	public Color sequenceColor = Color.RED;
	public Color fragmentColor = Color.BLACK;
	public Color selectedColor = Color.GREEN;
	public int scale = 2;
	
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
