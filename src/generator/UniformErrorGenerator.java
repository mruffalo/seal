package generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import assembly.Fragment;

public class UniformErrorGenerator extends FragmentErrorGenerator
{
	private double errorProbability;

	public void setErrorProbability(double errorProbability_)
	{
		if (errorProbability_ <= 1.0 && errorProbability_ >= 0.0)
		{
			errorProbability = errorProbability_;
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
	public List<? extends Fragment> generateErrors(List<? extends Fragment> fragments,
		String allowedCharacters)
	{
		Random r = new Random();
		int quality = phredScaleProbability(errorProbability);
		List<Fragment> list = new ArrayList<Fragment>(fragments.size());
		for (Fragment orig : fragments)
		{
			String s = orig.toString();
			StringBuilder sb = new StringBuilder();
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
			Fragment errored = new Fragment(sb.toString());
			errored.clonePositions(orig);
			for (int i = 0; i < s.length(); i++)
			{
				errored.setReadQuality(i, quality);
			}
			list.add(errored);
		}
		return list;
	}
}
