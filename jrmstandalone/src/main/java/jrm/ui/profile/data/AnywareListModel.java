package jrm.ui.profile.data;

import java.util.Set;

import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.AnywareStatus;
import jrm.ui.basic.AbstractEnhTableModel;

/**
 * Abstract base class for anyware list table models.
 * <p>
 * Provides common functionality for displaying anyware lists in tables.
 */
public abstract class AnywareListModel<T extends Anyware> extends AbstractEnhTableModel {

    /**
     * Applies a filter and fires a TableChanged event to listeners.
     * 
     * @param filter the new {@link Set} of {@link AnywareStatus} filter to apply
     */
    public abstract void setFilter(final Set<AnywareStatus> filter);

    /** Resets the model and refreshes the table. */
    public abstract void reset();

    /**
     * Returns the underlying anyware list.
     * @return the anyware list
     */
    public abstract AnywareList<T> getList(); // NOSONAR
}
