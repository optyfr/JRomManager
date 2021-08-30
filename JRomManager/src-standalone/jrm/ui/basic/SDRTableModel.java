package jrm.ui.basic;

import java.io.File;
import java.io.FileFilter;

import javax.swing.event.TableModelEvent;

import jrm.aui.basic.SrcDstResult;
import jrm.aui.basic.SrcDstResult.SDRList;

public abstract class SDRTableModel extends AbstractEnhTableModel
{
	private SDRList data = new SDRList();
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
	
	public void addFile(File file, int row, int col, int i)
	{
		final SrcDstResult line;
		if (row == -1 || row + i >= this.getData().size())
		{
			line = new SrcDstResult();
			this.getData().add(line);
		}
		else
			line = this.getData().get(row + i);
		if (col == 1)
			line.setDst(file.getPath());
		else
			line.setSrc(file.getPath());
	}
}
