package gui;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class AboutFrame extends JFrame
{
	private static final long serialVersionUID = 8860569373371189189L;
	
	private final FragmentDisplay fragmentDisplay;
	private JPanel panel;
	
	public AboutFrame(FragmentDisplay fragmentDisplay_)
	{
		super("About");
		fragmentDisplay = fragmentDisplay_;
		
		panel = new JPanel();
		this.add(panel);
		
		this.setLocationRelativeTo(fragmentDisplay.frame);
		this.pack();
		this.setVisible(true);
	}
}
