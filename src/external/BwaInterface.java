package external;

import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import io.FastaWriter;
import io.SamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import assembly.Fragment;

/**
 * TODO:
 * <ul>
 * <li>Generalize this code into an (actual Java) interface</li>
 * <li>Use the FASTQ format instead</li>
 * </ul>
 * 
 * @author mruffalo
 */
public class BwaInterface
{
	public static final String BWA_COMMAND = "bwa";
	public static final String INDEX_COMMAND = "index";
	public static final String ALIGN_COMMAND = "aln";
	public static final String SAM_SINGLE_END_COMMAND = "samse";
	public static final String SAM_PAIRED_END_COMMAND = "sampe";
	
	public static final int PHRED_MATCH_THRESHOLD = 35;
	
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
	
	public void align(File genome, File reads, File binary_output)
	{
		ProcessBuilder pb = new ProcessBuilder(BWA_COMMAND, ALIGN_COMMAND, "-f",
			binary_output.getAbsolutePath(), genome.getAbsolutePath(), reads.getAbsolutePath());
		pb.directory(genome.getParentFile());
		try
		{
			FastaWriter.writeFragments(fragments, reads);
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
	
	public void convertToSamFormat(File genome, File binary_output, File reads, File sam_output)
	{
		ProcessBuilder pb = new ProcessBuilder(BWA_COMMAND, SAM_SINGLE_END_COMMAND,
			genome.getAbsolutePath(), binary_output.getAbsolutePath(), reads.getAbsolutePath(),
			sam_output.getAbsolutePath());
		for (String arg : pb.command())
		{
			System.err.println(arg);
		}
		pb.directory(genome.getParentFile());
		try
		{
			FastaWriter.writeFragments(fragments, reads);
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			FileWriter w = new FileWriter(sam_output);
			while ((line = stdout.readLine()) != null)
			{
				w.write(String.format("%s%n", line));
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
	
	/**
	 * Don't need to read fragments; we have those already. TODO: Move this logic into
	 * {@link SamReader}
	 * 
	 * @param sam_output
	 */
	public void readAlignment(File sam_output)
	{
		int matches = 0;
		try
		{
			BufferedReader r = new BufferedReader(new FileReader(sam_output));
			String line = null;
			while ((line = r.readLine()) != null)
			{
				if (line.startsWith("@"))
				{
					continue;
				}
				String[] pieces = line.split("\\s+");
				if (pieces.length <= 3)
				{
					continue;
				}
				int readPosition = Integer.parseInt(pieces[0].split(":")[1]);
				int alignedPosition = Integer.parseInt(pieces[3]) - 1;
				int phredProbability = Integer.parseInt(pieces[4]);
				if (readPosition == alignedPosition && phredProbability >= PHRED_MATCH_THRESHOLD)
				{
					matches++;
				}
				else
				{
					System.out.println("Mismatch:");
					System.out.printf("\t%s%n", line);
				}
			}
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.printf("%d matches%n", matches);
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
		File genome = new File(path, "genome.fasta");
		File reads = new File(path, "fragments.fasta");
		File binary_output = new File(path, "alignment.sai");
		File sam_output = new File(path, "alignment.sam");
		path.mkdirs();
		b.createIndex(genome);
		b.align(genome, reads, binary_output);
		b.convertToSamFormat(genome, binary_output, reads, sam_output);
		b.readAlignment(sam_output);
	}
}
