package jrm.server.shared.actions;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import jrm.aui.basic.ResultColUpdater;
import jrm.aui.basic.SrcDstResult;
import jrm.aui.basic.SrcDstResult.SDRList;
import jrm.batch.DirUpdater;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.security.PathAbstractor;
import jrm.server.shared.WebSession;
import jrm.server.shared.Worker;

public class Dat2DirActions
{
	private final ActionsMgr ws;

	public Dat2DirActions(ActionsMgr ws)
	{
		this.ws = ws;
	}

	public void start(JsonObject jso)
	{
		(ws.getSession().setWorker(new Worker(()->{
			WebSession session = ws.getSession();
			boolean dryrun = session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_dry_run, true);
			session.getWorker().progress = new ProgressActions(ws);
			try
			{
				String[] srcdirs = StringUtils.split(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_srcdirs, ""),'|');
				if (srcdirs.length > 0)
				{
					SDRList sdrl =  SrcDstResult.fromJSON(session.getUser().getSettings().getProperty(SettingsEnum.dat2dir_sdr, "[]"));
					if (sdrl.stream().filter(sdr -> !session.getUser().getSettings().getProfileSettingsFile(PathAbstractor.getAbsolutePath(session, sdr.src).toFile()).exists()).count() > 0)
						new GlobalActions(ws).warn(ws.getSession().getMsgs().getString("MainFrame.AllDatsPresetsAssigned")); //$NON-NLS-1$
					else
					{
						new DirUpdater(session, sdrl, session.getWorker().progress, Stream.of(srcdirs).map(s->PathAbstractor.getAbsolutePath(session, s).toFile()).collect(Collectors.toList()), new ResultColUpdater()
						{
							@Override
							public void updateResult(int row, String result)
							{
								sdrl.get(row).result = result;
								session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr, SrcDstResult.toJSON(sdrl));
								session.getUser().getSettings().saveSettings();
								Dat2DirActions.this.updateResult(row, result);
							}
							
							@Override
							public void clearResults()
							{
								sdrl.forEach(sdr -> sdr.result = "");
								session.getUser().getSettings().setProperty(SettingsEnum.dat2dir_sdr, SrcDstResult.toJSON(sdrl));
								session.getUser().getSettings().saveSettings();
								Dat2DirActions.this.clearResults();
							}
						}, dryrun);
					}
				}
				else
					new GlobalActions(ws).warn(ws.getSession().getMsgs().getString("MainFrame.AtLeastOneSrcDir"));
			}
			catch(BreakException e)
			{
				// user cancelled action
			}
			finally
			{
				Dat2DirActions.this.end();
				session.curr_profile = null;
				session.curr_scan = null;
				session.getWorker().progress.close();
				session.getWorker().progress = null;
				session.setLastAction(new Date());
			}
		}))).start();
	}

	public void settings(JsonObject jso)
	{
		JsonArray srcs = jso.get("params").asObject().get("srcs").asArray();
		if(srcs!=null && srcs.size()>0)
		{
			final var src = srcs.get(0).asString();
			try
			{
				final var session = ws.getSession();
				final var settings = ws.getSession().getUser().getSettings().loadProfileSettings(PathAbstractor.getAbsolutePath(session,src).toFile(), null);
				if(ws.isOpen())
				{
					final var msg = new JsonObject();
					msg.add("cmd", "Dat2Dir.showSettings");
					final var params = new JsonObject();
					params.add("settings", settings.asJSO());
					params.add("srcs",srcs);
					msg.add("params", params);
					ws.send(msg.toString());
				}
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(),e);
			}
		}
	}
	
	
	void updateResult(int row, String result)
	{
		try
		{
			if(ws.isOpen())
			{
				final var msg = new JsonObject();
				msg.add("cmd", "Dat2Dir.updateResult");
				final var params = new JsonObject();
				params.add("row", row);
				params.add("result", result);
				msg.add("params", params);
				ws.send(msg.toString());
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
				final var msg = new JsonObject();
				msg.add("cmd", "Dat2Dir.clearResults");
				ws.send(msg.toString());
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
				final var msg = new JsonObject();
				msg.add("cmd", "Dat2Dir.end");
				ws.send(msg.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

}
