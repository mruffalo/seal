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
	public int hashCode()
	{
		return string.hashCode();
	}
	
	/**
	 * <code>equals()</code> can be more strict than <code>hashCode()</code> without violating the
	 * contract of these two methods. TODO: Examine this more thoroughly
	 */
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Fragment) || o == null)
		{
			return false;
		}
		Fragment that = (Fragment) o;
		if (!string.equals(that.string))
		{
			return false;
		}
		for (FragmentPositionSource source : FragmentPositionSource.values())
		{
			Integer thisPosition = this.getPosition(source);
			Integer thatPosition = that.getPosition(source);
			/*
			 * Only compare positions that are present in both fragments. If a position has not been
			 * assigned in one of the fragments, it doesn't count toward equality.
			 */
			if (thisPosition != null && thatPosition != null && thisPosition != thatPosition)
			{
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString()
	{
		return string;
	}
}
