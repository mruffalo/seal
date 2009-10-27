package gui;

import assembly.Fragment;
import assembly.FragmentPositionSource;
import generator.Fragmentizer;
import java.util.List;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import javax.swing.*;

public class FragmentDisplay
{
	static
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
	}
	
	JFrame frame;
	Image image;
	
	public FragmentDisplay(Image image_)
	{
		image = image_;
		frame = new JFrame("Fragment Display");
		// frame.setBounds(25, 25, 320, 320);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		System.out.println("Adding new ImagePanel");
		frame.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridheight = 1;
		constraints.gridwidth = 1;
		constraints.ipadx = constraints.ipady = 2;
		frame.getContentPane().add(new ImagePanel(image_), constraints);
		constraints.gridy = 1;
		frame.getContentPane().add(new ImagePanel(image_), constraints);
		constraints = new GridBagConstraints();
		constraints.ipadx = constraints.ipady = 2;
		constraints.gridheight = 2;
		constraints.gridx = 1;
		frame.getContentPane().add(new ImagePanel(image_), constraints);
		// frame.getContentPane().add(new JLabel("Test"));
		frame.pack();
		frame.setVisible(true);
	}
	
	public static Image getFragmentGroupImage(String origSequence, List<List<Fragment>> fragmentGroups,
		FragmentPositionSource source)
	{
		BufferedImage image = new BufferedImage(origSequence.length(), fragmentGroups.size() * 2 + 1,
			BufferedImage.TYPE_INT_ARGB);
		System.out.printf("Image height: %d%n", image.getHeight());
		System.out.printf("Image width: %d%n", image.getWidth());
		
		Graphics2D g2d = image.createGraphics();
		Color red = new Color(255, 0, 0, 255);
		g2d.setColor(red);
		g2d.fill(new Rectangle2D.Float(0, 0, origSequence.length(), 1));
		g2d.dispose();
		
		int i = 0;
		for (List<Fragment> list : fragmentGroups)
		{
			for (Fragment fragment : list)
			{
				g2d = image.createGraphics();
				// Make all filled pixels transparent
				Color black = new Color(0, 0, 0, 255);
				g2d.setColor(black);
				// g2d.setComposite(AlphaComposite.Src);
				g2d.fill(new Rectangle2D.Float(fragment.getPosition(source), (i + 1) * 2, fragment.string.length(), 1));
				g2d.dispose();
			}
			i++;
		}
		
		return image;
	}
	
	/**
	 * XXX: Temporary
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		if (args.length < 4)
		{
			System.err.printf("*** Usage: %s string n k kTolerance", FragmentDisplay.class.getCanonicalName());
			System.exit(1);
		}
		String string = args[0];
		int n = Integer.parseInt(args[1]);
		int k = Integer.parseInt(args[2]);
		int kTolerance = Integer.parseInt(args[3]);
		FragmentPositionSource source = FragmentPositionSource.ORIGINAL_SEQUENCE;
		List<Fragment> fragments = Fragmentizer.fragmentizeForShotgun(string, n, k, kTolerance);
		for (Fragment fragment : fragments)
		{
			System.out.printf("%5d: %s%n", fragment.getPosition(source), fragment.string);
		}
		System.out.println();
		System.out.println(string);
		List<List<Fragment>> grouped = Fragmentizer.groupByLine(fragments, source);
		for (List<Fragment> list : grouped)
		{
			int begin = 0;
			for (Fragment fragment : list)
			{
				for (int i = 0; i < fragment.getPosition(source) - begin; i++)
				{
					System.out.print(" ");
				}
				System.out.print(fragment.string);
				begin = fragment.getPosition(source) + fragment.string.length();
			}
			System.out.println();
		}
		FragmentDisplay display = new FragmentDisplay(getFragmentGroupImage(string, grouped,
			FragmentPositionSource.ORIGINAL_SEQUENCE));
	}
}
