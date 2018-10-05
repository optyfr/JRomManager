package jrm.server;

import jrm.security.Session;

public interface SessionStub
{
	public Session getSession();
	public void setSession(Session session);
	public void unsetSession(Session session);
}
