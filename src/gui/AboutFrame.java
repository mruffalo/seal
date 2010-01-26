package gui;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import utils.VersionInfo;

public class AboutFrame extends JFrame
{
	private static final long serialVersionUID = 8860569373371189189L;
	
	public static final String GITHUB_URL = "http://github.com/mruffalo/fragment-assembly/";
	
	private final FragmentDisplay fragmentDisplay;
	private JPanel panel;
	
	public AboutFrame(FragmentDisplay fragmentDisplay_)
	{
		super("About");
		fragmentDisplay = fragmentDisplay_;
		
		panel = new JPanel();
		
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(new JLabel("Fragment Assembly GUI"));
		panel.add(new JLabel(String.format("Version: %s", VersionInfo.SOFTWARE_VERSION)));
		panel.add(new JLabel("Author: Matthew Ruffalo <matthew.ruffalo@case.edu>"));
		panel.add(new JLabel("GitHub project page:"));
		JTextField github = new JTextField(GITHUB_URL);
		github.setEditable(false);
		panel.add(github);
		this.add(panel);
		
		this.setLocationRelativeTo(fragmentDisplay.frame);
		this.pack();
		this.setVisible(true);
	}
}
