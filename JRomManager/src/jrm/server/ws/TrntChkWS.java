package jrm.server.ws;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.eclipsesource.json.JsonObject;

import jrm.batch.TorrentChecker;
import jrm.io.torrent.options.TrntChkMode;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.server.WebSession;
import jrm.ui.basic.ResultColUpdater;
import jrm.ui.basic.SrcDstResult;

public class TrntChkWS
{
	private final WebSckt ws;

	public TrntChkWS(WebSckt ws)
	{
		this.ws = ws;
	}

	void start(JsonObject jso)
	{
		(ws.session.worker = new Worker(()->{
			WebSession session = ws.session;
			final TrntChkMode mode = TrntChkMode.valueOf(session.getUser().settings.getProperty("trntchk.mode", "FILENAME"));
			final boolean removeUnknownFiles = session.getUser().settings.getProperty("trntchk.remove_unknown_files", false);
			final boolean removeWrongSizedFiles = session.getUser().settings.getProperty("trntchk.remove_wrong_sized_files", false);
			final boolean detectArchivedFolders = session.getUser().settings.getProperty("trntchk.detect_archived_folders", true);

			session.worker.progress = new ProgressWS(ws);
			try
			{
				List<SrcDstResult> sdrl =  SrcDstResult.fromJSON(session.getUser().settings.getProperty("trntchk.sdr", "[]"));
				try
				{
					new TorrentChecker(session, session.worker.progress, sdrl, mode, new ResultColUpdater()
					{
						@Override
						public void updateResult(int row, String result)
						{
							sdrl.get(row).result = result;
							session.getUser().settings.setProperty("trntchk.sdr", SrcDstResult.toJSON(sdrl));
							TrntChkWS.this.updateResult(row, result);
						}
						
						@Override
						public void clearResults()
						{
							sdrl.forEach(sdr -> sdr.result = "");
							session.getUser().settings.setProperty("trntchk.sdr", SrcDstResult.toJSON(sdrl));
							TrntChkWS.this.clearResults();
						}
					}, removeUnknownFiles, removeWrongSizedFiles, detectArchivedFolders);
				}
				catch (IOException e)
				{
					Log.err(e.getMessage(),e);
				}
			}
			catch(BreakException e)
			{
				
			}
			finally
			{
				TrntChkWS.this.end();
				session.curr_profile = null;
				session.curr_scan = null;
				session.worker.progress.close();
				session.worker.progress = null;
				session.lastAction = new Date();
			}
		})).start();
	}
	
	@SuppressWarnings("serial")
	void updateResult(int row, String result)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "TrntChk.updateResult");
					add("params", new JsonObject() {{
						add("row", row);
						add("result", result);
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	@SuppressWarnings("serial")
	void clearResults()
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "TrntChk.clearResults");
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	@SuppressWarnings("serial")
	void end()
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "TrntChk.end");
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

}
