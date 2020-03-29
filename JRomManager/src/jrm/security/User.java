package jrm.security;

import jrm.misc.GlobalSettings;
import lombok.Getter;

public class User
{
	private final @Getter Session session;
	
	private final @Getter String name;
	
	private final @Getter GlobalSettings settings;
	
	public User(final Session session, final String name)
	{
		this.session = session;
		this.session.user = this;
		this.name = name;
		this.settings = new GlobalSettings(this);
	}
}
