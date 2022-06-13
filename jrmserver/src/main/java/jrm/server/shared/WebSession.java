package jrm.server.shared;

import java.io.Closeable;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import jrm.batch.Compressor.FileResult;
import jrm.batch.TrntChkReport;
import jrm.profile.report.Report;
import jrm.security.Session;
import lombok.Getter;
import lombok.Setter;

public class WebSession extends Session implements Closeable, Serializable
{
	private static final long serialVersionUID = 1L;
	
	private static Map<String, WebSession> allSessions = new ConcurrentHashMap<>();
	private static @Getter boolean terminate = false;
	private @Getter BlockingDeque<String> lprMsg = new LinkedBlockingDeque<>();

	private transient @Getter Worker worker = null;
	
	public Worker setWorker(Worker worker)
	{
		this.worker = worker;
		return this.worker;
	}
	
	private @Getter @Setter Date lastAction = new Date();

	private transient @Getter @Setter Report tmpReport = null;
	private transient @Getter @Setter TrntChkReport tmpTCReport = null;
	private transient TreeMap<Integer, Path> cachedProfileList = null;
	private transient @Getter TreeMap<String, FileResult> cachedCompressorList = new TreeMap<>();

	public WebSession(String sessionId)
	{
		super(sessionId);
		allSessions.put(sessionId, this);
	}

	public WebSession(String sessionId, String user, String[] roles)
	{
		super(sessionId, user, roles);
		allSessions.put(sessionId, this);
	}

	@Override
	public void close()
	{
		if (lprMsg.isEmpty())
			lprMsg.add("");
		allSessions.remove(getSessionId());
	}

	public static void closeAll()
	{
		terminate = true;
		allSessions.forEach((k, s) -> s.close());
	}

	public void putProfileList(Integer id, Path path)
	{
		cachedProfileList.put(id, path);
	}

	public void removeProfileList(Integer id)
	{
		cachedProfileList.remove(id);
	}

	public void newProfileList()
	{
		cachedProfileList = new TreeMap<>();
	}

	public Integer getLastProfileListKey()
	{
		return cachedProfileList != null && !cachedProfileList.isEmpty() ? cachedProfileList.lastKey() : 0;
	}

	public Path getProfileList(Integer id)
	{
		return cachedProfileList.get(id);
	}
}
