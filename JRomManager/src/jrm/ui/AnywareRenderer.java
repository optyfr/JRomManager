package jrm.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.Anyware;
import jrm.profile.data.Disk;
import jrm.profile.data.Entity;
import jrm.profile.data.Rom;

@SuppressWarnings("serial")
public final class AnywareRenderer
{
	public final static String[] columns = new String[] { "", "name", "size", "CRC", "MD5", "SHA-1" };
	public final static Class<?>[] columnsTypes = new Class<?>[] { Object.class, Object.class, Long.class, String.class, String.class, String.class };
	public final static int[] columnsWidths = new int[] { -20, 256, 80, 64, 256, 320 };
	public final static TableCellRenderer[] columnsRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
	{
		ImageIcon bullet_green = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/bullet_green.png"));
		ImageIcon bullet_red = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/bullet_red.png"));
		ImageIcon bullet_black = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/bullet_black.png"));

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			switch(((Entity) value).getStatus())
			{
				case OK:
					setIcon(bullet_green);
					break;
				case KO:
					setIcon(bullet_red);
					break;
				case UNKNOWN:
				default:
					setIcon(bullet_black);
					break;
			}
			setText("");
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		ImageIcon rom_small = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/rom_small.png"));
		ImageIcon drive = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/drive.png"));

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(value instanceof Rom)
				setIcon(rom_small);
			else if(value instanceof Disk)
				setIcon(drive);
			setText((value != null) ? (value instanceof Anyware ? ((Anyware) value).getName() : value.toString()) : null);
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{// anonymous constructor
			setHorizontalAlignment(TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			setText(value != null ? (value instanceof Long ? ((Long) value > 0 ? value.toString() : null) : value.toString()) : null);
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			setText(value!=null?value.toString():null);
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			setText(value!=null?value.toString():null);
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			setText(value!=null?value.toString():null);
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
			return this;
		}
	} };

	private AnywareRenderer()
	{
	}

}
