package jrm.security;

import java.util.ResourceBundle;

import jrm.locale.Messages;
import jrm.profile.Profile;
import jrm.profile.report.Report;
import jrm.profile.scan.Scan;
import lombok.Getter;

public class Session
{
	private final String sessionId;

	User user = null;

	public @Getter ResourceBundle msgs = null;

	// Extra settings coming from cmdline args
	public boolean server = false;
	public boolean multiuser = false;
	public boolean noupdate = false;

	public final Report report = new Report();

	/**
	 * This contain the current loaded profile
	 */
	public @Getter Profile curr_profile = null;

	/** The curr scan. */
	public Scan curr_scan;

	Session(boolean multiuser, boolean noupdate)
	{
		this.multiuser = multiuser;
		this.noupdate = noupdate;
		this.sessionId = null;
		user = new User(this, "JRomManager", new String[] {"admin"});
		msgs = Messages.getBundle();
	}

	public Session(String sessionId)
	{
		this.server = true;
		this.sessionId = sessionId;
	}

	public Session(String sessionId, String user, String[] roles)
	{
		this.multiuser = true;
		this.server = true;
		this.sessionId = sessionId;
		this.user = new User(this, user==null?"server":user, roles==null?new String[] {"admin"}:roles);
	}

	public User getUser()
	{
		if (user == null)
			user = new User(this, "JRomManager", new String[] {"admin"});
		return user;
	}
	
	public void setUser(String user, String[] roles)
	{
		this.user = new User(this, user, roles);
	}

	public String getSessionId()
	{
		return sessionId;
	}

}
