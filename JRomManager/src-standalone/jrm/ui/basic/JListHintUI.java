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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicListUI;

/**
 * The Class JListHintUI.
 */
public class JListHintUI extends BasicListUI
{
	
	/** The hint. */
	private final String hint;
	
	/** The hint color. */
	private final Color hintColor;

	/**
	 * Instantiates a new j list hint UI.
	 *
	 * @param hint the hint
	 * @param hintColor the hint color
	 */
	@SuppressWarnings("exports")
	public JListHintUI(final String hint, final Color hintColor)
	{
		this.hint = hint;
		this.hintColor = hintColor;
	}

	@SuppressWarnings("exports")
	@Override
	public void paint(final Graphics g, final JComponent c)
	{
		super.paint(g, c);
		if(list.getModel().getSize() == 0 && c.isEnabled())
		{
			g.setColor(hintColor);
			g.setFont(c.getFont().deriveFont(Font.ITALIC));
			final int paddingw = (c.getWidth() - g.getFontMetrics().stringWidth(hint)) / 2;
			final int paddingh = (c.getHeight() - c.getFont().getSize()) / 2;
			final int inset = 3;
			g.drawString(hint, paddingw + inset, c.getHeight() - paddingh - inset);
		}
	}
}
