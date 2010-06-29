package assembly;

import java.util.List;

public interface SequenceAssembler
{
	public String assembleSequence(List<? extends Fragment> fragments);
}
