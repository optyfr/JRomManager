/**
 * 
 */
package jrm.ui.basic;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * 
 * @author optyfr
 */
public interface EnhTableModel extends TableModel
{
	/**
	 * @return the cellRenderers
	 */
	@SuppressWarnings("exports")
	public abstract TableCellRenderer[] getCellRenderers();

	/**
	 * @return the cellEditors
	 */
	@SuppressWarnings("exports")
	public default TableCellEditor[] getCellEditors() {return new TableCellEditor[0];}

	/**
	 * get the declared renderer for a given column
	 * @param columnIndex the requested column index
	 * @return a {@link TableCellRenderer} associated with the given columnindex 
	 */
	@SuppressWarnings("exports")
	public default TableCellRenderer getColumnRenderer(int columnIndex)
	{
		return getCellRenderers()[columnIndex];
	}

	/**
	 * get the declared editor for a given column
	 * @param columnIndex the requested column index
	 * @return a {@link TableCellEditor} associated with the given columnindex 
	 */
	@SuppressWarnings("exports")
	public default TableCellEditor getColumnEditor(int columnIndex)
	{
		return getCellEditors()[columnIndex];
	}

	/**
	 * get the declared width for a given column
	 * @param columnIndex the requested column index
	 * @return a width in pixel (if negative then it's a fixed column width)
	 */
	public abstract int getColumnWidth(int columnIndex);
	
	/**
	 * get the tooltip text for a given column
	 * @param columnIndex the requested column index
	 * @return the string of the tooltip
	 */
	public abstract String getColumnTT(int columnIndex);
	
	/**
	 * apply column width limits to the table
	 * @param table the table to modify
	 */
	@SuppressWarnings("exports")
	public default void applyColumnsWidths(JTable table)
	{
		for(int i = 0; i < getColumnCount(); i++)
		{
			int width = getColumnWidth(i);
			TableColumn column = table.getColumnModel().getColumn(i);
			if (width > 0)
			{
				column.setMinWidth(width / 2);
				column.setPreferredWidth(width);
			}
			else if (width < -20)
			{
				column.setMinWidth(-width);
				column.setMaxWidth(-width);
			}
			else if (width < 0)
			{
				final Component component = column.getCellRenderer().getTableCellRendererComponent(table, null, false, false, 0, i);
				final var format = "%0" + (-width) + "d";
				final int pixwidth = component.getFontMetrics(component.getFont()).stringWidth(String.format(format, 0)); //$NON-NLS-1$ //$NON-NLS-2$
				column.setMinWidth(pixwidth / 2);
				column.setPreferredWidth(pixwidth);
				column.setMaxWidth(pixwidth);
			}
		}
	}
}
