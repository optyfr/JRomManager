package jrm.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.MachineList;

@SuppressWarnings("serial")
public final class MachineListListRenderer
{
	public final static String[] columns = new String[] { "name", "description", "have" };
	public final static Class<?>[] columnsTypes = new Class<?>[] { Object.class, String.class, String.class };
	public final static int[] columnsWidths = new int[] { 70, 150, -80 };
	public final static TableCellRenderer[] columnsRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
	{
		ImageIcon disk_multiple_green = new ImageIcon(MachineListListRenderer.class.getResource("/jrm/resources/disk_multiple_green.png"));
		ImageIcon disk_multiple_red = new ImageIcon(MachineListListRenderer.class.getResource("/jrm/resources/disk_multiple_red.png"));
		ImageIcon disk_multiple_orange = new ImageIcon(MachineListListRenderer.class.getResource("/jrm/resources/disk_multiple_orange.png"));
		ImageIcon disk_multiple_gray = new ImageIcon(MachineListListRenderer.class.getResource("/jrm/resources/disk_multiple_gray.png"));

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(value instanceof MachineList)
			{
				super.getTableCellRendererComponent(table, "*", isSelected, hasFocus, row, column);
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
	},
	null,
	new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(CENTER);
		}
	} };

	private MachineListListRenderer()
	{
	}

}
