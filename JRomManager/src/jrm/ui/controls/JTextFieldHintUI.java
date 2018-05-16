package jrm.ui.controls;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.JTextComponent;

public class JTextFieldHintUI extends BasicTextFieldUI implements FocusListener
{
	private final String hint;
	private final Color hintColor;

	public JTextFieldHintUI(final String hint, final Color hintColor)
	{
		this.hint = hint;
		this.hintColor = hintColor;
	}

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

	@Override
	public void focusGained(final FocusEvent e)
	{
		repaint();
	}

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
