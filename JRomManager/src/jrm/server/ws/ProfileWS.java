package jrm.server.ws;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

import jrm.misc.BreakException;
import jrm.misc.ProfileSettings;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.scan.Scan;
import jrm.server.WebSession;

public class ProfileWS
{
	private final WebSckt ws;

	public ProfileWS(WebSckt ws)
	{
		this.ws = ws;
	}

	void load(JsonObject jso)
	{
		(ws.session.worker = new Worker(()->{
			WebSession session = ws.session;
			if (session.curr_profile != null)
				session.curr_profile.saveSettings();
			session.worker.progress = new ProgressWS(ws);
			try
			{
				JsonObject jsobj = jso.get("params").asObject();
				session.curr_profile = Profile.load(session, new File(new File(jsobj.getString("parent", null)), jsobj.getString("file", null)), session.worker.progress);
				session.curr_profile.nfo.save(session);
				session.report.setProfile(session.curr_profile);
			}
			catch(BreakException ex)
			{
			}
			session.worker.progress.close();
			session.worker.progress = null;
			session.lastAction = new Date();
			loaded(session.curr_profile);
			new CatVerWS(ws).loaded(session.curr_profile);
			new NPlayersWS(ws).loaded(session.curr_profile);
		})).start();
	}
	
	void scan(JsonObject jso)
	{
		(ws.session.worker = new Worker(()->{
			WebSession session = ws.session;
			session.worker.progress = new ProgressWS(ws);
			try
			{
				session.curr_scan = new Scan(session.curr_profile, session.worker.progress);
			}
			catch(BreakException ex)
			{
			}
			session.worker.progress.close();
			session.worker.progress = null;
			session.lastAction = new Date();
			scanned(session.curr_scan);
		})).start();
	}
	
	void fix(JsonObject jso)
	{
		(ws.session.worker = new Worker(()->{
			WebSession session = ws.session;
			session.worker.progress = new ProgressWS(ws);
			try
			{
				if(session.curr_profile.hasPropsChanged())
				{
	/*				switch (JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(ScannerPanel.this), Messages.getString("MainFrame.WarnSettingsChanged"), Messages.getString("MainFrame.RescanBeforeFix"), JOptionPane.YES_NO_CANCEL_OPTION)) //$NON-NLS-1$ //$NON-NLS-2$
					{
						case JOptionPane.YES_OPTION:
							session.curr_scan = new Scan(session.curr_profile, progress);
							btnFix.setEnabled(session.curr_scan.actions.stream().mapToInt(Collection::size).sum() > 0);
							if (!btnFix.isEnabled())
								return null;
							break;
						case JOptionPane.NO_OPTION:
							break;
						case JOptionPane.CANCEL_OPTION:
						default:
							return null;
					}*/
					session.curr_scan = new Scan(session.curr_profile, session.worker.progress);
					boolean needfix = session.curr_scan.actions.stream().mapToInt(Collection::size).sum() > 0;
					if (!needfix)
						return;
				}
				final Fix fix = new Fix(session.curr_profile, session.curr_scan, session.worker.progress);
				fixed(fix);
			}
			finally
			{
				session.worker.progress.close();
				session.worker.progress = null;
				session.lastAction = new Date();
			}
		})).start();
	}
	
	void setProperty(JsonObject jso)
	{
		final String profile = jso.getString("profile", null);
		ProfileSettings settings = profile != null ? new ProfileSettings() : ws.session.curr_profile.settings;
		JsonObject pjso = jso.get("params").asObject();
		for (Member m : pjso)
		{
			JsonValue value = m.getValue();
			if (value.isBoolean())
				settings.setProperty(m.getName(), value.asBoolean());
			else if (value.isString())
				settings.setProperty(m.getName(), value.asString());
			else
				settings.setProperty(m.getName(), value.toString());
		}
		try
		{
			if (profile != null)
				ws.session.getUser().settings.saveProfileSettings(new File(profile), settings);
			else
				ws.session.curr_profile.saveSettings();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("serial")
	void loaded(final Profile profile)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Profile.loaded");
					add("params", new JsonObject() {{
						add("success", profile!=null);
						if(profile!=null)
						{
							add("name", profile.getName());
							if(profile.systems!=null)
							{
								add("systems", new JsonArray() {{
									profile.systems.forEach(s-> add(new JsonObject() {{
										add("name", s.toString());
										add("selected", s.isSelected(profile));
										add("property", s.getPropertyName());
										add("type", s.getType().toString());
									}}));
								}});
							}
							add("years", new JsonArray() {{
								ArrayList<String> arrlst = new ArrayList<String>(profile.years);
								arrlst.sort(String::compareTo); 
								arrlst.forEach(s->add(s));
							}});
							if(profile.settings!=null)
								add("settings",profile.settings.asJSO());
						}
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	
	@SuppressWarnings("serial")
	void scanned(final Scan scan)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Profile.scanned");
					add("params", new JsonObject() {{
						add("success", scan!=null);
						if(scan!=null)
							add("actions", scan.actions.stream().mapToInt(Collection::size).sum());
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("serial")
	void fixed(final Fix fix)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Profile.fixed");
					add("params", new JsonObject() {{
						add("success", fix!=null);
						if(fix!=null)
							add("actions", fix.getActionsRemain());
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
