package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SettingsFrame extends JFrame
{
	private static final long serialVersionUID = 327328698365828620L;
	private final FragmentDisplay fragmentDisplay;
	private JPanel panel;
	private FragmentDisplaySettings settings;
	private Map<FragmentDisplayColor, ColorDescriptor> map;
	
	public SettingsFrame(FragmentDisplay fragmentDisplay_)
	{
		super("Fragment Display Settings");
		fragmentDisplay = fragmentDisplay_;
		map = new EnumMap<FragmentDisplayColor, ColorDescriptor>(FragmentDisplayColor.class);
		settings = fragmentDisplay.getSettings().clone();
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
		JLabel backgroundColorLabel = new JLabel();
		map.put(FragmentDisplayColor.BACKGROUND, new ColorDescriptor(FragmentDisplayColor.BACKGROUND,
			backgroundColorLabel));
		backgroundColorLabel.setOpaque(true);
		backgroundColorLabel.setBackground(settings.colors.get(FragmentDisplayColor.BACKGROUND));
		Dimension sixteen = new Dimension(16, 16);
		backgroundColorLabel.setMinimumSize(sixteen);
		backgroundColorLabel.setPreferredSize(sixteen);
		panel.add(backgroundColorLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.1;
		gbc.weightx = 0.8;
		gbc.gridy = 1;
		JLabel sequenceColorText = new JLabel("Sequence Color");
		panel.add(sequenceColorText, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.2;
		JLabel sequenceColorLabel = new JLabel();
		map.put(FragmentDisplayColor.SEQUENCE, new ColorDescriptor(FragmentDisplayColor.SEQUENCE, sequenceColorLabel));
		sequenceColorLabel.setOpaque(true);
		sequenceColorLabel.setBackground(settings.colors.get(FragmentDisplayColor.SEQUENCE));
		sequenceColorLabel.setMinimumSize(sixteen);
		sequenceColorLabel.setPreferredSize(sixteen);
		panel.add(sequenceColorLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.1;
		gbc.weightx = 0.8;
		gbc.gridy = 2;
		JLabel fragmentColorText = new JLabel("Fragment Color");
		panel.add(fragmentColorText, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.2;
		JLabel fragmentColorLabel = new JLabel();
		map.put(FragmentDisplayColor.FRAGMENT, new ColorDescriptor(FragmentDisplayColor.FRAGMENT, fragmentColorLabel));
		fragmentColorLabel.setOpaque(true);
		fragmentColorLabel.setBackground(settings.colors.get(FragmentDisplayColor.FRAGMENT));
		fragmentColorLabel.setMinimumSize(sixteen);
		fragmentColorLabel.setPreferredSize(sixteen);
		panel.add(fragmentColorLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 0.1;
		gbc.weightx = 0.8;
		gbc.gridy = 3;
		JLabel selectedColorText = new JLabel("Selected Color");
		panel.add(selectedColorText, gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 0.2;
		JLabel selectedColorLabel = new JLabel();
		map.put(FragmentDisplayColor.SELECTED, new ColorDescriptor(FragmentDisplayColor.SELECTED, selectedColorLabel));
		selectedColorLabel.setOpaque(true);
		selectedColorLabel.setBackground(settings.colors.get(FragmentDisplayColor.SELECTED));
		selectedColorLabel.setMinimumSize(sixteen);
		selectedColorLabel.setPreferredSize(sixteen);
		panel.add(selectedColorLabel, gbc);
		
		this.add(panel);
		this.setLocationRelativeTo(fragmentDisplay.frame);
		this.pack();
		this.setVisible(true);
	}
	
	private class ColorDescriptor
	{
		public final FragmentDisplayColor color;
		public final JLabel colorLabel;
		
		public ColorDescriptor(FragmentDisplayColor color_, JLabel colorLabel_)
		{
			color = color_;
			colorLabel = colorLabel_;
		}
	}
}
