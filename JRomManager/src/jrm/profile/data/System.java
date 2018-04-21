package jrm.profile.data;

import jrm.profile.Profile;

public interface System
{
	public enum Type
	{
		STANDARD,
		MECHANICAL,
		DEVICE,
		BIOS,
		SOFTWARELIST
	}
	
	public Type getType();
	public System getSystem();
	public String getName();
	public default boolean isSelected()
	{
		return Profile.curr_profile.getProperty("filter."+getName(), true);
	}
	public default void setSelected(boolean selected)
	{
		Profile.curr_profile.setProperty("filter."+getName(), selected);
	}
}
