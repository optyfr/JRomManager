package jrm.ui.basic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import jrm.ui.basic.JFileDropTable.FileTableModel.SrcDstResult;

@SuppressWarnings("serial")
public class JFileDropTable extends JTable implements DropTargetListener
{
	protected static class FileTableModel implements TableModel
	{
		static class SrcDstResult
		{
			File src = null;
			File dst = null;
			String result = "";
			
			static String[] headers = {"Src Dats", "Dst Dirs", "Result"};
			static Class<?>[] types = {File.class, File.class, String.class};
			static TableCellRenderer[] columnsRenderers = {new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof File)
					{
						setText(((File)value).getPath());
					}
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}, new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
				{
					if(value instanceof File)
					{
						setText(((File)value).getPath());
					}
					return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				}
			}, new DefaultTableCellRenderer()};
		}
		
		protected List<SrcDstResult> data = new ArrayList<>();
	    protected EventListenerList listenerList = new EventListenerList();

		@Override
		public int getRowCount()
		{
			return data.size();
		}

		@Override
		public int getColumnCount()
		{
			return 3;
		}

		@Override
		public String getColumnName(int columnIndex)
		{
			return SrcDstResult.headers[columnIndex];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex)
		{
			return SrcDstResult.types[columnIndex];
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			switch(columnIndex)
			{
				case 0:
					return data.get(rowIndex).src;
				case 1:
					return data.get(rowIndex).dst;
				case 2:
					return data.get(rowIndex).result;
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			switch(columnIndex)
			{
				case 0:
					data.get(rowIndex).src = (File) aValue;
					break;
				case 1:
					data.get(rowIndex).dst = (File) aValue;
					break;
				case 2:
					data.get(rowIndex).result = (String) aValue;
					break;
			}
		}

		@Override
		public void addTableModelListener(final TableModelListener l)
		{
			if(listenerList == null)
				listenerList = new EventListenerList();
			listenerList.add(TableModelListener.class, l);
		}

		@Override
		public void removeTableModelListener(final TableModelListener l)
		{
			if(listenerList == null)
				listenerList = new EventListenerList();
			listenerList.remove(TableModelListener.class, l);
		}

		/**
		 * Sends TableChanged event to listeners
		 * @param e the {@link TableModelEvent} to send
		 */
		public void fireTableChanged(final TableModelEvent e)
		{
			if(listenerList == null)
				listenerList = new EventListenerList();
			final Object[] listeners = listenerList.getListenerList();
			for(int i = listeners.length - 2; i >= 0; i -= 2)
				if(listeners[i] == TableModelListener.class)
					((TableModelListener) listeners[i + 1]).tableChanged(e);
		}
		
	}
	
	/** The color. */
	private final Color color;
	
	private FileTableModel model;

	public JFileDropTable()
	{
		super(new FileTableModel());
		setCellSelectionEnabled(true);
		model = (FileTableModel)getModel();
		for(int i = 0; i < getColumnModel().getColumnCount(); i++)
			getColumnModel().getColumn(i).setCellRenderer(SrcDstResult.columnsRenderers[i]);
		color = getBackground();
		new DropTarget(this, this);
	}
	
	@Override
	public void dragEnter(DropTargetDragEvent dtde)
	{
	}

	@Override
	public void dragOver(DropTargetDragEvent dtde)
	{
		final Transferable transferable = dtde.getTransferable();
		if (isEnabled() && transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
		{
            Point point = dtde.getLocation();
            int row = rowAtPoint(point);
            int col = columnAtPoint(point);
            if(row==-1)
            {
        		clearSelection();
            	setBackground(Color.decode("#DDFFDD")); //$NON-NLS-1$
            }
            else
            {
            	setColumnSelectionInterval(col, col);
            	setRowSelectionInterval(row, row);
            }
			dtde.acceptDrag(DnDConstants.ACTION_COPY);
		}
		else
		{
			clearSelection();
			setBackground(Color.decode("#FFDDDD")); //$NON-NLS-1$
			dtde.rejectDrag();
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde)
	{
	}

	@Override
	public void dragExit(DropTargetEvent dte)
	{
		setBackground(color);
		clearSelection();
	}

	@Override
	public void drop(DropTargetDropEvent dtde)
	{
		setBackground(color);
		try
		{
			final Transferable transferable = dtde.getTransferable();

			if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				@SuppressWarnings("unchecked")
				final List<File> files = ((List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor)).stream().filter(f -> {
					return true;
				}).collect(Collectors.toList());
				if (files.size() > 0)
				{
		            Point point = dtde.getLocation();
		            int row = rowAtPoint(point);
		            int col = columnAtPoint(point);
	            	for(int i = 0; i < files.size(); i++)
	            	{
	            		File file = files.get(i);
	            		SrcDstResult line;
	            		if(row == -1 || row + i >= model.data.size())
			            	model.data.add(line = new SrcDstResult());
	            		else
	            			line = model.data.get(row + i);
		            	if(col==1)
		            		line.dst = file;
		            	else
		            		line.src = file;
	            	}
					dtde.getDropTargetContext().dropComplete(true);
				}
				else
					dtde.getDropTargetContext().dropComplete(false);
			}
			else
				dtde.rejectDrop();
		}
		catch (final UnsupportedFlavorException e)
		{
			dtde.rejectDrop();
		}
		catch (final Exception e)
		{
			dtde.rejectDrop();
		}
	}

}
