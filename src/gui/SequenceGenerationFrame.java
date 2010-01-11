package gui;

import java.awt.*;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SequenceGenerationFrame extends JFrame
{
	/**
	 * Generated
	 */
	private static final long serialVersionUID = 2041048037575448527L;
	
	private JPanel panel;
	private JTextField field;
	
	public SequenceGenerationFrame()
	{
		panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(getCharactersPanel(), gbc);
		
		gbc.gridx = 1;
		panel.add(getSettingsPanel(), gbc);
		
		ButtonGroup group = new ButtonGroup();
		List<JRadioButton> buttons = new ArrayList<JRadioButton>();
		final int buttonCount = 4;
		for (int i = 0; i < buttonCount; i++)
		{
			buttons.add(new JRadioButton(Integer.toString(i)));
		}
		
		JPanel buttonPanel = new JPanel();
		field = new JTextField();
		field.setMinimumSize(new Dimension(200, 1));
		field.setSize(new Dimension(200, 10));
		field.setText("test");
		buttonPanel.add(field);
		
		for (JRadioButton button : buttons)
		{
			group.add(button);
			buttonPanel.add(button);
			button.addActionListener(new SetTextActionListener(button.getText()));
		}
		
		gbc = new GridBagConstraints();
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(buttonPanel, gbc);
		
		this.add(panel);
		this.pack();
		this.setVisible(true);
	}
	
	public JComponent getCharactersPanel()
	{
		JPanel panel = new JPanel();
		final JTextField customCharacters = new JTextField();
		customCharacters.setEnabled(false);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Characters"));
		ButtonGroup group = new ButtonGroup();
		
		JRadioButton nucleotides = new JRadioButton("Nucleotides (ACGT)");
		nucleotides.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				customCharacters.setEnabled(false);
			}
		});
		group.add(nucleotides);
		panel.add(nucleotides);
		
		final JRadioButton custom = new JRadioButton("Custom:");
		custom.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				customCharacters.setEnabled(true);
			}
		});
		group.add(custom);
		panel.add(custom);
		
		panel.add(customCharacters);
		return panel;
	}
	
	public JComponent getSettingsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("Settings"));
		
		return panel;
	}
	
	private class SetTextActionListener implements ActionListener
	{
		private final String string;
		
		public SetTextActionListener(String string_)
		{
			string = string_;
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			field.setText(string);
		}
	}
	
	/**
	 * Temporary
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		JFrame frame = new SequenceGenerationFrame();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
