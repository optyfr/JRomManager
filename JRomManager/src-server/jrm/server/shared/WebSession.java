package jrm.server.shared;

import java.nio.file.Path;
import java.util.Date;
import java.util.TreeMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import jrm.batch.Compressor.FileResult;
import jrm.batch.TrntChkReport;
import jrm.profile.report.Report;
import jrm.security.Session;

public class WebSession extends Session
{
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
	}

}
