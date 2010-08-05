package generator;

import assembly.Fragment;
import java.util.List;
import java.util.Random;

public abstract class FragmentErrorGenerator
{
	private Random characterChoiceRandomizer;

	public FragmentErrorGenerator()
	{
		characterChoiceRandomizer = new Random();
	}

	public abstract Fragment generateErrors(Fragment fragment, String allowedCharacters);

	public abstract List<? extends Fragment> generateErrors(List<? extends Fragment> fragments,
		String allowedCharacters);

	public char chooseRandomCharacter(String characters)
	{
		return characters.charAt(characterChoiceRandomizer.nextInt(characters.length()));
	}

	public int phredScaleProbability(double errorProbability)
	{
		return (int) (-10 * Math.log10(errorProbability));
	}
}
