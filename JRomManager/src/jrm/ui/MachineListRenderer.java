package jrm.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.Machine;

@SuppressWarnings("serial")
public final class MachineListRenderer
{
	private final static ImageIcon folder_closed_green = new ImageIcon(MachineListRenderer.class.getResource("/jrm/resources/folder_closed_green.png"));
	private final static ImageIcon folder_closed_orange = new ImageIcon(MachineListRenderer.class.getResource("/jrm/resources/folder_closed_orange.png"));
	private final static ImageIcon folder_closed_red = new ImageIcon(MachineListRenderer.class.getResource("/jrm/resources/folder_closed_red.png"));
	private final static ImageIcon folder_closed_gray = new ImageIcon(MachineListRenderer.class.getResource("/jrm/resources/folder_closed_gray.png"));
	
	public static  String[] columns = new String[] { "", "name", "description", "have", "cloneof", "romof", "sampleof" };
	public static  Class<?>[] columnsTypes = new Class<?>[] { Object.class, Object.class, String.class, String.class, Object.class, Object.class, String.class };
	public static  int[] columnsWidths = new int[] { -20, 40, 200, -45, 40, 40, 40 };
	public static  TableCellRenderer[] columnsRenderers = new TableCellRenderer[] { new DefaultTableCellRenderer()
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(value instanceof Machine)
			{
				super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
				switch(((Machine) value).getStatus())
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
	},
	new DefaultTableCellRenderer()
	{
		ImageIcon application_osx_terminal = new ImageIcon(MachineListRenderer.class.getResource("/jrm/resources/icons/application_osx_terminal.png"));
		ImageIcon computer = new ImageIcon(MachineListRenderer.class.getResource("/jrm/resources/icons/computer.png"));
		ImageIcon wrench = new ImageIcon(MachineListRenderer.class.getResource("/jrm/resources/icons/wrench.png"));
		ImageIcon joystick = new ImageIcon(MachineListRenderer.class.getResource("/jrm/resources/icons/joystick.png"));
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(value instanceof Machine)
			{
				super.getTableCellRendererComponent(table, ((Machine) value).name, isSelected, hasFocus, row, column);
				if(((Machine) value).isbios)
					setIcon(application_osx_terminal);
				else if(((Machine) value).isdevice)
					setIcon(computer);
				else if(((Machine) value).ismechanical)
					setIcon(wrench);
				else
					setIcon(joystick);
				setText(((Machine) value).name);
				return this;
			}
			setIcon(null);
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}, 
	null, 
	new DefaultTableCellRenderer()
	{
		{
			setHorizontalAlignment(CENTER);
		}
	},
	new DefaultTableCellRenderer()
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(value instanceof Machine)
			{
				super.getTableCellRendererComponent(table, ((Machine) value).name, isSelected, hasFocus, row, column);
				switch(((Machine) value).getStatus())
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
	},
	new DefaultTableCellRenderer()
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(value instanceof Machine)
			{
				super.getTableCellRendererComponent(table, ((Machine) value).name, isSelected, hasFocus, row, column);
				switch(((Machine) value).getStatus())
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
	}, null };

	private MachineListRenderer()
	{
	}

}
