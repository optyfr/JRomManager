package jrm.security;

import java.util.HashMap;
import java.util.Map;

public final class Sessions
{
	public static boolean single_mode = false;
	public static Session single_session = new Session();
	
	private static Map<String,Session> sessions = new HashMap<>();

	public static Session getSession(String session)
	{
		return sessions.get(session);
	}
	
	public static void setSession(String session)
	{
		sessions.putIfAbsent(session, new Session(session));
	}
}
