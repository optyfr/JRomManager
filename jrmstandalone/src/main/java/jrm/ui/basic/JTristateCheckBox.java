/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.ui.basic;

import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JCheckBox;

import jrm.ui.MainFrame;

/**
 * The Class JTristateCheckBox.
 */
@SuppressWarnings("serial")
public class JTristateCheckBox extends JCheckBox
{
	
	/** The half state. */
	private boolean halfState;
	
	/** The selected. */
	private static Icon selected = MainFrame.getIcon("/jrm/resicons/selected.png"); //$NON-NLS-1$
	
	/** The unselected. */
	private static Icon unselected = MainFrame.getIcon("/jrm/resicons/unselected.png"); //$NON-NLS-1$
	
	/** The halfselected. */
	private static Icon halfselected = MainFrame.getIcon("/jrm/resicons/halfselected.png"); //$NON-NLS-1$

	/**
	 * Instantiates a new j tristate check box.
	 */
	public JTristateCheckBox()
	{
		super();
	}

	@SuppressWarnings("exports")
	@Override
	public void paint(final Graphics g)
	{
		final Icon icon;
		if(isSelected())
		{
			halfState = false;
			icon = JTristateCheckBox.selected;
		}
		else if(halfState)
			icon = JTristateCheckBox.halfselected;
		else
			icon = JTristateCheckBox.unselected;
		setIcon(icon);
		super.paint(g);
	}

	/**
	 * Checks if is half selected.
	 *
	 * @return true, if is half selected
	 */
	public boolean isHalfSelected()
	{
		return halfState;
	}

	/**
	 * Sets the half selected.
	 *
	 * @param halfState the new half selected
	 */
	public void setHalfSelected(final boolean halfState)
	{
		this.halfState = halfState;
		if(halfState)
		{
			setSelected(false);
			repaint();
		}
	}
}
