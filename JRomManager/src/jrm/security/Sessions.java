package jrm.security;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

public final @UtilityClass class Sessions
{
	private static @Getter @Setter boolean singleMode = false;
	private static @Getter @Setter Session singleSession = null;

	private static final Map<String, Session> sessionsMap = new HashMap<>();

	public static Session getSession(boolean multiuser, boolean noupdate)
	{
		assert singleMode;
		if (singleSession == null)
			singleSession = new Session(multiuser, noupdate);
		return singleSession;
	}

	public static Session getSession(String session)
	{
		assert !singleMode;
		return sessionsMap.get(session);
	}

	public static void setSession(String session)
	{
		assert !singleMode;
		sessionsMap.putIfAbsent(session, new Session(session));
	}
}
