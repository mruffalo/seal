package assembly;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Fragment implements Cloneable
{
	protected final CharSequence sequence;
	protected final Map<FragmentPositionSource, Integer> positions;
	/**
	 * Array of Phred-scaled read quality values
	 */
	protected final int[] readQuality;

	public Fragment(CharSequence sequence_)
	{
		sequence = sequence_;
		positions = new EnumMap<FragmentPositionSource, Integer>(FragmentPositionSource.class);
		readQuality = new int[getSequence().length()];
	}

	public Integer getPosition(FragmentPositionSource source)
	{
		return positions.get(source);
	}

	public void setPosition(FragmentPositionSource source, Integer value)
	{
		positions.put(source, value);
	}

	@Override
	public int hashCode()
	{
		return getSequence().hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Fragment) || o == null)
		{
			return false;
		}
		Fragment that = (Fragment) o;
		if (getSequence() == null)
		{
			return that.getSequence() == null;
		}
		return getSequence().equals(that.getSequence());
	}

	public boolean equalsWithPositions(Fragment that)
	{
		if (!getSequence().equals(that.getSequence()))
		{
			return false;
		}
		for (FragmentPositionSource source : FragmentPositionSource.values())
		{
			Integer thisPosition = this.getPosition(source);
			Integer thatPosition = that.getPosition(source);
			/*
			 * Only compare positions that are present in both fragments. If a
			 * position has not been assigned in one of the fragments, it
			 * doesn't count toward equality.
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
	 * @param length
	 *            The length of the paired end reads on either side of the
	 *            actual fragment
	 * @return
	 */
	public List<? extends Fragment> pairedEndClone(int length)
	{
		List<PairedEndFragment> list = new ArrayList<PairedEndFragment>(2);
		list.add(new PairedEndFragment(sequence, true, length));
		list.add(new PairedEndFragment(sequence, false, length));
		return list;
	}

	public static List<List<? extends Fragment>> pairedEndClone(List<? extends Fragment> list,
		int length)
	{
		List<PairedEndFragment> one = new ArrayList<PairedEndFragment>(list.size());
		List<PairedEndFragment> two = new ArrayList<PairedEndFragment>(list.size());
		for (Fragment f : list)
		{
			one.add(new PairedEndFragment(f.getSequence(), true, length));
			two.add(new PairedEndFragment(f.getSequence(), false, length));
		}
		List<List<? extends Fragment>> both = new ArrayList<List<? extends Fragment>>(2);
		both.add(one);
		both.add(two);
		return both;
	}

	/**
	 * Use {@link Fragment#getSequence()} if you can. In the event that the
	 * sequence is actually a {@link util.ropes.Rope} or something like that,
	 * don't force conversion to a String unless it's necessary.
	 */
	@Override
	public String toString()
	{
		return getSequence().toString();
	}

	public CharSequence getSequence()
	{
		return sequence;
	}

	public void clonePositions(Fragment that)
	{
		for (FragmentPositionSource source : FragmentPositionSource.values())
		{
			this.setPosition(source, that.getPosition(source));
		}
	}
}
