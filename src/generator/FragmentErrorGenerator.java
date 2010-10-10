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
	private final Random characterChoiceRandomizer;
	protected final Random r = new Random();
	protected final String allowedCharacters;
	/**
	 * Used to ensure that characters are replaced with a different character;
	 * i.e. that the same character is not randomly chosen. This requires n^2
	 * space with respect to the set of allowed characters, but these alphabets
	 * are usually very small anyway (the largest considered here,
	 * {@link SequenceGenerator#AMINO_ACID_ALLOWED_CHARACTERS}, is 27 characters
	 * long).
	 */
	protected final Map<Character, String> replacements;
	protected boolean verbose;

	public FragmentErrorGenerator(String allowedCharacters_)
	{
		allowedCharacters = allowedCharacters_;
		characterChoiceRandomizer = new Random();
		replacements = new HashMap<Character, String>(allowedCharacters.length());
		for (int i = 0; i < allowedCharacters.length(); i++)
		{
			StringBuilder sb = new StringBuilder(allowedCharacters.length() - 1);
			for (int j = 0; j < i; j++)
			{
				sb.append(allowedCharacters.charAt(j));
			}
			for (int j = i + 1; j < allowedCharacters.length(); j++)
			{
				sb.append(allowedCharacters.charAt(j));
			}
			replacements.put(allowedCharacters.charAt(i), sb.toString());
		}
	}

	protected abstract double getErrorProbability(int position, int length);

	public abstract CharSequence generateErrors(CharSequence sequence);

	public Fragment generateErrors(Fragment fragment)
	{
		String s = fragment.toString();
		Fragment errored = new Fragment(generateErrors(s).toString());
		errored.clonePositionsAndReadQuality(fragment);
		for (int i = 0; i < s.length(); i++)
		{
			errored.setReadQuality(i, phredScaleProbability(getErrorProbability(i, s.length())));
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

	public char chooseRandomCharacter(char originalCharacter)
	{
		String possibilities = replacements.get(originalCharacter);
		return possibilities.charAt(characterChoiceRandomizer.nextInt(possibilities.length()));
	}

	public int phredScaleProbability(double errorProbability)
	{
		return (int) (-10 * Math.log10(errorProbability));
	}

	public void setVerboseOutput(boolean verbose_)
	{
		verbose = verbose_;
	}
}
