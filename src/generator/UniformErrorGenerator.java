package generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import assembly.Fragment;

public class UniformErrorGenerator extends FragmentErrorGenerator
{
	private double errorProbability;
	private int phredScaledErrorProbability;
	private Random r = new Random();

	public UniformErrorGenerator(String allowedCharacters_, double errorProbability_)
	{
		super(allowedCharacters_);
		errorProbability = errorProbability_;
	}

	public void setErrorProbability(double errorProbability_)
	{
		if (errorProbability_ <= 1.0 && errorProbability_ >= 0.0)
		{
			errorProbability = errorProbability_;
			phredScaledErrorProbability = phredScaleProbability(errorProbability);
		}
		else
		{
			// TODO: be nicer about this :)
			throw new IllegalArgumentException("error probability must be >= 0.0 and <= 1.0");
		}
	}

	public double getErrorProbability()
	{
		return errorProbability;
	}

	@Override
	public List<? extends Fragment> generateErrors(List<? extends Fragment> fragments)
	{
		r = new Random();
		List<Fragment> list = new ArrayList<Fragment>(fragments.size());
		for (Fragment fragment : fragments)
		{
			list.add(generateErrors(fragment));
		}
		return list;
	}

	@Override
	public CharSequence generateErrors(CharSequence s)
	{
		StringBuilder sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++)
		{
			if (r.nextDouble() <= errorProbability)
			{
				sb.append(chooseRandomCharacter(allowedCharacters));
			}
			else
			{
				sb.append(s.charAt(i));
			}
		}
		return sb;
	}

	@Override
	public Fragment generateErrors(Fragment fragment)
	{
		String s = fragment.toString();
		Fragment errored = new Fragment(generateErrors(s).toString());
		errored.clonePositions(fragment);
		for (int i = 0; i < s.length(); i++)
		{
			errored.setReadQuality(i, phredScaledErrorProbability);
		}
		return errored;
	}
}
