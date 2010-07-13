package external;

import generator.Fragmentizer;
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
	
	private String sequence;
	private List<Fragment> fragments;
	
	public BwaInterface(String string_, List<Fragment> fragments_)
	{
		sequence = string_;
		fragments = fragments_;
	}
	
	public void createIndex(File file)
	{
		ProcessBuilder pb = new ProcessBuilder(BWA_COMMAND, INDEX_COMMAND, file.getAbsolutePath());
		try
		{
			FastaWriter.writeSequence(sequence, file);
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
		String sequence = g.generateSequence(100000, 0, 0);
		Fragmentizer.Options o = new Fragmentizer.Options();
		o.k = 100;
		o.n = 1000;
		o.ksd = 10;
		List<Fragment> list = Fragmentizer.fragmentizeForShotgun(sequence, o);
		BwaInterface b = new BwaInterface(sequence, list);
		b.createIndex(new File("sequence.fasta"));
		b.align();
		b.readAlignment();
	}
}
