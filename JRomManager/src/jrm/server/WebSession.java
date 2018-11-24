package jrm.server;

import java.nio.file.Path;
import java.util.Date;
import java.util.TreeMap;

import jrm.batch.TrntChkReport;
import jrm.profile.report.Report;
import jrm.security.Session;
import jrm.server.ws.Worker;

public class WebSession extends Session
{
	public Worker worker = null;
	public Date lastAction = new Date();
	public Report tmp_report = null;
	public TrntChkReport tmp_tc_report = null;
	public TreeMap<Integer,Path> tmp_profile_lst = null;
	
	public WebSession(String sessionId)
	{
		super(sessionId);
	}

}
