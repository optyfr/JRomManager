package jrm.security;

import java.util.ResourceBundle;

import jrm.locale.Messages;
import jrm.profile.Profile;
import jrm.profile.report.Report;
import jrm.profile.scan.Scan;
import lombok.Getter;
import lombok.Setter;

public class Session
{
	private static final String ADMIN = "admin";

	private @Setter String sessionId;

	protected User user = null;

	private @Getter @Setter ResourceBundle msgs = null;

	// Extra settings coming from cmdline args
	private @Getter boolean server = false;
	private @Getter boolean multiuser = false;
	private @Getter boolean noupdate = false;

	private final @Getter Report report = new Report();

	/**
	 * This contain the current loaded profile
	 */
	private @Getter @Setter Profile currProfile = null;

	/** The curr scan. */
	private @Getter @Setter Scan currScan;

	protected Session()
	{
		
	}
	
	Session(boolean multiuser, boolean noupdate)
	{
		this.multiuser = multiuser;
		this.noupdate = noupdate;
		this.sessionId = null;
		user = new User(this, "JRomManager", new String[] {ADMIN});
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
		this.user = new User(this, user==null?"server":user, roles==null?new String[] {ADMIN}:roles);
	}

	public User getUser()
	{
		if (user == null)
			user = new User(this, "JRomManager", new String[] {ADMIN});
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
