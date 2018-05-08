package jrm.profile.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

@SuppressWarnings("serial")
public final class Years extends AbstractListModel<String> implements ComboBoxModel<String>, Serializable
{
	final ArrayList<String> years;
	Object selectedObject = null;

	public Years(Collection<String> years)
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
	public void setSelectedItem(Object anObject)
	{
		if((selectedObject != null && !selectedObject.equals(anObject)) || selectedObject == null && anObject != null)
		{
			selectedObject = anObject;
			fireContentsChanged(this, -1, -1);
		}
	}

	// implements javax.swing.ComboBoxModel
	public Object getSelectedItem()
	{
		return selectedObject;
	}

	// implements javax.swing.ListModel
	public int getSize()
	{
		return years.size();
	}

	// implements javax.swing.ListModel
	public String getElementAt(int index)
	{
		if(index >= 0 && index < years.size())
			return years.get(index);
		else
			return null;
	}

	/**
	 * Returns the index-position of the specified object in the list.
	 *
	 * @param anObject
	 * @return an int representing the index position, where 0 is the first position
	 */
	public int getIndexOf(Object anObject)
	{
		return years.indexOf(anObject);
	}

}
