package jrm.profile.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * ListModel of years
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class Years extends AbstractListModel<String> implements ComboBoxModel<String>, Serializable
{
	/**
	 * The internal list of years
	 */
	final ArrayList<String> years;
	/**
	 * The currently selected object
	 */
	Object selectedObject = null;

	/**
	 * Constructor, will build a sorted list of years
	 * @param years a {@link Collection} of years {@link String}s
	 */
	public Years(final Collection<String> years)
	{
		this.years = new ArrayList<>(years);
		this.years.sort(String::compareTo);
	}

	// implements javax.swing.ComboBoxModel
	/**
	 * Set the value of the selected item. The selected item may be null.
	 *
	 * @param anObject
	 *            The combo box value or null for no selection.
	 */
	@Override
	public void setSelectedItem(final Object anObject)
	{
		if((selectedObject != null && !selectedObject.equals(anObject)) || selectedObject == null && anObject != null)
		{
			selectedObject = anObject;
			fireContentsChanged(this, -1, -1);
		}
	}

	// implements javax.swing.ComboBoxModel
	@Override
	public Object getSelectedItem()
	{
		return selectedObject;
	}

	// implements javax.swing.ListModel
	@Override
	public int getSize()
	{
		return years.size();
	}

	// implements javax.swing.ListModel
	@Override
	public String getElementAt(final int index)
	{
		if(index >= 0 && index < years.size())
			return years.get(index);
		else
			return null;
	}

	/**
	 * Returns the index-position of the specified object in the list.
	 *
	 * @param anObject The combo box value
	 * 
	 * @return an int representing the index position, where 0 is the first position
	 */
	public int getIndexOf(final Object anObject)
	{
		return years.indexOf(anObject);
	}

}
