package gui;

import generator.Fragmentizer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import utils.FastaHandler;
import utils.LicenseUtil;
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
	
	JFrame frame;
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
	private JSpinner ksdSpinner;
	private FragmentDisplaySettings settings;
	private JScrollPane origImageScroller;
	private JScrollPane assembledImageScroller;
	
	Fragment selectedFragment = null;
	
	public FragmentDisplay()
	{
		settings = new FragmentDisplaySettings();
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
		item.addActionListener(new GenerateFragmentsActionListener());
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
		item.addActionListener(new SettingsDialogActionListener());
		menu.add(item);
		bar.add(menu);
		
		menu = new JMenu("Help");
		item = new JMenuItem("About...");
		item.addActionListener(new AboutDialogActionListener());
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
		stringField = new JTextField();
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
		JLabel ksdLabel = new JLabel("Size Std.Dev.");
		ksdLabel.setToolTipText("Each fragment's size will be normally distributed with this standard deviation");
		panel.add(ksdLabel, gbc);
		
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
		SpinnerNumberModel ksdModel = new SpinnerNumberModel(0.0, 0.0, null, 0.5);
		ksdSpinner = new JSpinner(ksdModel);
		panel.add(ksdSpinner, gbc);
		
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
	
	private JComponent getFragmentDisplayPanel()
	{
		JPanel imagePanel = new JPanel(new GridBagLayout());
		
		origGrouped = Fragmentizer.groupByLine(fragments, FragmentPositionSource.ORIGINAL_SEQUENCE);
		assembledGrouped = Fragmentizer.groupByLine(fragments, FragmentPositionSource.ASSEMBLED_SEQUENCE);
		printFragmentGraph(origString, origGrouped, FragmentPositionSource.ORIGINAL_SEQUENCE);
		printFragmentGraph(assembledString, assembledGrouped, FragmentPositionSource.ASSEMBLED_SEQUENCE);
		Image origImage = ImagePanel.getFragmentGroupImage(settings, origString, origGrouped, null,
			FragmentPositionSource.ORIGINAL_SEQUENCE);
		Image assembledImage = ImagePanel.getFragmentGroupImage(settings, assembledString, assembledGrouped, null,
			FragmentPositionSource.ASSEMBLED_SEQUENCE);
		
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.weightx = 1.0;
		constraints.weighty = 0.5;
		constraints.ipadx = constraints.ipady = 2;
		constraints.fill = GridBagConstraints.BOTH;
		origImagePanel = new ImagePanel(this, origImage);
		origImageScroller = new JScrollPane(origImagePanel);
		imagePanel.add(origImageScroller, constraints);
		constraints.gridy = 1;
		assembledImagePanel = new ImagePanel(this, assembledImage);
		assembledImageScroller = new JScrollPane(assembledImagePanel);
		
		final JScrollBar origImageScrollBar = origImageScroller.getHorizontalScrollBar();
		final JScrollBar assembledImageScrollBar = assembledImageScroller.getHorizontalScrollBar();
		
		origImageScrollBar.addAdjustmentListener(new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				if (!e.getValueIsAdjusting())
				{
					return;
				}
				
				int range1 = origImageScrollBar.getMaximum() - origImageScrollBar.getMinimum()
						- origImageScrollBar.getModel().getExtent();
				int range2 = assembledImageScrollBar.getMaximum() - assembledImageScrollBar.getMinimum()
						- assembledImageScrollBar.getModel().getExtent();
				
				double percent = (double) (origImageScrollBar.getValue()) / range1;
				
				int newVal = (int) (percent * range2);
				assembledImageScrollBar.setValue(newVal);
			}
		});
		
		assembledImageScrollBar.addAdjustmentListener(new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				if (!e.getValueIsAdjusting())
				{
					return;
				}
				
				int range1 = origImageScrollBar.getMaximum() - origImageScrollBar.getMinimum()
						- origImageScrollBar.getModel().getExtent();
				int range2 = assembledImageScrollBar.getMaximum() - assembledImageScrollBar.getMinimum()
						- assembledImageScrollBar.getModel().getExtent();
				
				double percent = (double) assembledImageScrollBar.getValue() / range2;
				
				int newVal = (int) (percent * range1);
				
				origImageScrollBar.setValue(newVal);
			}
		});
		
		imagePanel.add(assembledImageScroller, constraints);
		
		tableModel = new FragmentTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new FragmentRedrawSelectionListener());
		
		JScrollPane tableScroller = new JScrollPane(table);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imagePanel, tableScroller);
		Dimension minimumImageSize = new Dimension(200, 1);
		imagePanel.setMinimumSize(minimumImageSize);
		return splitPane;
	}
	
	/**
	 * XXX: This probably needs to be synchronized
	 * 
	 * @param string
	 */
	public void setString(String string)
	{
		stringField.setText(string);
		// TODO: Maybe clear fragments like splitString() does
	}
	
	private void splitString()
	{
		Fragmentizer.Options o = new Fragmentizer.Options();
		o.n = (Integer) nSpinner.getValue();
		o.k = (Integer) kSpinner.getValue();
		o.ksd = (Double) ksdSpinner.getValue();
		origString = stringField.getText();
		table.clearSelection();
		fragments = new ArrayList<Fragment>(Fragmentizer.fragmentizeForShotgun(origString, o));
		Collections.sort(fragments, new FragmentComparator());
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
				fragment.getString());
		}
		origGrouped = Fragmentizer.groupByLine(fragments, FragmentPositionSource.ORIGINAL_SEQUENCE);
		assembledGrouped = Fragmentizer.groupByLine(fragments, FragmentPositionSource.ASSEMBLED_SEQUENCE);
		printFragmentGraph(origString, origGrouped, FragmentPositionSource.ORIGINAL_SEQUENCE);
		printFragmentGraph(assembledString, assembledGrouped, FragmentPositionSource.ASSEMBLED_SEQUENCE);
		redrawImages();
	}
	
	/**
	 * Temporary
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
				System.out.print(fragment.getString());
				begin = fragment.getPosition(source) + fragment.getString().length();
			}
			System.out.println();
		}
	}
	
	private void redrawImages()
	{
		Image origImage = ImagePanel.getFragmentGroupImage(settings, origString, origGrouped, selectedFragment,
			FragmentPositionSource.ORIGINAL_SEQUENCE);
		origImagePanel.setImage(origImage);
		origImagePanel.revalidate();
		origImageScroller.getViewport().setBackground(settings.colors.get(FragmentDisplayColor.BACKGROUND));
		assembledImageScroller.getViewport().setBackground(settings.colors.get(FragmentDisplayColor.BACKGROUND));
		Image assembledImage = ImagePanel.getFragmentGroupImage(settings, assembledString, assembledGrouped,
			selectedFragment, FragmentPositionSource.ASSEMBLED_SEQUENCE);
		assembledImagePanel.setImage(assembledImage);
		assembledImagePanel.revalidate();
		
		frame.repaint();
	}
	
	public void setSettings(FragmentDisplaySettings settings_)
	{
		settings = settings_;
		redrawImages();
	}
	
	public FragmentDisplaySettings getSettings()
	{
		return settings;
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
				return fragment.getString();
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
			if (stringField.getText().length() > 0)
			{
				splitString();
				assembleFragments();
			}
			else
			{
				JOptionPane.showMessageDialog(frame, "Can't split/assemble an empty string.", "Error",
					JOptionPane.ERROR_MESSAGE);
			}
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
					setString(FastaHandler.getSequence(file));
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
			int returnVal = fc.showSaveDialog(frame);
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
			int returnVal = fc.showSaveDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();
				try
				{
					FastaHandler.writeFragmentsWithPositions(fragments, file);
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
					origString = "";
					List<Fragment> rawFragments = FastaHandler.getFragmentsWithPositions(file);
					fragments = new ArrayList<Fragment>(Fragmentizer.removeSubstrings(rawFragments));
					Collections.sort(fragments, new FragmentComparator());
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
	
	private class GenerateFragmentsActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			new SequenceGenerationFrame(FragmentDisplay.this);
		}
	}
	
	private class SettingsDialogActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			new SettingsFrame(FragmentDisplay.this);
		}
	}
	
	private class AboutDialogActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			new AboutFrame(FragmentDisplay.this);
		}
	}
	
	private class FragmentComparator implements Comparator<Fragment>
	{
		@Override
		public int compare(Fragment f1, Fragment f2)
		{
			return f1.getString().compareTo(f2.getString());
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		LicenseUtil.printLicense();
		new FragmentDisplay();
	}
}
