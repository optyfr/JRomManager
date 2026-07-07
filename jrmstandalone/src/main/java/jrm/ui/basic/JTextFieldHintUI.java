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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

/**
 * A custom {@link BasicTextFieldUI} delegate that renders a hint message when the text field is empty and unfocused.
 * <p>
 * When the text component contains no text, does not have focus, and is enabled, this UI delegate
 * paints an italic hint string within the text field. The hint disappears automatically when the
 * user focuses the field or types text.
 * </p>
 *
 * @see javax.swing.JTextField
 * @see BasicTextFieldUI
 */
public class JTextFieldHintUI extends BasicTextFieldUI implements FocusListener {

    /** The hint text displayed when the field is empty and unfocused. */
    private final String hint;

    /** The color used to render the hint text. */
    private final Color hintColor;

    /**
     * Constructs a new text field hint UI delegate.
     *
     * @param hint the hint text to display when the field is empty
     * @param hintColor the {@link Color} used to render the hint text
     */
    public JTextFieldHintUI(final String hint, final Color hintColor) {
        this.hint = hint;
        this.hintColor = hintColor;
    }

    /**
     * Triggers a repaint of the associated text component.
     */
    private void repaint() {
        if (getComponent() != null) {
            getComponent().repaint();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * After the default paint, if the text component is empty, unfocused, and enabled,
     * the hint text is drawn in italic font at the left edge of the component.
     * </p>
     *
     * @param g the {@link Graphics} context to paint with
     */
    @Override
    protected void paintSafely(final Graphics g) {
        // Render the default text field UI
        super.paintSafely(g);
        // Render the hint text
        final JTextComponent component = getComponent();
        if (component.getText().isEmpty() && !component.hasFocus() && component.isEnabled()) {
            g.setColor(hintColor);
            g.setFont(component.getFont().deriveFont(Font.ITALIC));
            final int padding = (component.getHeight() - component.getFont().getSize()) / 2;
            final int inset = 3;
            g.drawString(hint, inset, component.getHeight() - padding - inset);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Triggers a repaint to hide the hint when focus is gained.
     * </p>
     *
     * @param e the {@link FocusEvent}
     */
    @Override
    public void focusGained(final FocusEvent e) {
        repaint();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Triggers a repaint to show the hint when focus is lost.
     * </p>
     *
     * @param e the {@link FocusEvent}
     */
    @Override
    public void focusLost(final FocusEvent e) {
        repaint();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Installs the default listeners and adds this instance as a focus listener on the text component.
     * </p>
     */
    @Override
    public void installListeners() {
        super.installListeners();
        getComponent().addFocusListener(this);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Removes this instance as a focus listener from the text component, then uninstalls the default listeners.
     * </p>
     */
    @Override
    public void uninstallListeners() {
        super.uninstallListeners();
        getComponent().removeFocusListener(this);
    }
}
