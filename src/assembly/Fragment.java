package assembly;

import java.util.EnumMap;
import java.util.Map;

public class Fragment
{
	public final String string;
	private Map<FragmentPositionSource, Integer> positions;
	
	public Fragment(String string_)
	{
		string = string_;
		positions = new EnumMap<FragmentPositionSource, Integer>(FragmentPositionSource.class);
	}
	
	public int getPosition(FragmentPositionSource source)
	{
		return positions.get(source);
	}
	
	public void setPosition(FragmentPositionSource source, int value)
	{
		positions.put(source, value);
	}
	
	@Override
	public String toString()
	{
		return string;
	}
}
