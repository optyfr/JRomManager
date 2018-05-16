package jrm.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.Messages;
import jrm.profile.data.Disk;
import jrm.profile.data.Entity;
import jrm.profile.data.Rom;

@SuppressWarnings("serial")
public final class AnywareRenderer
{
	public final static String[] columns = new String[] { Messages.getString("AnywareRenderer.Status"), Messages.getString("AnywareRenderer.Name"), Messages.getString("AnywareRenderer.Size"), Messages.getString("AnywareRenderer.CRC"), Messages.getString("AnywareRenderer.MD5"), Messages.getString("AnywareRenderer.SHA-1"), Messages.getString("AnywareRenderer.Merge"), Messages.getString("AnywareRenderer.DumpStatus") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
	public final static Class<?>[] columnsTypes = new Class<?>[] { Object.class, Object.class, Long.class, String.class, String.class, String.class, String.class, Object.class };
	public final static int[] columnsWidths = new int[] { -3, 256, -12, -10, -34, -42, 100, -3 };
	public final static TableCellRenderer[] columnsRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
	{
		ImageIcon bullet_green = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/bullet_green.png")); //$NON-NLS-1$
		ImageIcon bullet_red = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/bullet_red.png")); //$NON-NLS-1$
		ImageIcon bullet_black = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/bullet_black.png")); //$NON-NLS-1$

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			if(value!=null) switch(((Entity) value).getStatus())
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
			setText(""); //$NON-NLS-1$
			setBackground(AnywareRenderer.getBackground(row, column));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		ImageIcon rom_small = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/rom_small.png")); //$NON-NLS-1$
		ImageIcon drive = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/drive.png")); //$NON-NLS-1$

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			if(value instanceof Rom)
				setIcon(rom_small);
			else if(value instanceof Disk)
				setIcon(drive);
			setText((value != null) ? (value instanceof Entity ? ((Entity) value).name : value.toString()) : null);
			setToolTipText(getText());
			setBackground(AnywareRenderer.getBackground(row, column));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{// anonymous constructor
			setHorizontalAlignment(SwingConstants.TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setText(value != null ? (value instanceof Long ? ((Long) value > 0 ? value.toString() : null) : value.toString()) : null);
			setBackground(AnywareRenderer.getBackground(row, column));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(SwingConstants.TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setText(value != null ? value.toString() : null);
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
			setBackground(AnywareRenderer.getBackground(row, column));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(SwingConstants.TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setText(value != null ? value.toString() : null);
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
			setBackground(AnywareRenderer.getBackground(row, column));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(SwingConstants.TRAILING);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setText(value != null ? value.toString() : null);
			setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
			setBackground(AnywareRenderer.getBackground(row, column));
			return this;
		}
	}, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(SwingConstants.LEADING);
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			setText(value != null ? value.toString() : null);
			setToolTipText(getText());
			setBackground(AnywareRenderer.getBackground(row, column));
			return this;
		}
	},
	new DefaultTableCellRenderer()
	{
		ImageIcon verified = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/star.png")); //$NON-NLS-1$
		ImageIcon good = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/tick.png")); //$NON-NLS-1$
		ImageIcon baddump = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/delete.png")); //$NON-NLS-1$
		ImageIcon nodump = new ImageIcon(AnywareRenderer.class.getResource("/jrm/resources/icons/error.png")); //$NON-NLS-1$
		{
			setHorizontalAlignment(SwingConstants.CENTER);
		}
		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			if(value instanceof Entity.Status)
			{
				switch((Entity.Status)value)
				{
					case verified:
						setIcon(verified);
						break;
					case good:
						setIcon(good);
						break;
					case baddump:
						setIcon(baddump);
						break;
					case nodump:
						setIcon(nodump);
						break;
				}
			}
			setToolTipText(value != null ? value.toString() : null);
			setText(""); //$NON-NLS-1$
			setBackground(AnywareRenderer.getBackground(row, column));
			return this;
		}
	} };

	private static Color getBackground(final int row, final int col)
	{
		if((col%2)==0)
		{
			if((row%2)==0)
				return Color.decode("0xDDDDEE");
			return Color.decode("0xEEEEEE");
		}
		else
		{
			if((row%2)==0)
				return Color.decode("0xEEEEFF");
			return Color.decode("0xFFFFFF");
		}
	}

	private AnywareRenderer()
	{
	}

}
