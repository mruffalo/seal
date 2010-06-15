package assembly;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Fragment
{
	public final String string;
	private final Map<FragmentPositionSource, Integer> positions;
	private final int[] readQuality;
	
	public Fragment(String string_)
	{
		string = string_;
		positions = new EnumMap<FragmentPositionSource, Integer>(FragmentPositionSource.class);
		readQuality = new int[string.length()];
	}
	
	public Integer getPosition(FragmentPositionSource source)
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
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Fragment) || o == null)
		{
			return false;
		}
		Fragment that = (Fragment) o;
		if (string == null)
		{
			return that.string == null;
		}
		return string.equals(that.string);
	}
	
	public boolean equalsWithPositions(Fragment that)
	{
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
	
	public int getReadQuality(int index)
	{
		return readQuality[index];
	}
	
	public void setReadQuality(int index, int quality)
	{
		readQuality[index] = quality;
	}
	
	/**
	 * TODO: Determine parameters for this method
	 * 
	 * @return
	 */
	public List<Fragment> pairedEndClone()
	{
		List<Fragment> list = new ArrayList<Fragment>(2);
		// TODO: this
		return list;
	}
	
	@Override
	public String toString()
	{
		return string;
	}
}
