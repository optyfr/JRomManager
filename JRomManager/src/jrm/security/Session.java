package jrm.security;

import jrm.profile.Profile;
import jrm.profile.report.Report;

public class Session
{
	private final String sessionId;
	
	User user = null;

	// Extra settings coming from cmdline args
	public boolean multiuser = false;
	public boolean noupdate = false;

	public final Report report  = new Report();
	
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
		if(user==null)
			user = new User(this, "JRomManager");
		return user;
	}
	
	public String getSessionId()
	{
		return sessionId;
	}

}
