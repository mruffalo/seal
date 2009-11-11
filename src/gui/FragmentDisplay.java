package gui;

import generator.Fragmentizer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import assembly.*;

public class FragmentDisplay
{
	private static final String FRAGMENT_TEXT = "Fragment";
	
	static
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}
	}
	
	private JFrame frame;
	private JTable table;
	private FragmentTableModel tableModel;
	private ImagePanel origImagePanel;
	private ImagePanel assembledImagePanel;
	private String origString;
	private String assembledString;
	List<List<Fragment>> origGrouped;
	List<List<Fragment>> assembledGrouped;
	/**
	 * Specify this as an ArrayList instead of a List to prevent accidentally storing a LinkedList.
	 * Various parts of this code require random access, for which a LinkedList is badly suited.
	 */
	private ArrayList<Fragment> fragments;
	private JTextField stringField;
	private JTextField assembledField;
	private JSpinner nSpinner;
	private JSpinner kSpinner;
	private JSpinner ktSpinner;
	private int scale = 2;
	private int n;
	private int k;
	private int kt;
	
	Fragment selectedFragment = null;
	
	public FragmentDisplay()
	{
		fragments = new ArrayList<Fragment>();
		origString = assembledString = "";
		origGrouped = Fragmentizer.groupByLine(fragments, FragmentPositionSource.ORIGINAL_SEQUENCE);
		assembledGrouped = Fragmentizer.groupByLine(fragments, FragmentPositionSource.ASSEMBLED_SEQUENCE);
		
		frame = new JFrame("Fragment Display");
		// frame.setBounds(25, 25, 320, 320);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar(getMenuBar());
		frame.getContentPane().setLayout(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.weighty = 0.0;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(2, 2, 2, 2);
		frame.add(getParametersPanel(), constraints);
		
		constraints = new GridBagConstraints();
		constraints.weightx = 1;
		constraints.weighty = 1;
		constraints.gridy = 1;
		constraints.fill = GridBagConstraints.BOTH;
		constraints.insets = new Insets(2, 2, 2, 2);
		frame.add(getFragmentDisplayPanel(), constraints);
		
		frame.pack();
		frame.setVisible(true);
	}
	
	/**
	 * TODO: Externalize strings
	 * 
	 * @return
	 */
	private JMenuBar getMenuBar()
	{
		JMenuBar bar = new JMenuBar();
		JMenuItem item;
		JMenu menu;
		
		menu = new JMenu("Sequence");
		item = new JMenuItem("Open...");
		item.addActionListener(new OpenSequenceActionListener());
		menu.add(item);
		item = new JMenuItem("Generate...");
		menu.add(item);
		item = new JMenuItem("Export...");
		item.addActionListener(new SaveSequenceActionListener());
		menu.add(item);
		menu.addSeparator();
		item = new JMenuItem("Exit");
		menu.add(item);
		bar.add(menu);
		
		menu = new JMenu("Fragments");
		item = new JMenuItem("Open...");
		item.addActionListener(new OpenFragmentsActionListener());
		menu.add(item);
		item = new JMenuItem("Export...");
		item.addActionListener(new SaveFragmentsActionListener());
		menu.add(item);
		bar.add(menu);
		
		menu = new JMenu("Settings");
		item = new JMenuItem("Configure Fragment Display...");
		menu.add(item);
		bar.add(menu);
		
		menu = new JMenu("Help");
		item = new JMenuItem("About");
		menu.add(item);
		bar.add(menu);
		
		return bar;
	}
	
	/**
	 * TODO: Externalize strings
	 * 
	 * @return
	 */
	private JPanel getParametersPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.gridwidth = 3;
		JLabel stringLabel = new JLabel("String to read/assemble:");
		panel.add(stringLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1;
		gbc.gridwidth = 3;
		gbc.gridy = 1;
		stringField = new JTextField(
			"vp8yp7894hp;ob7p985u6p;o34ig;oris;hvh7100*OYglicg7isdgvP(S&DGF:Kjh;kv83hawas5rf2$AD!)%8;oij45oc78GCG^*Aptin3;oFO*Tlivb;ou;34o58H{}GTDA%2o8ffd67gA");
		panel.add(stringField, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.33;
		gbc.gridy = 2;
		JLabel nLabel = new JLabel("Fragment Count");
		panel.add(nLabel, gbc);
		
		gbc.gridx = 1;
		JLabel kLabel = new JLabel("Fragment Size");
		panel.add(kLabel, gbc);
		
		gbc.gridx = 2;
		JLabel ktLabel = new JLabel("Size Leeway");
		ktLabel.setToolTipText("Each fragment will be of length (fragment size) +/- (size leeway)");
		panel.add(ktLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.33;
		gbc.gridy = 3;
		SpinnerNumberModel nModel = new SpinnerNumberModel();
		nModel.setMinimum(1);
		nModel.setValue(50);
		nSpinner = new JSpinner(nModel);
		panel.add(nSpinner, gbc);
		
		gbc.gridx = 1;
		SpinnerNumberModel kModel = new SpinnerNumberModel();
		kModel.setMinimum(1);
		kModel.setValue(20);
		kSpinner = new JSpinner(kModel);
		panel.add(kSpinner, gbc);
		
		gbc.gridx = 2;
		SpinnerNumberModel ktModel = new SpinnerNumberModel();
		ktModel.setMinimum(0);
		ktSpinner = new JSpinner(ktModel);
		panel.add(ktSpinner, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridy = 4;
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.EAST;
		JButton assembleButton = new JButton("Assemble");
		assembleButton.addActionListener(new AssembleSequenceActionListener());
		panel.add(assembleButton, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridy = 5;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.BOTH;
		JLabel assembledLabel = new JLabel("Assembled string:");
		panel.add(assembledLabel, gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridy = 6;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.BOTH;
		assembledField = new JTextField();
		assembledField.setEditable(false);
		panel.add(assembledField, gbc);
		
		return panel;
	}
	
	private JPanel getFragmentDisplayPanel()
	{
		JPanel panel = new JPanel(new GridBagLayout());
		
		origGrouped = Fragmentizer.groupByLine(fragments, FragmentPositionSource.ORIGINAL_SEQUENCE);
		assembledGrouped = Fragmentizer.groupByLine(fragments, FragmentPositionSource.ASSEMBLED_SEQUENCE);
		printFragmentGraph(origString, origGrouped, FragmentPositionSource.ORIGINAL_SEQUENCE);
		printFragmentGraph(assembledString, assembledGrouped, FragmentPositionSource.ASSEMBLED_SEQUENCE);
		Image origImage = ImagePanel.getFragmentGroupImage(origString, origGrouped, null,
			FragmentPositionSource.ORIGINAL_SEQUENCE, scale);
		Image assembledImage = ImagePanel.getFragmentGroupImage(assembledString, assembledGrouped, null,
			FragmentPositionSource.ASSEMBLED_SEQUENCE, scale);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridheight = 1;
		constraints.weightx = 0.5;
		constraints.weighty = 0.5;
		constraints.gridwidth = 1;
		constraints.ipadx = constraints.ipady = 2;
		constraints.fill = GridBagConstraints.BOTH;
		origImagePanel = new ImagePanel(origImage);
		JScrollPane origImageScroller = new JScrollPane(origImagePanel);
		panel.add(origImageScroller, constraints);
		constraints.gridy = 1;
		assembledImagePanel = new ImagePanel(assembledImage);
		JScrollPane assembledImageScroller = new JScrollPane(assembledImagePanel);
		panel.add(assembledImageScroller, constraints);
		
		constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.ipadx = constraints.ipady = 2;
		constraints.gridheight = 2;
		constraints.gridx = 1;
		constraints.weightx = 0.5;
		constraints.weighty = 1;
		tableModel = new FragmentTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new FragmentRedrawSelectionListener());
		
		JScrollPane tableScroller = new JScrollPane(table);
		panel.add(tableScroller, constraints);
		return panel;
	}
	
	private void splitString()
	{
		n = (Integer) nSpinner.getValue();
		k = (Integer) kSpinner.getValue();
		kt = (Integer) ktSpinner.getValue();
		origString = stringField.getText();
		table.clearSelection();
		fragments = new ArrayList<Fragment>(Fragmentizer.fragmentizeForShotgun(origString, n, k, kt));
		tableModel.fireTableDataChanged();
		selectedFragment = null;
	}
	
	private void assembleFragments()
	{
		SequenceAssembler sa = new ShotgunSequenceAssembler();
		assembledString = sa.assembleSequence(fragments);
		tableModel.fireTableDataChanged();
		assembledField.setText(assembledString);
		for (Fragment fragment : fragments)
		{
			System.out.printf("%5d: %s%n", fragment.getPosition(FragmentPositionSource.ORIGINAL_SEQUENCE),
				fragment.string);
		}
		origGrouped = Fragmentizer.groupByLine(fragments, FragmentPositionSource.ORIGINAL_SEQUENCE);
		assembledGrouped = Fragmentizer.groupByLine(fragments, FragmentPositionSource.ASSEMBLED_SEQUENCE);
		printFragmentGraph(origString, origGrouped, FragmentPositionSource.ORIGINAL_SEQUENCE);
		printFragmentGraph(assembledString, assembledGrouped, FragmentPositionSource.ASSEMBLED_SEQUENCE);
		redrawImages();
	}
	
	/**
	 * XXX: Temporary
	 */
	private static void printFragmentGraph(String string, List<List<Fragment>> grouped, FragmentPositionSource source)
	{
		System.out.println();
		System.out.println(string);
		for (List<Fragment> list : grouped)
		{
			int begin = 0;
			for (Fragment fragment : list)
			{
				for (int i = 0; i < fragment.getPosition(source) - begin; i++)
				{
					System.out.print(" ");
				}
				System.out.print(fragment.string);
				begin = fragment.getPosition(source) + fragment.string.length();
			}
			System.out.println();
		}
	}
	
	private void redrawImages()
	{
		Image origImage = ImagePanel.getFragmentGroupImage(origString, origGrouped, selectedFragment,
			FragmentPositionSource.ORIGINAL_SEQUENCE, scale);
		origImagePanel.setImage(origImage);
		origImagePanel.revalidate();
		Image assembledImage = ImagePanel.getFragmentGroupImage(assembledString, assembledGrouped, selectedFragment,
			FragmentPositionSource.ASSEMBLED_SEQUENCE, scale);
		assembledImagePanel.setImage(assembledImage);
		assembledImagePanel.revalidate();
		
		frame.repaint();
	}
	
	/**
	 * TableModel that uses its parent FragmentDisplay's fragment list
	 */
	private class FragmentTableModel extends AbstractTableModel
	{
		/**
		 * Generated by Eclipse
		 */
		private static final long serialVersionUID = -4514730749142944712L;
		
		@Override
		public int getColumnCount()
		{
			return 1 + FragmentPositionSource.values().length;
		}
		
		@Override
		public int getRowCount()
		{
			return fragments.size();
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			Fragment fragment = fragments.get(rowIndex);
			if (columnIndex == 0)
			{
				return fragment.string;
			}
			else
			{
				FragmentPositionSource source = FragmentPositionSource.values()[columnIndex - 1];
				return fragment.getPosition(source);
			}
		}
		
		@Override
		public String getColumnName(int columnIndex)
		{
			if (columnIndex == 0)
			{
				return FRAGMENT_TEXT;
			}
			else
			{
				FragmentPositionSource source = FragmentPositionSource.values()[columnIndex - 1];
				return source.guiDescription;
			}
		}
	}
	
	private class FragmentRedrawSelectionListener implements ListSelectionListener
	{
		@Override
		public void valueChanged(ListSelectionEvent e)
		{
			selectedFragment = table.getSelectedRowCount() > 0 ? fragments.get(table.getSelectedRow()) : null;
			redrawImages();
		}
	}
	
	private class AssembleSequenceActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			splitString();
			assembleFragments();
		}
	}
	
	private class OpenSequenceActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent unused)
		{
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();
				try
				{
					stringField.setText(FastaHandler.getSequence(file));
				}
				catch (IOException e)
				{
					// TODO: Show GUI problem box
					e.printStackTrace();
				}
			}
		}
	}
	
	private class SaveSequenceActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent unused)
		{
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();
				try
				{
					FastaHandler.writeSequence(assembledString, file);
				}
				catch (IOException e)
				{
					// TODO: Show GUI problem box
					e.printStackTrace();
				}
			}
		}
	}
	
	private class SaveFragmentsActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent unused)
		{
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();
				try
				{
					// TODO: Make this a delimited file instead of using FastaHandler
					FastaHandler.writeFragments(fragments, file);
				}
				catch (IOException e)
				{
					// TODO: Show GUI problem box
					e.printStackTrace();
				}
			}
		}
	}
	
	private class OpenFragmentsActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent unused)
		{
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();
				try
				{
					fragments = new ArrayList<Fragment>(Fragmentizer.removeSubstrings(FastaHandler.getFragments(file)));
					assembleFragments();
				}
				catch (IOException e)
				{
					// TODO: Show GUI problem box
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * XXX: Temporary
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		new FragmentDisplay();
	}
}
