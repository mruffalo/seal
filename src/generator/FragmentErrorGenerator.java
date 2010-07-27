package generator;

import assembly.Fragment;
import java.util.List;

public interface FragmentErrorGenerator
{
	public List<? extends Fragment> generateErrors(List<? extends Fragment> fragments,
		String allowedCharacters);
}
