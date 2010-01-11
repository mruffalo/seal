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
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.5;
		
		panel = new JPanel(new GridBagLayout());
		
		ButtonGroup group = new ButtonGroup();
		
		List<JRadioButton> buttons = new ArrayList<JRadioButton>();
		final int buttonCount = 4;
		for (int i = 0; i < buttonCount; i++)
		{
			buttons.add(new JRadioButton(Integer.toString(i)));
		}
		
		field = new JTextField();
		field.setMinimumSize(new Dimension(200, 0));
		panel.add(field);
		
		for (JRadioButton button : buttons)
		{
			group.add(button);
			panel.add(button);
			button.addActionListener(new SetTextActionListener(button.getText()));
		}
		
		this.add(panel);
		this.pack();
		this.setVisible(true);
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
		new SequenceGenerationFrame();
	}
}
