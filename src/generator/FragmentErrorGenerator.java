package generator;

import assembly.Fragment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * A class that extends this really only needs to implement 'get the error
 * probability for this position' (which can be uniform, increase linearly,
 * increase exponentially, etc.) and 'mangle the input sequence given that error
 * probability'.
 * 
 * @author mruffalo
 */
public abstract class FragmentErrorGenerator
{
	protected final Random characterChoiceRandomizer;
	protected final Random r = new Random();
	protected final String allowedCharacters;
	protected boolean verbose;

	public FragmentErrorGenerator(String allowedCharacters_)
	{
		allowedCharacters = allowedCharacters_;
		characterChoiceRandomizer = new Random();
	}

	public abstract CharSequence generateErrors(CharSequence sequence);

	public Fragment generateErrors(Fragment fragment)
	{
		String s = fragment.toString();
		Fragment errored = new Fragment(generateErrors(s).toString());
		errored.clonePositionsAndReadQuality(fragment);
		for (int i = 0; i < errored.getSequence().length(); i++)
		{
			errored.setReadQuality(i, getQuality(i, errored.getSequence().length()));
		}
		return errored;
	}

	public List<? extends Fragment> generateErrors(List<? extends Fragment> fragments)
	{
		List<Fragment> list = new ArrayList<Fragment>(fragments.size());
		for (Fragment fragment : fragments)
		{
			list.add(generateErrors(fragment));
		}
		return list;
	}

	public abstract int getQuality(int position, int length);

	public static int phredScaleProbability(double errorProbability)
	{
		return (int) (-10 * Math.log10(errorProbability));
	}

	public void setVerboseOutput(boolean verbose_)
	{
		verbose = verbose_;
	}
}
