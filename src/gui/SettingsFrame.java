package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SettingsFrame extends JFrame
{
	private static final long serialVersionUID = 327328698365828620L;
	private static final String CHOOSE_BUTTON_TEXT = "Choose...";
	private final FragmentDisplay fragmentDisplay;
	private JPanel panel;
	private FragmentDisplaySettings settings;
	private Map<FragmentDisplayColor, JLabel> colorLabelMap;
	
	public SettingsFrame(FragmentDisplay fragmentDisplay_)
	{
		super("Fragment Display Settings");
		fragmentDisplay = fragmentDisplay_;
		colorLabelMap = new EnumMap<FragmentDisplayColor, JLabel>(FragmentDisplayColor.class);
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
		colorLabelMap.put(FragmentDisplayColor.BACKGROUND, backgroundColorLabel);
		backgroundColorLabel.setOpaque(true);
		backgroundColorLabel.setBackground(settings.colors.get(FragmentDisplayColor.BACKGROUND));
		Dimension sixteen = new Dimension(16, 16);
		backgroundColorLabel.setMinimumSize(sixteen);
		backgroundColorLabel.setPreferredSize(sixteen);
		panel.add(backgroundColorLabel, gbc);
		
		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.NONE;
		JButton backgroundColorButton = new JButton(CHOOSE_BUTTON_TEXT);
		backgroundColorButton.addActionListener(new ChooseColorButtonActionListener(FragmentDisplayColor.BACKGROUND));
		panel.add(backgroundColorButton, gbc);
		
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
		colorLabelMap.put(FragmentDisplayColor.SEQUENCE, sequenceColorLabel);
		sequenceColorLabel.setOpaque(true);
		sequenceColorLabel.setBackground(settings.colors.get(FragmentDisplayColor.SEQUENCE));
		sequenceColorLabel.setMinimumSize(sixteen);
		sequenceColorLabel.setPreferredSize(sixteen);
		panel.add(sequenceColorLabel, gbc);
		
		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.NONE;
		JButton sequenceColorButton = new JButton(CHOOSE_BUTTON_TEXT);
		sequenceColorButton.addActionListener(new ChooseColorButtonActionListener(FragmentDisplayColor.SEQUENCE));
		panel.add(sequenceColorButton, gbc);
		
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
		colorLabelMap.put(FragmentDisplayColor.FRAGMENT, fragmentColorLabel);
		fragmentColorLabel.setOpaque(true);
		fragmentColorLabel.setBackground(settings.colors.get(FragmentDisplayColor.FRAGMENT));
		fragmentColorLabel.setMinimumSize(sixteen);
		fragmentColorLabel.setPreferredSize(sixteen);
		panel.add(fragmentColorLabel, gbc);
		
		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.NONE;
		JButton fragmentColorButton = new JButton(CHOOSE_BUTTON_TEXT);
		fragmentColorButton.addActionListener(new ChooseColorButtonActionListener(FragmentDisplayColor.FRAGMENT));
		panel.add(fragmentColorButton, gbc);
		
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
		colorLabelMap.put(FragmentDisplayColor.SELECTED, selectedColorLabel);
		selectedColorLabel.setOpaque(true);
		selectedColorLabel.setBackground(settings.colors.get(FragmentDisplayColor.SELECTED));
		selectedColorLabel.setMinimumSize(sixteen);
		selectedColorLabel.setPreferredSize(sixteen);
		panel.add(selectedColorLabel, gbc);
		
		gbc.gridx = 2;
		gbc.fill = GridBagConstraints.NONE;
		JButton selectedColorButton = new JButton(CHOOSE_BUTTON_TEXT);
		selectedColorButton.addActionListener(new ChooseColorButtonActionListener(FragmentDisplayColor.SELECTED));
		panel.add(selectedColorButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridy = 4;
		gbc.gridwidth = 3;
		gbc.weightx = 1.0;
		gbc.insets = new Insets(5, 0, 0, 0);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panel.add(createOkCancelButtonsPanel(), gbc);
		
		this.add(panel);
		this.setLocationRelativeTo(fragmentDisplay.frame);
		this.pack();
		this.setVisible(true);
	}
	
	private JPanel createOkCancelButtonsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.add(Box.createHorizontalGlue());
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new OkButtonActionListener());
		panel.add(okButton);
		
		panel.add(Box.createHorizontalStrut(5));
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelButtonActionListener());
		panel.add(cancelButton);
		
		return panel;
	}
	
	private void setColor(FragmentDisplayColor fragmentDisplayColor, Color color)
	{
		settings.colors.put(fragmentDisplayColor, color);
		colorLabelMap.get(fragmentDisplayColor).setBackground(color);
	}
	
	private class OkButtonActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			fragmentDisplay.setSettings(settings);
			SettingsFrame.this.dispose();
		}
	}
	
	private class CancelButtonActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			SettingsFrame.this.dispose();
		}
	}
	
	private class ChooseColorButtonActionListener implements ActionListener
	{
		private final FragmentDisplayColor fdc;
		
		public ChooseColorButtonActionListener(FragmentDisplayColor fdc_)
		{
			fdc = fdc_;
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			String title = String.format("Choose %s Color", fdc.getDescription());
			Color newColor = JColorChooser.showDialog(SettingsFrame.this, title, settings.colors.get(fdc));
			setColor(fdc, newColor);
		}
	}
}
