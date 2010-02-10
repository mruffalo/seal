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
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.1;
		gbc.weightx = 0.8;
		JLabel backgroundColorText = new JLabel("Background Color");
		panel.add(backgroundColorText, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.2;
		JLabel backgroundColor = new JLabel();
		backgroundColor.setOpaque(true);
		backgroundColor.setBackground(settings.backgroundColor);
		Dimension sixteen = new Dimension(16, 16);
		backgroundColor.setMinimumSize(sixteen);
		backgroundColor.setPreferredSize(sixteen);
		panel.add(backgroundColor, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.1;
		gbc.weightx = 0.8;
		gbc.gridy = 1;
		JLabel sequenceColorText = new JLabel("Sequence Color");
		panel.add(sequenceColorText, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.2;
		JLabel sequenceColor = new JLabel();
		sequenceColor.setOpaque(true);
		sequenceColor.setBackground(settings.sequenceColor);
		sequenceColor.setMinimumSize(sixteen);
		sequenceColor.setPreferredSize(sixteen);
		panel.add(sequenceColor, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.1;
		gbc.weightx = 0.8;
		gbc.gridy = 2;
		JLabel fragmentColorText = new JLabel("Fragment Color");
		panel.add(fragmentColorText, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.2;
		JLabel fragmentColor = new JLabel();
		fragmentColor.setOpaque(true);
		fragmentColor.setBackground(settings.fragmentColor);
		fragmentColor.setMinimumSize(sixteen);
		fragmentColor.setPreferredSize(sixteen);
		panel.add(fragmentColor, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.1;
		gbc.weightx = 0.8;
		gbc.gridy = 3;
		JLabel selectedColorText = new JLabel("Selected Color");
		panel.add(selectedColorText, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.2;
		JLabel selectedColor = new JLabel();
		selectedColor.setOpaque(true);
		selectedColor.setBackground(settings.selectedColor);
		selectedColor.setMinimumSize(sixteen);
		selectedColor.setPreferredSize(sixteen);
		panel.add(selectedColor, gbc);
		
		this.add(panel);
		this.setLocationRelativeTo(fragmentDisplay.frame);
		this.pack();
		this.setVisible(true);
	}
}
