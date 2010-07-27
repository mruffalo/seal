package generator;

import java.util.List;
import assembly.Fragment;

public class UniformErrorGenerator implements FragmentErrorGenerator
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
		// TODO Auto-generated method stub
		return null;
	}
}
