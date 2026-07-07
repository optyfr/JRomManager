/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.ui.basic;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicTableUI;

/**
 * A custom {@link BasicTableUI} delegate that renders a hint message when the table is empty.
 * <p>
 * When the table model contains no rows and the component is enabled, this UI delegate paints
 * an italic hint string centered within the table area. This provides visual guidance to the user
 * about what the table is intended to contain.
 * </p>
 *
 * @see javax.swing.JTable
 * @see BasicTableUI
 */
public class JTableHintUI extends BasicTableUI {

    /** The hint text displayed when the table is empty. */
    private final String hint;

    /** The color used to render the hint text. */
    private final Color hintColor;

    /**
     * Constructs a new table hint UI delegate.
     *
     * @param hint the hint text to display when the table is empty
     * @param hintColor the {@link Color} used to render the hint text
     */
    public JTableHintUI(final String hint, final Color hintColor) {
        this.hint = hint;
        this.hintColor = hintColor;
    }

    /**
     * {@inheritDoc}
     * <p>
     * After the default paint, if the table has no rows and the component is enabled,
     * the hint text is drawn in italic font centered within the component bounds.
     * </p>
     *
     * @param g the {@link Graphics} context to paint with
     * @param c the {@link JComponent} being painted
     */
    @Override
    public void paint(final Graphics g, final JComponent c) {
        super.paint(g, c);
        if (table.getModel().getRowCount() == 0 && c.isEnabled()) {
            g.setColor(hintColor);
            g.setFont(c.getFont().deriveFont(Font.ITALIC));
            final int paddingw = (c.getWidth() - g.getFontMetrics().stringWidth(hint)) / 2;
            final int paddingh = (c.getHeight() - c.getFont().getSize()) / 2;
            final int inset = 3;
            g.drawString(hint, paddingw + inset, c.getHeight() - paddingh - inset);
        }
    }
}
