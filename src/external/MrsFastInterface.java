package external;

import generator.Fragmentizer;
import generator.SeqGenSingleSequenceMultipleRepeats;
import generator.SequenceGenerator;
import generator.UniformErrorGenerator;
import io.Constants;
import io.FastaWriter;
import io.FastqWriter;
import io.SamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import assembly.Fragment;

public class MrsFastInterface extends AlignmentToolInterface
{
	public static final String SEQ_OPTION = "--seq";
	public static final String MRSFAST_COMMAND = "mrsfast";
	public static final String INDEX_COMMAND = "--index";
	public static final String SEARCH_COMMAND = "--search";

	private CharSequence sequence;
	private List<? extends Fragment> fragments;

	private File genome;
	private File reads;
	private File binary_output;
	private File sam_output;

	public MrsFastInterface(CharSequence string_, List<? extends Fragment> fragments_,
		File genome_, File reads_, File binary_output_, File sam_output_)
	{
		super();
		sequence = string_;
		fragments = fragments_;
		genome = genome_;
		reads = reads_;
		binary_output = binary_output_;
		sam_output = sam_output_;
	}

	public void createIndex(File file)
	{
		ProcessBuilder pb = new ProcessBuilder(MRSFAST_COMMAND, INDEX_COMMAND,
			file.getAbsolutePath());
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
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void align()
	{
		System.out.print("Aligning reads...");
		ProcessBuilder pb = new ProcessBuilder(MRSFAST_COMMAND, SEARCH_COMMAND,
			genome.getAbsolutePath(), SEQ_OPTION, reads.getAbsolutePath(), "-o",
			sam_output.getAbsolutePath());
		pb.directory(genome.getParentFile());
		try
		{
			FastqWriter.writeFragments(fragments, reads);
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
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		System.out.println("done.");
	}

	/**
	 * Don't need to read fragments; we have those already. TODO: Move this
	 * logic into {@link SamReader}
	 */
	@Override
	public void readAlignment()
	{
		System.out.print("Reading alignment...");
		int matches = 0;
		int total = 0;
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
				int readPosition = -1;
				Matcher m = Constants.READ_POSITION_HEADER.matcher(pieces[0]);
				if (m.matches())
				{
					readPosition = Integer.parseInt(m.group(2));
				}
				int alignedPosition = Integer.parseInt(pieces[3]) - 1;
				int phredProbability = Integer.parseInt(pieces[4]);
				if (readPosition == alignedPosition && phredProbability >= PHRED_MATCH_THRESHOLD)
				{
					matches++;
				}
				else
				{
					System.out.println(line);
				}
				total++;
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.out.println("done.");
		System.out.printf("%d matches / %d total fragments read (%f)%n", matches, total,
			(double) matches / (double) total);
	}

	@Override
	public void preAlignmentProcessing()
	{
		System.out.print("Indexing genome...");
		createIndex(genome);
		System.out.println("done.");
	}

	@Override
	public void postAlignmentProcessing()
	{
	}
}
