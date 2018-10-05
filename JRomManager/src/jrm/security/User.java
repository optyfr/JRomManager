package jrm.security;

import jrm.misc.GlobalSettings;

public class User
{
	public final Session parent;
	
	public final String name;
	
	public final GlobalSettings settings;
	
	public User(final Session parent, final String name)
	{
		this.parent = parent;
		this.parent.user = this;
		this.name = name;
		this.settings = new GlobalSettings(this);
	}

}
