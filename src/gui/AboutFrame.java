package gui;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class AboutFrame extends JFrame
{
	private static final long serialVersionUID = 8860569373371189189L;
	
	/**
	 * TODO: Move this somewhere else
	 */
	public final static String SOFTWARE_VERSION = "1.0";
	
	private final FragmentDisplay fragmentDisplay;
	private JPanel panel;
	
	public AboutFrame(FragmentDisplay fragmentDisplay_)
	{
		super("About");
		fragmentDisplay = fragmentDisplay_;
		
		panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(new JLabel("Fragment Assembly GUI"));
		panel.add(new JLabel(String.format("Version %s", SOFTWARE_VERSION)));
		panel.add(new JLabel("Author: Matthew Ruffalo <matthew.ruffalo@case.edu>"));
		this.add(panel);
		
		this.setLocationRelativeTo(fragmentDisplay.frame);
		this.pack();
		this.setVisible(true);
	}
}
