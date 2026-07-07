package jrm.ui.profile.data;

import java.util.Set;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import jrm.profile.data.AnywareStatus;
import jrm.ui.basic.AbstractEnhTableModel;

/**
 * Abstract base class for anyware list-of-lists table models.
 * <p>
 * Provides common functionality for displaying aggregated statistics
 * of multiple anyware lists in a single table.
 */
public abstract class AnywareListListModel extends AbstractEnhTableModel {
    /** Resets the model and refreshes the table. */
    public abstract void reset();

    /**
     * Applies a filter and refreshes the table.
     *
     * @param filter the new {@link Set} of {@link AnywareStatus} filter to apply
     */
    public abstract void setFilter(final Set<AnywareStatus> filter);

    @Override
    public TableCellRenderer[] getCellRenderers() {
        return AnywareListListRenderer.columnsRenderers;
    }

    @Override
    public int getColumnWidth(int columnIndex) {
        return AnywareListListRenderer.columnsWidths[columnIndex];
    }

    @Override
    public String getColumnTT(int columnIndex) {
        return AnywareListListRenderer.columns[columnIndex];
    }

    @Override
    public int getColumnCount() {
        return AnywareListListRenderer.columns.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return AnywareListListRenderer.columns[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return AnywareListListRenderer.columnsTypes[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        // do nothing
    }

    @Override
    public TableCellRenderer getColumnRenderer(int columnIndex) {
        return AnywareListListRenderer.columnsRenderers[columnIndex] != null ? AnywareListListRenderer.columnsRenderers[columnIndex] : new DefaultTableCellRenderer();
    }

}
