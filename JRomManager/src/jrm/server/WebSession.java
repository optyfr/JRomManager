package jrm.server;

import java.util.Date;

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
	
	public WebSession(String sessionId)
	{
		super(sessionId);
	}

}
