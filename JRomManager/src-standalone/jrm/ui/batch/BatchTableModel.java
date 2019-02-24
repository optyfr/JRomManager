package jrm.ui.batch;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.io.File;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import jrm.locale.Messages;
import jrm.ui.basic.JTableButton;
import jrm.ui.basic.SDRTableModel;

public class BatchTableModel extends SDRTableModel
{
	
	private final String[] headers;
	private final String[] headers_tt;
	private final Class<?>[] types = {File.class, File.class, String.class, String.class, Boolean.class};
	private final int[] widths = {0, 0, 0, -70, -22};

	public BatchTableModel()
	{
		this.headers = new String[] {Messages.getString("BatchTableModel.SrcDats"), Messages.getString("BatchTableModel.DstDirs"), Messages.getString("BatchTableModel.Result"), "Details", "Selected"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		this.headers_tt = this.headers;
	}
	
	public BatchTableModel(String[] headers)
	{
		this.headers = headers;
		this.headers_tt = headers;
	}

	public void setButtonHandler(JTableButton.TableButtonPressedHandler buttonHandler)
	{
		buttons.addHandler(buttonHandler);
	}
	
	private JTableButton buttons = new JTableButton();
	
    @SuppressWarnings("serial")
	private	final TableCellRenderer[] cellRenderers = {new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(row == getCurrentRow() && column == getCurrentCol())
				setBackground(Color.decode("#DDFFDD")); //$NON-NLS-1$
			else
				setBackground(Color.white);
			if(value instanceof File)
			{
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setText(trimmedStringCalculator(((File)value).getPath(), table,this, table.getColumnModel().getColumn(column).getWidth()-10));
				return this;
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
		}
	}, new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if(row == getCurrentRow() && column == getCurrentCol())
				setBackground(Color.decode("#DDFFDD")); //$NON-NLS-1$
			else
				setBackground(Color.white);
			if(value instanceof File)
			{
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setText(trimmedStringCalculator(((File)value).getPath(), table ,this, table.getColumnModel().getColumn(column).getWidth()-10));
				return this;
			}
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	}, new DefaultTableCellRenderer() {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			setBackground(Color.white);
			setHorizontalAlignment(TRAILING);
			return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		}
	},buttons, null};

    private final TableCellEditor[] cellEditors = {null,null,null,buttons,null};
    
	private static String trimmedStringCalculator(String inputText, JTable table, JLabel component, int width)
	{
			String ellipses = "..."; //$NON-NLS-1$
			String textToBeDisplayed = ""; //$NON-NLS-1$
			FontMetrics fm = table.getFontMetrics(component.getFont());
			for (int i = inputText.length() - 1; i >= 0; i--)
				if (fm.stringWidth(ellipses + textToBeDisplayed) <= width)
					textToBeDisplayed = inputText.charAt(i) + textToBeDisplayed;
			if (!textToBeDisplayed.equals(inputText))
				return ellipses.concat(textToBeDisplayed);
		return inputText;
	}
    
    @Override
	public int getRowCount()
	{
		return getData().size();
	}

	@Override
	public int getColumnCount()
	{
		return cellRenderers.length;
	}

	@Override
	public String getColumnName(int columnIndex)
	{
		return headers[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		return types[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex)
	{
		return columnIndex>=3;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0:
				return getData().get(rowIndex).src;
			case 1:
				return getData().get(rowIndex).dst;
			case 2:
				return getData().get(rowIndex).result;
			case 3:
				return "Detail";
			case 4:
				return getData().get(rowIndex).selected;
		}
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex)
	{
		switch(columnIndex)
		{
			case 0:
				getData().get(rowIndex).src = (File) aValue;
				break;
			case 1:
				getData().get(rowIndex).dst = (File) aValue;
				break;
			case 2:
				getData().get(rowIndex).result = (String) aValue;
				break;
			case 3:
				return;
			case 4:
				getData().get(rowIndex).selected = (Boolean) aValue;
				break;
		}
		fireTableChanged(new TableModelEvent(this, rowIndex, rowIndex, columnIndex, TableModelEvent.UPDATE));
	}

	/**
	 * @return the cellRenderers
	 */
	@Override
	public TableCellRenderer[] getCellRenderers()
	{
		return cellRenderers;
	}

	/**
	 * @return the cellRenderers
	 */
	@Override
	public TableCellEditor[] getCellEditors()
	{
		return cellEditors;
	}

	@Override
	public int getColumnWidth(int columnIndex)
	{
		return widths[columnIndex];
	}

	@Override
	public String getColumnTT(int columnIndex)
	{
		return headers_tt[columnIndex];
	}
	
	
	
}