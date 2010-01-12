package gui;

import java.awt.*;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import generator.*;

public class SequenceGenerationFrame extends JFrame
{
	/**
	 * Generated
	 */
	private static final long serialVersionUID = 2041048037575448527L;
	
	private final FragmentDisplay fragmentDisplay;
	private JTextField stringField;
	private JSpinner mSpinner;
	private JSpinner rSpinner;
	private JSpinner lSpinner;
	private SequenceGenerator generator;
	
	public SequenceGenerationFrame(FragmentDisplay fragmentDisplay_)
	{
		fragmentDisplay = fragmentDisplay_;
		generator = new SeqGenSingleSequenceMultipleRepeats();
		
		setTitle("Sequence Generator");
		JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(getCharactersPanel(), gbc);
		
		gbc.gridx = 1;
		panel.add(getSettingsPanel(), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(getButtonPanel(), gbc);
		
		this.add(panel);
		if (fragmentDisplay != null)
		{
			this.setLocationRelativeTo(fragmentDisplay.frame);
		}
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
		
		JLabel mLabel = new JLabel("String Length");
		panel.add(mLabel);
		
		SpinnerNumberModel mModel = new SpinnerNumberModel();
		mModel.setMinimum(1);
		mModel.setValue(50);
		mSpinner = new JSpinner(mModel);
		panel.add(mSpinner);
		
		JLabel rLabel = new JLabel("Repeat Count");
		panel.add(rLabel);
		
		SpinnerNumberModel rModel = new SpinnerNumberModel();
		rModel.setMinimum(1);
		rModel.setValue(2);
		rSpinner = new JSpinner(rModel);
		panel.add(rSpinner);
		
		JLabel lLabel = new JLabel("Repeat Length");
		panel.add(lLabel);
		
		SpinnerNumberModel lModel = new SpinnerNumberModel();
		lModel.setMinimum(1);
		lModel.setValue(5);
		lSpinner = new JSpinner(lModel);
		panel.add(lSpinner);
		
		return panel;
	}
	
	public JComponent getButtonPanel()
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
		
		JPanel generateButtonPanel = new JPanel();
		generateButtonPanel.setLayout(new BoxLayout(generateButtonPanel, BoxLayout.LINE_AXIS));
		generateButtonPanel.add(Box.createHorizontalGlue());
		JButton generateButton = new JButton("Generate");
		generateButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int m = (Integer) mSpinner.getValue();
				int r = (Integer) rSpinner.getValue();
				int l = (Integer) lSpinner.getValue();
				String string = generator.generateSequence(m, r, l);
				stringField.setText(string);
			}
		});
		generateButtonPanel.add(generateButton);
		buttonPanel.add(generateButtonPanel);
		
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.LINE_AXIS));
		stringField = new JTextField();
		textPanel.add(stringField);
		buttonPanel.add(textPanel);
		
		JPanel copyButtonPanel = new JPanel();
		copyButtonPanel.setLayout(new BoxLayout(copyButtonPanel, BoxLayout.LINE_AXIS));
		copyButtonPanel.add(Box.createHorizontalGlue());
		JButton copyButton = new JButton("Copy to Fragment Display GUI");
		copyButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				fragmentDisplay.setString(stringField.getText());
			}
		});
		copyButton.setEnabled(fragmentDisplay != null);
		copyButtonPanel.add(copyButton);
		
		buttonPanel.add(copyButtonPanel);
		return buttonPanel;
	}
	
	/**
	 * Temporary
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		JFrame frame = new SequenceGenerationFrame(null);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
