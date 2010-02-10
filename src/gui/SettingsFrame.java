package gui;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SettingsFrame extends JFrame
{
	private static final long serialVersionUID = 327328698365828620L;
	private final FragmentDisplay fragmentDisplay;
	private JPanel panel;
	
	public SettingsFrame(FragmentDisplay fragmentDisplay_)
	{
		super("Fragment Display Settings");
		fragmentDisplay = fragmentDisplay_;
		
		panel = new JPanel();
		this.add(panel);
		
		this.setLocationRelativeTo(fragmentDisplay.frame);
		this.pack();
		this.setVisible(true);
	}
}
