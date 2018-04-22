package jrm.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.Software;

@SuppressWarnings("serial")
public final class SoftwareListRenderer
{
	private final static ImageIcon folder_closed_green = new ImageIcon(SoftwareListRenderer.class.getResource("/jrm/resources/folder_closed_green.png"));
	private final static ImageIcon folder_closed_orange = new ImageIcon(SoftwareListRenderer.class.getResource("/jrm/resources/folder_closed_orange.png"));
	private final static ImageIcon folder_closed_red = new ImageIcon(SoftwareListRenderer.class.getResource("/jrm/resources/folder_closed_red.png"));
	private final static ImageIcon folder_closed_gray = new ImageIcon(SoftwareListRenderer.class.getResource("/jrm/resources/folder_closed_gray.png"));
	
	public final static String[] columns = new String[] { "", "name", "description", "have", "cloneof" };
	public final static Class<?>[] columnsTypes = new Class<?>[] { Object.class, Object.class, String.class, String.class, Object.class };
	public final static int[] columnsWidths = new int[] { -20, 40, 200, -45, 40 };
	public final static TableCellRenderer[] columnsRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
	{

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(value instanceof Software)
			{
				super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
				switch(((Software) value).getStatus())
				{
					case COMPLETE:
						setIcon(folder_closed_green);
						break;
					case PARTIAL:
						setIcon(folder_closed_orange);
						break;
					case MISSING:
						setIcon(folder_closed_red);
						break;
					case UNKNOWN:
					default:
						setIcon(folder_closed_gray);
						break;
				}
				return this;
			}
			setIcon(null);
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}, new DefaultTableCellRenderer()
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(value instanceof Software)
			{
				return super.getTableCellRendererComponent(table, ((Software) value).name, isSelected, hasFocus, row, column);
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}, null, new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(CENTER);
		}
	}, new DefaultTableCellRenderer()
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(value instanceof Software)
			{
				super.getTableCellRendererComponent(table, ((Software) value).name, isSelected, hasFocus, row, column);
				switch(((Software) value).getStatus())
				{
					case COMPLETE:
						setIcon(folder_closed_green);
						break;
					case PARTIAL:
						setIcon(folder_closed_orange);
						break;
					case MISSING:
						setIcon(folder_closed_red);
						break;
					case UNKNOWN:
					default:
						setIcon(folder_closed_gray);
						break;
				}
				return this;
			}
			setIcon(null);
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	} };

	private SoftwareListRenderer()
	{
	}

}
