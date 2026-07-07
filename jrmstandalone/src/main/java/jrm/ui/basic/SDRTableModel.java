package jrm.ui.basic;

import java.io.File;
import java.io.FileFilter;

import javax.swing.event.TableModelEvent;

import jrm.aui.basic.SDRList;
import jrm.aui.basic.SrcDstResult;

/**
 * Abstract table model for managing source-destination-result (SDR) data in a table.
 * <p>
 * This model extends {@link AbstractEnhTableModel} to provide data management for table views
 * that display source/destination file pairs along with their comparison results. It tracks
 * the current hover position (row/column) for drag-and-drop highlighting and supports
 * configurable file filters for source and destination columns.
 * </p>
 *
 * @see AbstractEnhTableModel
 * @see SrcDstResult
 * @see SDRList
 */
public abstract class SDRTableModel extends AbstractEnhTableModel {
    /** The list of source-destination-result data entries. */
    private SDRList<SrcDstResult> data = new SDRList<>();
    /** The file filter applied to source column drag-and-drop operations. */
    private FileFilter srcFilter = null;
    /** The file filter applied to destination column drag-and-drop operations. */
    private FileFilter dstFilter = null;

    /** The currently hovered row index during drag operations, or {@code -1} if none. */
    private int currentRow;
    /** The currently hovered column index during drag operations. */
    private int currentCol;

    /**
     * Returns the currently hovered row index.
     *
     * @return the current row index, or {@code -1} if no row is hovered
     */
    public int getCurrentRow() {
        return currentRow;
    }

    /**
     * Sets the currently hovered row index.
     *
     * @param currentRow the row index to set as current
     * @return the updated current row index
     */
    public int setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
        return currentRow;
    }

    /**
     * Returns the currently hovered column index.
     *
     * @return the current column index
     */
    public int getCurrentCol() {
        return currentCol;
    }

    /**
     * Sets the currently hovered column index.
     *
     * @param currentCol the column index to set as current
     * @return the updated current column index
     */
    public int setCurrentCol(int currentCol) {
        this.currentCol = currentCol;
        return currentCol;
    }

    /**
     * Returns the underlying data list.
     *
     * @return the {@link SDRList} of {@link SrcDstResult} entries
     */
    public SDRList<SrcDstResult> getData() {
        return data;
    }

    /**
     * Replaces the data list and fires a table change event.
     * <p>
     * Resets the current row to {@code -1} before setting the new data.
     * </p>
     *
     * @param data the new {@link SDRList} of {@link SrcDstResult} entries
     */
    public void setData(SDRList<SrcDstResult> data) {
        currentRow = -1;
        this.data = data;
        fireTableChanged(new TableModelEvent(this));
    }

    /**
     * Returns the file filter for source column drag-and-drop operations.
     *
     * @return the source {@link FileFilter}, or {@code null} if no filter is set
     */
    public FileFilter getSrcFilter() {
        return srcFilter;
    }

    /**
     * Sets the file filter for source column drag-and-drop operations.
     *
     * @param srcFilter the source {@link FileFilter} to set, or {@code null} to clear
     */
    public void setSrcFilter(FileFilter srcFilter) {
        this.srcFilter = srcFilter;
    }

    /**
     * Returns the file filter for destination column drag-and-drop operations.
     *
     * @return the destination {@link FileFilter}, or {@code null} if no filter is set
     */
    public FileFilter getDstFilter() {
        return dstFilter;
    }

    /**
     * Sets the file filter for destination column drag-and-drop operations.
     *
     * @param dstFilter the destination {@link FileFilter} to set, or {@code null} to clear
     */
    public void setDstFilter(FileFilter dstFilter) {
        this.dstFilter = dstFilter;
    }

    /**
     * Adds a file to the data at the specified position.
     * <p>
     * If the row is {@code -1} or beyond the current data size, a new {@link SrcDstResult} is created
     * and appended. The file path is set as either the source or destination based on the column index.
     * </p>
     *
     * @param file the {@link File} to add
     * @param row the target row index, or {@code -1} to append
     * @param col the column index ({@code 0} for source, {@code 1} for destination)
     * @param i the offset from the row for batch insertions
     */
    public void addFile(File file, int row, int col, int i) {
        final SrcDstResult line;
        if (row == -1 || row + i >= this.getData().size()) {
            line = new SrcDstResult();
            this.getData().add(line);
        } else
            line = this.getData().get(row + i);
        if (col == 1)
            line.setDst(file.getPath());
        else
            line.setSrc(file.getPath());
    }
}
