package jrm.ui.controls;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicListUI;

public class JListHintUI extends BasicListUI
{
	private String hint;
	private Color hintColor;

	public JListHintUI(String hint, Color hintColor)
	{
		this.hint = hint;
		this.hintColor = hintColor;
	}

	@Override
	public void paint(Graphics g, JComponent c)
	{
		super.paint(g, c);
		if(list.getModel().getSize() == 0 && c.isEnabled())
		{
			g.setColor(hintColor);
			g.setFont(c.getFont().deriveFont(Font.ITALIC));
			int paddingw = (c.getWidth() - g.getFontMetrics().stringWidth(hint)) / 2;
			int paddingh = (c.getHeight() - c.getFont().getSize()) / 2;
			int inset = 3;
			g.drawString(hint, paddingw + inset, c.getHeight() - paddingh - inset);
		}
	}
}
