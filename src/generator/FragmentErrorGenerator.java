package generator;

import assembly.Fragment;
import java.util.List;
import java.util.Random;

public abstract class FragmentErrorGenerator
{
	private Random characterChoiceRandomizer;
	protected String allowedCharacters;

	public FragmentErrorGenerator(String allowedCharacters_)
	{
		allowedCharacters = allowedCharacters_;
		characterChoiceRandomizer = new Random();
	}

	public abstract CharSequence generateErrors(CharSequence sequence);

	public abstract Fragment generateErrors(Fragment fragment);

	public abstract List<? extends Fragment> generateErrors(List<? extends Fragment> fragments);

	public char chooseRandomCharacter(String characters)
	{
		return characters.charAt(characterChoiceRandomizer.nextInt(characters.length()));
	}

	public int phredScaleProbability(double errorProbability)
	{
		return (int) (-10 * Math.log10(errorProbability));
	}
}
