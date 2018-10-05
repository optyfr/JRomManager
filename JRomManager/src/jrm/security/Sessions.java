package jrm.security;

import java.util.HashMap;
import java.util.Map;

public final class Sessions
{
	public static boolean single_mode = false;
	public static Session single_session = null;

	private static Map<String, Session> sessions = new HashMap<>();

	public static Session getSession()
	{
		assert single_mode == true;
		if (single_session == null)
			single_session = new Session();
		return single_session;
	}

	public static Session getSession(String session)
	{
		assert single_mode == false;
		return sessions.get(session);
	}

	public static void setSession(String session)
	{
		assert single_mode == false;
		sessions.putIfAbsent(session, new Session(session));
	}
}
