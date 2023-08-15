package jrm.ui.profile.data;

import java.util.Set;

import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.AnywareStatus;
import jrm.ui.basic.AbstractEnhTableModel;

public abstract class AnywareListModel<T extends Anyware> extends AbstractEnhTableModel
{
	
	/**
	 * filter then fire a TableChanged event to listeners
	 * @param filter the new {@link Set} of {@link AnywareStatus} filter to apply
	 */
	public abstract void setFilter(final Set<AnywareStatus> filter);

	
	public abstract void reset();
	
	public abstract AnywareList<T> getList();	//NOSONAR
}
