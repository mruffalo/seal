package gui;

import javax.swing.*;

public class FragmentDisplay
{
	JFrame frame;
	
	public FragmentDisplay()
	{
		frame = new JFrame();
		frame.pack();
		frame.setBounds(25, 25, 320, 320);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
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
