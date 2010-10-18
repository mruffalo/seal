package external;

import java.util.List;
import java.util.Map;
import assembly.Fragment;

public class NovoalignInterface extends AlignmentToolInterface
{
	public NovoalignInterface(int index, String description, List<Integer> thresholds,
		CharSequence sequence, List<? extends Fragment> list, Options o,
		Map<String, Map<Integer, AlignmentResults>> m)
	{
		super(index, description, thresholds, sequence, list, o, m);
	}

	@Override
	public void align()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void postAlignmentProcessing()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void preAlignmentProcessing()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public AlignmentResults readAlignment(int qualityThreshold)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
