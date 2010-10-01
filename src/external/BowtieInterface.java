package external;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import assembly.Fragment;
import external.AlignmentToolInterface.AlignmentResults;
import external.AlignmentToolInterface.Options;

public class BowtieInterface extends AlignmentToolInterface
{
	private static final String BOWTIE_COMMAND = "bowtie";
	private static final String BOWTIE_INDEX_COMMAND = "bowtie-build";

	public BowtieInterface(int index_, String description_, List<Integer> thresholds_,
		CharSequence sequence_, List<? extends Fragment> list_, Options o_,
		Map<String, Map<Integer, AlignmentResults>> m_)
	{
		super(index_, description_, thresholds_, sequence_, list_, o_, m_);
	}

	@Override
	public void preAlignmentProcessing()
	{
		List<String> commands = new ArrayList<String>();
		commands.add(BOWTIE_INDEX_COMMAND);

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
	public AlignmentResults readAlignment(int qualityThreshold)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
