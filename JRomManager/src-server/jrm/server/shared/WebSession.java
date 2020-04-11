package jrm.server.shared;

import java.io.Closeable;
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

public class WebSession extends Session implements Closeable
{
	private static Map<String,WebSession> allSessions = new ConcurrentHashMap<>();
	private static @Getter boolean terminate = false;
	public BlockingDeque<String> lprMsg = new LinkedBlockingDeque<>();

	public Worker worker = null;
	public Date lastAction = new Date();
	
	public Report tmp_report = null;
	public TrntChkReport tmp_tc_report = null;
	public TreeMap<Integer,Path> tmp_profile_lst = null;
	public TreeMap<String,FileResult> tmp_compressor_lst = null;
	
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
		if(lprMsg.isEmpty())
			lprMsg.add("");
		allSessions.remove(getSessionId());
	}
	
	public static void closeAll()
	{
		terminate = true;
		allSessions.forEach((k,s)->s.close());
	}
	
	

}
