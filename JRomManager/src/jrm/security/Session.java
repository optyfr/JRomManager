package jrm.security;

import jrm.profile.Profile;

public class Session
{
	private final String sessionId;
	
	private User user = null;

	// Extra settings coming from cmdline args
	public boolean multiuser = false;
	public boolean noupdate = false;

	/**
	 * This contain the current loaded profile
	 */
	public Profile curr_profile = null;

	Session()
	{
		this.sessionId = null;
		user = new User(this,"JRomManager");
	}
	
	public Session(String sessionId)
	{
		this.sessionId = sessionId;
	}
	
	public User getUser()
	{
		return user;
	}
	
	public String getSessionId()
	{
		return sessionId;
	}

}
