package jrm.server.ws;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

import jrm.misc.BreakException;
import jrm.misc.FindCmd;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.misc.ProfileSettings;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.manager.Import;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.Scan;
import jrm.profile.scan.options.ScanAutomation;
import jrm.server.WebSession;

public class ProfileWS
{
	private final WebSckt ws;

	public ProfileWS(WebSckt ws)
	{
		this.ws = ws;
	}
	
	void imprt(JsonObject jso)
	{
		(ws.session.worker = new Worker(()->{
			WebSession session = ws.session;
			session.worker.progress = new ProgressWS(ws);
			session.worker.progress.canCancel(false);
			session.worker.progress.setProgress(session.msgs.getString("MainFrame.ImportingFromMame"), -1); //$NON-NLS-1$
			try
			{
				JsonObject jsobj = jso.get("params").asObject();
				String filename = FindCmd.findMame();
				if (filename != null)
				{
					final boolean sl = jsobj.getBoolean("sl", false);
					final Import imprt = new Import(session, new File(filename), sl, session.worker.progress);
					if(imprt.file != null)
					{
						final File parent = new File(Optional.ofNullable(jsobj.get("parent")).filter(JsonValue::isString).map(JsonValue::asString).orElse(session.getUser().settings.getWorkPath().toString()));
						final File file = new File(parent, imprt.file.getName());
						FileUtils.copyFile(imprt.file, file);
						final ProfileNFO pnfo = ProfileNFO.load(session, file);
						pnfo.mame.set(imprt.org_file, sl);
						if (imprt.roms_file != null)
						{
							FileUtils.copyFileToDirectory(imprt.roms_file, parent);
							pnfo.mame.fileroms = new File(parent, imprt.roms_file.getName());
							if(sl)
							{
								if (imprt.sl_file != null)
								{
									FileUtils.copyFileToDirectory(imprt.sl_file, parent);
									pnfo.mame.filesl = new File(parent, imprt.sl_file.getName());
								}
								else
									new GlobalWS(ws).warn("Could not import softwares list");
							}
							pnfo.save(session);
							imported(pnfo.file);
						}
						else
						{
							new GlobalWS(ws).warn("Could not import roms list");
							file.delete();
						}
					}
					else
						new GlobalWS(ws).warn("Could not import anything from Mame");
				}
				else
					new GlobalWS(ws).warn("Mame not found in system's search path");
			}
			catch(BreakException ex)
			{
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(), e);
				new GlobalWS(ws).warn(e.getMessage());
			}
			finally
			{
				session.worker.progress.close();
				session.worker.progress = null;
				session.lastAction = new Date();
			}
		})).start();
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
				if (session.curr_profile != null)
				{
					session.curr_profile.nfo.save(session);
					session.report.setProfile(session.curr_profile);
					loaded(session.curr_profile);
					new CatVerWS(ws).loaded(session.curr_profile);
					new NPlayersWS(ws).loaded(session.curr_profile);
				}
			}
			catch(BreakException ex)
			{
			}
			finally
			{
				session.worker.progress.close();
				session.worker.progress = null;
				session.lastAction = new Date();
			}
		})).start();
	}
	
	void scan(JsonObject jso, final boolean automate)
	{
		(ws.session.worker = new Worker(() -> {
			WebSession session = ws.session;
			session.worker.progress = new ProgressWS(ws);
			try
			{
				session.curr_scan = new Scan(session.curr_profile, session.worker.progress);
			}
			catch (BreakException ex)
			{
			}
			session.worker.progress.close();
			session.worker.progress = null;
			session.lastAction = new Date();
			ScanAutomation automation = ScanAutomation.valueOf(session.curr_profile.settings.getProperty(SettingsEnum.automation_scan, ScanAutomation.SCAN.toString()));
			scanned(session.curr_scan, automation.hasReport());
			if(automate)
			{
				if(session.curr_scan!=null && session.curr_scan.actions.stream().mapToInt(Collection::size).sum() > 0 && automation.hasFix())
				{
					fix(jso);
				}
			}
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
				ScanAutomation automation = ScanAutomation.valueOf(session.curr_profile.settings.getProperty(SettingsEnum.automation_scan, ScanAutomation.SCAN.toString()));
				if(automation.hasScanAgain())
					scan(jso, false);
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
			Log.err(e.getMessage(),e);
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
			Log.err(e.getMessage(),e);
		}
	}

	
	@SuppressWarnings("serial")
	void scanned(final Scan scan, final boolean hasReport)
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
						{
							add("actions", scan.actions.stream().mapToInt(Collection::size).sum());
							add("report", hasReport);
						}
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
			Log.err(e.getMessage(),e);
		}
	}

	@SuppressWarnings("serial")
	void imported(final File file)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Profile.imported");
					add("params", new JsonObject() {{
						add("path", file.getPath());
						add("parent", file.getParent());
						add("name", file.getName());
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}
}
