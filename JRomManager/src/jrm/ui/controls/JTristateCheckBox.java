package jrm.ui.controls;

import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

@SuppressWarnings("serial")
public class JTristateCheckBox extends JCheckBox
{
	private boolean halfState;
	private static Icon selected = new ImageIcon(JTristateCheckBox.class.getResource("/jrm/resources/selected.png"));
	private static Icon unselected = new ImageIcon(JTristateCheckBox.class.getResource("/jrm/resources/unselected.png"));
	private static Icon halfselected = new ImageIcon(JTristateCheckBox.class.getResource("/jrm/resources/halfselected.png"));

	public JTristateCheckBox()
	{
		super();
	}

	@Override
	public void paint(Graphics g)
	{
		if(isSelected())
		{
			halfState = false;
		}
		setIcon(halfState ? halfselected : isSelected() ? selected : unselected);
		super.paint(g);
	}

	public boolean isHalfSelected()
	{
		return halfState;
	}

	public void setHalfSelected(boolean halfState)
	{
		this.halfState = halfState;
		if(halfState)
		{
			setSelected(false);
			repaint();
		}
	}
}
