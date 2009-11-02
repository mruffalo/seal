package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import assembly.Fragment;
import assembly.FragmentPositionSource;

public class ImagePanel extends JPanel
{
	/**
	 * Generated by Eclipse
	 */
	private static final long serialVersionUID = 3006769532505931833L;
	
	private Image img;
	
	public ImagePanel(String img)
	{
		this(new ImageIcon(img).getImage());
	}
	
	public ImagePanel(Image img)
	{
		this.img = img;
		Dimension size = new Dimension(img.getWidth(null), img.getHeight(null));
		setPreferredSize(size);
		setMinimumSize(size);
		setMaximumSize(size);
		setSize(size);
		setLayout(null);
	}
	
	public void paintComponent(Graphics g)
	{
		g.drawImage(img, 0, 0, null);
	}
	
	public void setImage(Image image)
	{
		img = image;
	}
	
	public static Image getFragmentGroupImage(String sequence, List<List<Fragment>> fragmentGroups, Fragment selected,
		FragmentPositionSource source)
	{
		BufferedImage image = new BufferedImage(sequence.length(), fragmentGroups.size() * 2 + 1,
			BufferedImage.TYPE_INT_ARGB);
		System.out.printf("Image height: %d%n", image.getHeight());
		System.out.printf("Image width: %d%n", image.getWidth());
		
		Graphics2D g2d = image.createGraphics();
		Color red = new Color(255, 0, 0, 255);
		g2d.setColor(red);
		g2d.fill(new Rectangle2D.Float(0, 0, sequence.length(), 1));
		g2d.dispose();
		
		int i = 0;
		for (List<Fragment> list : fragmentGroups)
		{
			for (Fragment fragment : list)
			{
				g2d = image.createGraphics();
				Color color = fragment.equals(selected) ? new Color(0, 255, 0, 255) : new Color(0, 0, 0, 255);
				g2d.setColor(color);
				
				g2d.fill(new Rectangle2D.Float(fragment.getPosition(source), (i + 1) * 2, fragment.string.length(), 1));
				g2d.dispose();
			}
			i++;
		}
		
		return image;
	}
}
