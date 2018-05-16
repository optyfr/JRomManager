package jrm.ui.controls;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicListUI;

public class JListHintUI extends BasicListUI
{
	private final String hint;
	private final Color hintColor;

	public JListHintUI(final String hint, final Color hintColor)
	{
		this.hint = hint;
		this.hintColor = hintColor;
	}

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
