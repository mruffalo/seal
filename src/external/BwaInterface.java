package external;

import java.io.File;
import java.util.List;
import assembly.Fragment;

public class BwaInterface
{
	private String string;
	private List<Fragment> fragments;
	
	public BwaInterface(String string_, List<Fragment> fragments_)
	{
		string = string_;
		fragments = fragments_;
	}
	
	public void createIndex(File file)
	{
		
	}
	
	public void align()
	{
		
	}
	
	public void readAlignment()
	{
		
	}
}
