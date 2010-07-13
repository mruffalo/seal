package external;

import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import io.FastaWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
		pb.directory(file.getParentFile());
		try
		{
			FastaWriter.writeSequence(sequence, file);
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
			{
				System.out.println(line);
			}
			while ((line = stderr.readLine()) != null)
			{
				System.err.println(line);
			}
			p.waitFor();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InterruptedException e)
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
		File path = new File("data");
		path.mkdirs();
		b.createIndex(new File(path, "sequence.fasta"));
		b.align();
		b.readAlignment();
	}
}
