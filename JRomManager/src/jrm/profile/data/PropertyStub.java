package jrm.profile.data;

import java.io.Serializable;

import jrm.profile.Profile;

/**
 * interface definition for linking selectable data classes with profile properties getter/setter
 * @author optyfr
 *
 */
public interface PropertyStub extends Serializable
{
	/**
	 * get the defined property name of the current class
	 * @return the name of the property
	 */
	public String getPropertyName();

	/**
	 * get the selection state in profile properties according  {@link #getPropertyName()}
	 * @return true if selected
	 */
	public default boolean isSelected()
	{
		return Profile.curr_profile.getProperty(getPropertyName(), true);
	}

	/**
	 * set the selection state in profile properties according {@link #getPropertyName()}
	 * @param selected the selection state to set
	 */
	public default void setSelected(final boolean selected)
	{
		Profile.curr_profile.setProperty(getPropertyName(), selected);
	}

}
