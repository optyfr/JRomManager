package jrm.ui.basic;

import java.io.FileFilter;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import jrm.aui.basic.SrcDstResult.SDRList;

public abstract class SDRTableModel implements EnhTableModel
{
	private SDRList data = new SDRList();
    private final EventListenerList listenerList = new EventListenerList();
    private FileFilter srcFilter = null;
    private FileFilter dstFilter = null;


	private int currentRow;
	private int currentCol;
	
	/**
	 * @return the current_row
	 */
	public int getCurrentRow()
	{
		return currentRow;
	}

	/**
	 * @param currentRow the current_row to set
	 * @return the current row
	 */
	public int setCurrentRow(int currentRow)
	{
		this.currentRow = currentRow;
		return currentRow;
	}

	/**
	 * @return the current_col
	 */
	public int getCurrentCol()
	{
		return currentCol;
	}

	/**
	 * @param currentCol the current_col to set
	 * @return the current col
	 */
	public int setCurrentCol(int currentCol)
	{
		this.currentCol = currentCol;
		return currentCol;
	}

	/**
	 * @return the data
	 */
	@SuppressWarnings("exports")
	public SDRList getData()
	{
		return data;
	}

	/**
	 * @param data initialize data
	 */
	@SuppressWarnings("exports")
	public void setData(SDRList data)
	{
		currentRow = -1;
		this.data = data;
		fireTableChanged(new TableModelEvent(this));
	}

	@SuppressWarnings("exports")
	@Override
	public void addTableModelListener(final TableModelListener l)
	{
		listenerList.add(TableModelListener.class, l);
	}

	@SuppressWarnings("exports")
	@Override
	public void removeTableModelListener(final TableModelListener l)
	{
		listenerList.remove(TableModelListener.class, l);
	}

	/**
	 * Sends TableChanged event to listeners
	 * @param e the {@link TableModelEvent} to send
	 */
	@SuppressWarnings("exports")
	public void fireTableChanged(final TableModelEvent e)
	{
		final Object[] listeners = listenerList.getListenerList();
		for(int i = listeners.length - 2; i >= 0; i -= 2)
			if(listeners[i] == TableModelListener.class)
				((TableModelListener) listeners[i + 1]).tableChanged(e);
	}

	/**
	 * @return the srcFilter
	 */
	public FileFilter getSrcFilter()
	{
		return srcFilter;
	}

	/**
	 * @param srcFilter the srcFilter to set
	 */
	public void setSrcFilter(FileFilter srcFilter)
	{
		this.srcFilter = srcFilter;
	}

	/**
	 * @return the dstFilter
	 */
	public FileFilter getDstFilter()
	{
		return dstFilter;
	}

	/**
	 * @param dstFilter the dstFilter to set
	 */
	public void setDstFilter(FileFilter dstFilter)
	{
		this.dstFilter = dstFilter;
	}
	

}
