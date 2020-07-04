package jrm.ui.basic;

import java.io.FileFilter;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import jrm.ui.basic.SrcDstResult.SDRList;

public abstract class SDRTableModel implements EnhTableModel
{
	private SDRList data = new SDRList();
    private final EventListenerList listenerList = new EventListenerList();
    private FileFilter srcFilter = null;
    private FileFilter dstFilter = null;


	private int current_row;
	private int current_col;
	
	/**
	 * @return the current_row
	 */
	public int getCurrentRow()
	{
		return current_row;
	}

	/**
	 * @param current_row the current_row to set
	 * @return the current row
	 */
	public int setCurrentRow(int current_row)
	{
		this.current_row = current_row;
		return current_row;
	}

	/**
	 * @return the current_col
	 */
	public int getCurrentCol()
	{
		return current_col;
	}

	/**
	 * @param current_col the current_col to set
	 * @return the current col
	 */
	public int setCurrentCol(int current_col)
	{
		this.current_col = current_col;
		return current_col;
	}

	/**
	 * @return the data
	 */
	public SDRList getData()
	{
		return data;
	}

	/**
	 * @param data initialize data
	 */
	public void setData(SDRList data)
	{
		current_row = -1;
		this.data = data;
		fireTableChanged(new TableModelEvent(this));
	}

	@Override
	public void addTableModelListener(final TableModelListener l)
	{
		listenerList.add(TableModelListener.class, l);
	}

	@Override
	public void removeTableModelListener(final TableModelListener l)
	{
		listenerList.remove(TableModelListener.class, l);
	}

	/**
	 * Sends TableChanged event to listeners
	 * @param e the {@link TableModelEvent} to send
	 */
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
