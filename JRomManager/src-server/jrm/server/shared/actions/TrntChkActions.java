package jrm.server.shared.actions;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;

import com.eclipsesource.json.JsonObject;

import jrm.aui.basic.ResultColUpdater;
import jrm.aui.basic.SrcDstResult;
import jrm.aui.basic.SrcDstResult.SDRList;
import jrm.batch.TorrentChecker;
import jrm.io.torrent.options.TrntChkMode;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.server.shared.WebSession;
import jrm.server.shared.Worker;

public class TrntChkActions
{
	private final ActionsMgr ws;

	public TrntChkActions(ActionsMgr ws)
	{
		this.ws = ws;
	}

	@SuppressWarnings("exports")
	public void start(JsonObject jso)
	{
		(ws.getSession().setWorker(new Worker(()->{
			WebSession session = ws.getSession();
			final var mode = TrntChkMode.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_mode));
			final var opts = EnumSet.noneOf(TorrentChecker.Options.class);
			if (session.getUser().getSettings().getProperty(SettingsEnum.trntchk_remove_unknown_files, Boolean.class))
				opts.add(TorrentChecker.Options.REMOVEUNKNOWNFILES);
			if (session.getUser().getSettings().getProperty(SettingsEnum.trntchk_remove_wrong_sized_files, Boolean.class))
				opts.add(TorrentChecker.Options.REMOVEWRONGSIZEDFILES);
			if (session.getUser().getSettings().getProperty(SettingsEnum.trntchk_detect_archived_folders, Boolean.class))
				opts.add(TorrentChecker.Options.DETECTARCHIVEDFOLDERS);

			session.getWorker().progress = new ProgressActions(ws);
			try
			{
				SDRList sdrl =  SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(SettingsEnum.trntchk_sdr));
				new TorrentChecker(session, session.getWorker().progress, sdrl, mode, new ResultColUpdater()
				{
					@Override
					public void updateResult(int row, String result)
					{
						sdrl.get(row).setResult(result);
						session.getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr, SrcDstResult.toJSON(sdrl));
						session.getUser().getSettings().saveSettings();
						TrntChkActions.this.updateResult(row, result);
					}
					
					@Override
					public void clearResults()
					{
						sdrl.forEach(sdr -> sdr.setResult(""));
						session.getUser().getSettings().setProperty(SettingsEnum.trntchk_sdr, SrcDstResult.toJSON(sdrl));
						session.getUser().getSettings().saveSettings();
						TrntChkActions.this.clearResults();
					}
				}, opts);
			}
			catch(BreakException e)
			{
				// user cancelled action
			}
			finally
			{
				TrntChkActions.this.end();
				session.getWorker().progress.close();
				session.getWorker().progress = null;
				session.setLastAction(new Date());
			}
		}))).start();
	}
	
	void updateResult(int row, String result)
	{
		try
		{
			if(ws.isOpen())
			{
				final var rjso = new JsonObject();
				rjso.add("cmd", "TrntChk.updateResult");
				final var params = new JsonObject();
				params.add("row", row);
				params.add("result", result);
				rjso.add("params", params);
				ws.send(rjso.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	void clearResults()
	{
		try
		{
			if(ws.isOpen())
			{
				final var rjso = new JsonObject();
				rjso.add("cmd", "TrntChk.clearResults");
				ws.send(rjso.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	void end()
	{
		try
		{
			if(ws.isOpen())
			{
				final var rjso = new JsonObject();
				rjso.add("cmd", "TrntChk.end");
				ws.send(rjso.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

}
