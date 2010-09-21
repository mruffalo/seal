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

	public BowtieInterface(int index, String description, CharSequence sequence,
		List<? extends Fragment> list, Options o, Map<String, AlignmentResults> m)
	{
		super(index, description, sequence, list, o, m);
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
	public AlignmentResults readAlignment()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
