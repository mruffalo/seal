package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
		FragmentDisplaySettings settings = new FragmentDisplaySettings();
		
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel backgroundColorLabel = new JLabel("Background Color");
		panel.add(backgroundColorLabel);
		this.add(panel);
		
		this.setLocationRelativeTo(fragmentDisplay.frame);
		this.pack();
		this.setVisible(true);
	}
}
