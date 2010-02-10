package gui;

import java.awt.Color;
import java.awt.Dimension;
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
		FragmentDisplaySettings settings = fragmentDisplay.getSettings().clone();
		panel = new JPanel();
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel backgroundColorText = new JLabel("Background Color");
		panel.add(backgroundColorText);
		
		gbc.gridx = 1;
		JLabel backgroundColor = new JLabel();
		backgroundColor.setOpaque(true);
		backgroundColor.setBackground(settings.backgroundColor);
		Dimension sixteen = new Dimension(16, 16);
		backgroundColor.setMinimumSize(sixteen);
		backgroundColor.setPreferredSize(sixteen);
		panel.add(backgroundColor);
		
		this.add(panel);
		this.setLocationRelativeTo(fragmentDisplay.frame);
		this.pack();
		this.setVisible(true);
	}
}
