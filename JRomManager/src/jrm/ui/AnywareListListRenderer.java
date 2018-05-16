package jrm.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.Messages;
import jrm.profile.data.MachineList;
import jrm.profile.data.SoftwareList;

@SuppressWarnings("serial")
public final class AnywareListListRenderer
{
	public final static String[] columns = new String[] { Messages.getString("SoftwareListListRenderer.Name"), Messages.getString("SoftwareListListRenderer.Description"), Messages.getString("SoftwareListListRenderer.Have") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	public final static Class<?>[] columnsTypes = new Class<?>[] { Object.class, String.class, String.class };
	public final static int[] columnsWidths = new int[] { 70, 150, -80 };
	public final static TableCellRenderer[] columnsRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
	{
		ImageIcon disk_multiple_green = new ImageIcon(AnywareListListRenderer.class.getResource("/jrm/resources/disk_multiple_green.png")); //$NON-NLS-1$
		ImageIcon disk_multiple_orange = new ImageIcon(AnywareListListRenderer.class.getResource("/jrm/resources/disk_multiple_orange.png")); //$NON-NLS-1$
		ImageIcon disk_multiple_red = new ImageIcon(AnywareListListRenderer.class.getResource("/jrm/resources/disk_multiple_red.png")); //$NON-NLS-1$
		ImageIcon disk_multiple_gray = new ImageIcon(AnywareListListRenderer.class.getResource("/jrm/resources/disk_multiple_gray.png")); //$NON-NLS-1$

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			if(value instanceof SoftwareList)
			{
				super.getTableCellRendererComponent(table, ((SoftwareList) value).name, isSelected, hasFocus, row, column);
				switch(((SoftwareList) value).getStatus())
				{
					case COMPLETE:
						setIcon(disk_multiple_green);
						break;
					case MISSING:
						setIcon(disk_multiple_red);
						break;
					case PARTIAL:
						setIcon(disk_multiple_orange);
						break;
					case UNKNOWN:
					default:
						setIcon(disk_multiple_gray);
						break;

				}
				return this;
			}
			else if(value instanceof MachineList)
			{
				super.getTableCellRendererComponent(table, Messages.getString("MachineListListRenderer.*"), isSelected, hasFocus, row, column); //$NON-NLS-1$
				switch(((MachineList) value).getStatus())
				{
					case COMPLETE:
						setIcon(disk_multiple_green);
						break;
					case MISSING:
						setIcon(disk_multiple_red);
						break;
					case PARTIAL:
						setIcon(disk_multiple_orange);
						break;
					case UNKNOWN:
					default:
						setIcon(disk_multiple_gray);
						break;

				}
				return this;
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}, new DefaultTableCellRenderer(), new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(SwingConstants.CENTER);
		}
	} };

	private AnywareListListRenderer()
	{
	}

}
