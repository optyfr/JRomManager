package jrm.profile.data;

import java.io.Serializable;

import jrm.profile.Profile;

public interface PropertyStub extends Serializable
{
	public String getPropertyName();

	public default boolean isSelected()
	{
		return Profile.curr_profile.getProperty(getPropertyName(), true);
	}

	public default void setSelected(final boolean selected)
	{
		Profile.curr_profile.setProperty(getPropertyName(), selected);
	}

}
