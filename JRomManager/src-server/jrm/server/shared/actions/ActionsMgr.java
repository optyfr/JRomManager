package jrm.server.shared.actions;

import java.io.IOException;
import java.util.Date;

import com.eclipsesource.json.JsonObject;

import jrm.misc.Log;
import jrm.server.shared.SessionStub;
import jrm.server.shared.WebSession;

public interface ActionsMgr extends SessionStub
{
	public void send(String msg) throws IOException;

	public boolean isOpen();

	public WebSession getSession();

	public default void processActions(ActionsMgr mgr, JsonObject jso)
	{
		try
		{
			if (jso != null)
			{
				mgr.getSession().setLastAction(new Date());
				switch (jso.getString("cmd", "unknown"))
				{
					case "Global.setProperty":
					{
						new GlobalActions(this).setProperty(jso);
						break;
					}
					case "Global.getMemory":
					{
						new GlobalActions(this).setMemory(jso);
						break;
					}
					case "Global.GC":
					{
						new GlobalActions(this).gc(jso);
						break;
					}
					case "Profile.import":
					{
						new ProfileActions(this).imprt(jso);
						break;
					}
					case "Profile.load":
					{
						new ProfileActions(this).load(jso);
						break;
					}
					case "Profile.scan":
					{
						new ProfileActions(this).scan(jso, true);
						break;
					}
					case "Profile.fix":
					{
						new ProfileActions(this).fix(jso);
						break;
					}
					case "Profile.importSettings":
					{
						new ProfileActions(this).importSettings(jso);
						break;
					}
					case "Profile.exportSettings":
					{
						new ProfileActions(this).exportSettings(jso);
						break;
					}
					case "Profile.setProperty":
					{
						new ProfileActions(this).setProperty(jso);
						break;
					}
					case "ReportLite.setFilter":
					{
						new ReportActions(this).setFilter(jso, true);
						break;
					}
					case "Report.setFilter":
					{
						new ReportActions(this).setFilter(jso, false);
						break;
					}
					case "CatVer.load":
					{
						new CatVerActions(this).load(jso);
						break;
					}
					case "NPlayers.load":
					{
						new NPlayersActions(this).load(jso);
						break;
					}
					case "Progress.cancel":
					{
						if (mgr.getSession().getWorker() != null && mgr.getSession().getWorker().isAlive() && mgr.getSession().getWorker().progress != null)
							mgr.getSession().getWorker().getProgress().cancel();
						break;
					}
					case "Dat2Dir.start":
					{
						new Dat2DirActions(this).start(jso);
						break;
					}
					case "Dir2Dat.start":
					{
						new Dir2DatActions(this).start(jso);
						break;
					}
					case "TrntChk.start":
					{
						new TrntChkActions(this).start(jso);
						break;
					}
					case "Compressor.start":
					{
						new CompressorActions(this).start(jso);
						break;
					}
					case "Dat2Dir.settings":
					{
						new Dat2DirActions(this).settings(jso);
						break;
					}
					default:
						System.err.println("Unknown command : " + jso.getString("cmd", "unknown"));
						break;
				}
			}
		}
		catch (Exception e)
		{
			System.err.println(jso.toString());
			Log.err(e.getMessage(), e);
		}
	}

}
