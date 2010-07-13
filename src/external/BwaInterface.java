package external;

import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import io.FastaWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import assembly.Fragment;

public class BwaInterface
{
	public static final String BWA_COMMAND = "bwa";
	public static final String INDEX_COMMAND = "index";
	
	private String string;
	private List<Fragment> fragments;
	
	public BwaInterface(String string_, List<Fragment> fragments_)
	{
		string = string_;
		fragments = fragments_;
	}
	
	public void createIndex(File file)
	{
		ProcessBuilder pb = new ProcessBuilder(BWA_COMMAND, INDEX_COMMAND, file.getAbsolutePath());
		try
		{
			FastaWriter.writeSequence(string, file);
			Process p = pb.start();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void align()
	{
		
	}
	
	public void readAlignment()
	{
		
	}
	
	public static void main(String args[])
	{
		SequenceGenerator g = new SeqGenSingleSequenceMultipleRepeats();
		String string = g.generateSequence(100000, 0, 0);
		System.out.println(string);
	}
}
