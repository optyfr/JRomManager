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
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;

// TODO: Auto-generated Javadoc
/**
 * The Class JTristateCheckBox.
 */
@SuppressWarnings("serial")
public class JTristateCheckBox extends JCheckBox
{
	
	/** The half state. */
	private boolean halfState;
	
	/** The selected. */
	private static Icon selected = new ImageIcon(JTristateCheckBox.class.getResource("/jrm/resicons/selected.png")); //$NON-NLS-1$
	
	/** The unselected. */
	private static Icon unselected = new ImageIcon(JTristateCheckBox.class.getResource("/jrm/resicons/unselected.png")); //$NON-NLS-1$
	
	/** The halfselected. */
	private static Icon halfselected = new ImageIcon(JTristateCheckBox.class.getResource("/jrm/resicons/halfselected.png")); //$NON-NLS-1$

	/**
	 * Instantiates a new j tristate check box.
	 */
	public JTristateCheckBox()
	{
		super();
	}

	@Override
	public void paint(final Graphics g)
	{
		if(isSelected())
		{
			halfState = false;
		}
		setIcon(halfState ? JTristateCheckBox.halfselected : isSelected() ? JTristateCheckBox.selected : JTristateCheckBox.unselected);
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
