package generator;

import java.util.ArrayList;
import java.util.List;
import assembly.Fragment;

public class FragmentFilter
{
	private SequenceFilter f;

	public FragmentFilter(SequenceFilter filter)
	{
		f = filter;
	}

	/**
	 * TODO: Fix this
	 * 
	 * @param fragments
	 * @return
	 */
	public List<? extends Fragment> generateErrors(List<? extends Fragment> fragments)
	{
		List<Fragment> list = new ArrayList<Fragment>(fragments.size());
		for (Fragment fragment : fragments)
		{
			list.add(new Fragment(f.filter(fragment.getSequence())));
		}
		return list;
	}

}
