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
	
	private final FragmentDisplay fragmentDisplay;
	private JPanel panel;
	
	public SequenceGenerationFrame(FragmentDisplay fragmentDisplay_)
	{
		fragmentDisplay = fragmentDisplay_;
		setTitle("Sequence Generator");
		panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.5;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(getCharactersPanel(), gbc);
		
		gbc.gridx = 1;
		panel.add(getSettingsPanel(), gbc);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		buttonPanel.add(Box.createHorizontalGlue());
		JButton generateButton = new JButton("Generate");
		generateButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				// TODO Auto-generated method stub
			}
		});
		buttonPanel.add(generateButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		JButton copyButton = new JButton("Copy to Fragment Display GUI");
		copyButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				// TODO Auto-generated method stub
			}
		});
		copyButton.setEnabled(fragmentDisplay != null);
		buttonPanel.add(copyButton);
		
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
		
		JLabel mLabel = new JLabel("String Length");
		panel.add(mLabel);
		
		SpinnerNumberModel mModel = new SpinnerNumberModel();
		mModel.setMinimum(1);
		mModel.setValue(50);
		JSpinner mSpinner = new JSpinner(mModel);
		panel.add(mSpinner);
		
		JLabel rLabel = new JLabel("Repeat Count");
		panel.add(rLabel);
		
		SpinnerNumberModel rModel = new SpinnerNumberModel();
		rModel.setMinimum(1);
		rModel.setValue(2);
		JSpinner rSpinner = new JSpinner(rModel);
		panel.add(rSpinner);
		
		JLabel lLabel = new JLabel("Repeat Length");
		panel.add(lLabel);
		
		SpinnerNumberModel lModel = new SpinnerNumberModel();
		lModel.setMinimum(1);
		lModel.setValue(5);
		JSpinner lSpinner = new JSpinner(lModel);
		panel.add(lSpinner);
		
		return panel;
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
