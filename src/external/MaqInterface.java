package external;

import io.Constants;
import io.FastaWriter;
import io.FastqWriter;
import io.SamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import external.AlignmentToolInterface.AlignmentResults;
import external.AlignmentToolInterface.Options;
import assembly.Fragment;

/**
 * TODO: Clean up local variables vs. method parameters
 * 
 * @author mruffalo
 */
public class MaqInterface extends AlignmentToolInterface
{
	public static final String MAQ_COMMAND = "maq";
	public static final String FASTQ_TO_BFQ_COMMAND = "fastq2bfq";
	public static final String FASTA_TO_BFA_COMMAND = "fasta2bfa";
	public static final String ALIGN_COMMAND = "map";
	public static final String ASSEMBLE_COMMAND = "assemble";
	public static final String VIEW_ALIGNMENT_COMMAND = "mapview";

	public MaqInterface(int index_, CharSequence sequence_, List<? extends Fragment> fragments_,
		Options o_, Map<Class<? extends AlignmentToolInterface>, AlignmentResults> m_)
	{
		super(index_, sequence_, fragments_, o_, m_);
	}

	@Override
	public void align()
	{
		System.out.print("Aligning reads...");

		List<String> commands = new ArrayList<String>();
		commands.add(MAQ_COMMAND);
		commands.add(ALIGN_COMMAND);
		commands.add(o.raw_output.getAbsolutePath());
		commands.add(o.binary_genome.getAbsolutePath());
		for (Options.Reads r : o.reads)
		{
			commands.add(r.binary_reads.getAbsolutePath());
		}
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(o.genome.getParentFile());
		try
		{
			FastqWriter.writeFragments(fragments, o.reads.get(0).reads, 0);
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
	 * TODO: Duplicate much less code here and in
	 * {@link #convertFastqToBfq(File, File)}
	 * 
	 * @param genome
	 * @param binary_genome
	 */
	public void convertGenomeToBfa()
	{
		List<String> commands = new ArrayList<String>();
		commands.add(MAQ_COMMAND);
		commands.add(FASTA_TO_BFA_COMMAND);
		commands.add(o.genome.getAbsolutePath());
		commands.add(o.binary_genome.getAbsolutePath());
		for (String arg : commands)
		{
			System.err.println(arg);
		}
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(o.genome.getParentFile());
		try
		{
			FastaWriter.writeSequence(sequence, o.genome);
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
			{
				System.err.println(line);
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

	/**
	 * TODO: Duplicate much less code here and in
	 * {@link #convertFastaToBfa(File, File)}
	 * 
	 * @param reads
	 * @param binary_reads
	 */
	public void convertReadsToBfq()
	{
		for (Options.Reads r : o.reads)
		{
			List<String> commands = new ArrayList<String>();
			commands.add(MAQ_COMMAND);
			commands.add(FASTQ_TO_BFQ_COMMAND);
			commands.add(r.reads.getAbsolutePath());
			commands.add(r.binary_reads.getAbsolutePath());
			for (String arg : commands)
			{
				System.err.println(arg);
			}
			for (String arg : commands)
			{
				System.err.println(arg);
			}
			ProcessBuilder pb = new ProcessBuilder(commands);
			pb.directory(r.reads.getParentFile());
			try
			{
				Process p = pb.start();
				BufferedReader stdout = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
				BufferedReader stderr = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));
				String line = null;
				while ((line = stdout.readLine()) != null)
				{
					System.err.println(line);
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
	}

	/**
	 * Don't need to read fragments; we have those already. TODO: Move this
	 * logic into {@link SamReader}
	 */
	@Override
	public AlignmentResults readAlignment()
	{
		System.out.print("Reading alignment...");
		AlignmentResults rs = new AlignmentResults();
		List<String> commands = new ArrayList<String>();
		commands.add(MAQ_COMMAND);
		commands.add(VIEW_ALIGNMENT_COMMAND);
		commands.add(o.raw_output.getAbsolutePath());
		for (String arg : commands)
		{
			System.err.println(arg);
		}
		ProcessBuilder pb = new ProcessBuilder(commands);
		pb.directory(o.genome.getParentFile());
		try
		{
			FastaWriter.writeSequence(sequence, o.genome);
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			while ((line = stdout.readLine()) != null)
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
				String fragmentIdentifier = pieces[0];
				Matcher m = Constants.READ_POSITION_HEADER.matcher(fragmentIdentifier);
				if (m.matches())
				{
					readPosition = Integer.parseInt(m.group(2));
				}
				int alignedPosition = Integer.parseInt(pieces[2]) - 1;
				int phredProbability = Integer.parseInt(pieces[6]);
				if (readPosition == alignedPosition)
				{
					if (phredProbability >= o.phred_match_threshold)
					{
						rs.truePositives++;
					}
					else
					{
						rs.falseNegatives++;
					}
				}
				else
				{
					if (phredProbability >= o.phred_match_threshold)
					{
						rs.falsePositives++;
					}
					// System.out.println(line);
				}
				totalMappedFragments.add(fragmentIdentifier);
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
		return rs;
	}

	@Override
	public void preAlignmentProcessing()
	{
		System.out.print("Converting FASTQ output to BFQ...");
		convertReadsToBfq();
		System.out.println("done.");
		System.out.print("Converting FASTA genome to BFA...");
		convertGenomeToBfa();
		System.out.println("done.");
	}

	@Override
	public void postAlignmentProcessing()
	{
	}
}
