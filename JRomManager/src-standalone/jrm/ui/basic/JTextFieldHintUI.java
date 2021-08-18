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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

/**
 * The Class JTextFieldHintUI.
 */
public class JTextFieldHintUI extends BasicTextFieldUI implements FocusListener
{
	
	/** The hint. */
	private final String hint;
	
	/** The hint color. */
	private final Color hintColor;

	/**
	 * Instantiates a new j text field hint UI.
	 *
	 * @param hint the hint
	 * @param hintColor the hint color
	 */
	@SuppressWarnings("exports")
	public JTextFieldHintUI(final String hint, final Color hintColor)
	{
		this.hint = hint;
		this.hintColor = hintColor;
	}

	/**
	 * Repaint.
	 */
	private void repaint()
	{
		if(getComponent() != null)
		{
			getComponent().repaint();
		}
	}

	@Override
	protected void paintSafely(final Graphics g)
	{
		// Render the default text field UI
		super.paintSafely(g);
		// Render the hint text
		final JTextComponent component = getComponent();
		if(component.getText().length() == 0 && !component.hasFocus() && component.isEnabled())
		{
			g.setColor(hintColor);
			g.setFont(component.getFont().deriveFont(Font.ITALIC));
			final int padding = (component.getHeight() - component.getFont().getSize()) / 2;
			final int inset = 3;
			g.drawString(hint, inset, component.getHeight() - padding - inset);
		}
	}

	@SuppressWarnings("exports")
	@Override
	public void focusGained(final FocusEvent e)
	{
		repaint();
	}

	@SuppressWarnings("exports")
	@Override
	public void focusLost(final FocusEvent e)
	{
		repaint();
	}

	@Override
	public void installListeners()
	{
		super.installListeners();
		getComponent().addFocusListener(this);
	}

	@Override
	public void uninstallListeners()
	{
		super.uninstallListeners();
		getComponent().removeFocusListener(this);
	}
}
