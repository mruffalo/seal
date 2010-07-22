package generator;

import assembly.Fragment;
import java.util.List;

public interface FragmentErrorGenerator
{
	public void generateErrors(List<Fragment> fragments, String allowedCharacters);
}
