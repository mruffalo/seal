package external.tool;

import external.AlignmentResults;
import external.AlignmentToolInterface;

import java.util.List;
import java.util.Map;

public class GsnapInterface extends AlignmentToolInterface
{
	public GsnapInterface(int index_, String description_, List<Integer> thresholds_,
			Options o_, Map<String, AlignmentResults> m_)
	{
		super(index_, description_, thresholds_, o_, m_);
	}

	@Override
	public void preAlignmentProcessing()
	{
	}

	@Override
	public void align()
	{
	}

	@Override
	public void postAlignmentProcessing()
	{
	}
}
