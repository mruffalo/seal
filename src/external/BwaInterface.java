package external;

import io.Constants;
import io.FastaWriter;
import io.FastqWriter;
import io.SamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import external.AlignmentToolInterface.AlignmentResults;
import assembly.Fragment;

/**
 * TODO: Move some of this code into the general {@link AlignmentToolInterface}
 * class
 * 
 * @author mruffalo
 */
public class BwaInterface extends AlignmentToolInterface
{
	public static final String BWA_COMMAND = "bwa";
	public static final String INDEX_COMMAND = "index";
	public static final String ALIGN_COMMAND = "aln";
	public static final String SAM_SINGLE_END_COMMAND = "samse";
	public static final String SAM_PAIRED_END_COMMAND = "sampe";

	public static final String OUTPUT_TEMPLATE = "%s\t%d\t%d\t%d\t%d%n";

	public BwaInterface(CharSequence sequence_, List<? extends Fragment> fragments_, Options o_)
	{
		super(sequence_, fragments_, o_);
	}

	public void createIndex(File file)
	{
		String index_filename = file.getName() + ".bwt";
		o.index = new File(file.getParentFile(), index_filename);
		if (o.index.isFile())
		{
			System.out.println("Index found; skipping");
		}
		else
		{
			ProcessBuilder pb = new ProcessBuilder(BWA_COMMAND, INDEX_COMMAND,
				file.getAbsolutePath());
			pb.directory(file.getParentFile());
			try
			{
				FastaWriter.writeSequence(sequence, file);
				Process p = pb.start();
				BufferedReader stdout = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
				BufferedReader stderr = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));
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
	}

	@Override
	public void align()
	{
		System.out.print("Aligning reads...");

		for (int i = 0; i < o.reads.size(); i++)
		{
			List<String> commands = new ArrayList<String>();
			commands.add(BWA_COMMAND);
			commands.add(ALIGN_COMMAND);
			commands.add(o.genome.getAbsolutePath());
			commands.add(o.reads.get(i).reads.getAbsolutePath());
			ProcessBuilder pb = new ProcessBuilder(commands);
			pb.directory(o.genome.getParentFile());
			try
			{
				FastqWriter.writeFragments(pairedEndFragments.get(i), o.reads.get(i).reads,
					o.reads.get(i).index);
				Process p = pb.start();
				InputStream stdout = p.getInputStream();
				BufferedReader stderr = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));

				// TODO: Use a faster bulk copy method for this
				FileOutputStream w = new FileOutputStream(o.reads.get(i).aligned_reads);
				int data = -1;
				while ((data = stdout.read()) != -1)
				{
					w.write(data);
				}
				w.close();
				String line = null;
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
		System.out.println("done.");
	}

	public void convertToSamFormat()
	{
		List<String> commands = new ArrayList<String>();
		commands.add(BWA_COMMAND);
		commands.add(o.is_paired_end ? SAM_PAIRED_END_COMMAND : SAM_SINGLE_END_COMMAND);
		commands.add(o.genome.getAbsolutePath());
		for (Options.Reads r : o.reads)
		{
			commands.add(r.aligned_reads.getAbsolutePath());
		}
		for (Options.Reads r : o.reads)
		{
			commands.add(r.reads.getAbsolutePath());
		}
		commands.add(o.sam_output.getAbsolutePath());

		ProcessBuilder pb = new ProcessBuilder(commands);
		for (String arg : pb.command())
		{
			System.err.println(arg);
		}
		pb.directory(o.genome.getParentFile());
		try
		{
			Process p = pb.start();
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = null;
			FileWriter w = new FileWriter(o.sam_output);
			while ((line = stdout.readLine()) != null)
			{
				w.write(String.format("%s%n", line));
			}
			w.close();
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
	 * Don't need to read fragments; we have those already. TODO: Move this
	 * logic into {@link SamReader}
	 */
	@Override
	public AlignmentResults readAlignment()
	{
		System.out.print("Reading alignment...");
		AlignmentResults rs = new AlignmentResults();
		try
		{
			BufferedReader r = new BufferedReader(new FileReader(o.sam_output));
			BufferedWriter w = new BufferedWriter(new FileWriter(o.converted_output));
			String line = null;
			while ((line = r.readLine()) != null)
			{
				if (line.startsWith("@"))
				{
					continue;
				}
				String[] pieces = line.split("\\s+");
				if (pieces.length <= 4)
				{
					continue;
				}
				int readPosition = -1;
				Matcher m = Constants.READ_POSITION_HEADER.matcher(pieces[0]);

				if (m.matches())
				{
					readPosition = Integer.parseInt(m.group(2));
				}
				int flags = Integer.parseInt(pieces[1]);
				int alignedPosition = Integer.parseInt(pieces[3]) - 1;
				int phredProbability = Integer.parseInt(pieces[4]);
				int matePosition = Integer.parseInt(pieces[7]);
				int inferredInsertSize = Integer.parseInt(pieces[8]);

				/*
				 * TODO: Clean up this code and move it elsewhere
				 */
				int orientation = 0;
				if (((flags & 64) == 64 && inferredInsertSize > 0)
						|| ((flags & 128) == 128 && inferredInsertSize < 0))
				{
					orientation = 1;
				}
				if (((flags & 64) == 64 && inferredInsertSize < 0)
						|| ((flags & 128) == 128 && inferredInsertSize > 0))
				{
					orientation = 2;
				}
				String output = String.format(OUTPUT_TEMPLATE, pieces[0], alignedPosition,
					inferredInsertSize, orientation, phredProbability);
				w.write(output);

				if (readPosition == alignedPosition)
				{
					if (phredProbability >= phredMatchThreshold)
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
					if (phredProbability >= phredMatchThreshold)
					{
						rs.falsePositives++;
					}
					// System.out.println(line);
				}
				rs.totalFragmentsRead++;
			}
			/*
			 * If a fragment didn't appear in the output at all, count it as a
			 * false negative
			 */
			if (fragments.size() >= rs.totalFragmentsRead)
			{
				rs.falseNegatives += (fragments.size() - rs.totalFragmentsRead);
			}
			r.close();
			w.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return rs;
	}

	@Override
	public void preAlignmentProcessing()
	{
		System.out.print("Indexing genome...");
		createIndex(o.genome);
		System.out.println("done.");
	}

	@Override
	public void postAlignmentProcessing()
	{
		System.out.print("Converting output to SAM format...");
		convertToSamFormat();
		System.out.println("done.");
	}
}
