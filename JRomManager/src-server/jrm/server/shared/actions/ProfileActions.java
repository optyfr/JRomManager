package jrm.server.shared.actions;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import jrm.misc.ProfileSettings;
import jrm.misc.SettingsEnum;
import jrm.profile.fix.Fix;
import jrm.profile.manager.Import;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.scan.Scan;
import jrm.profile.scan.options.ScanAutomation;
import jrm.security.PathAbstractor;
import jrm.server.shared.WebSession;
import jrm.server.shared.Worker;
import lombok.val;

public class ProfileActions extends PathAbstractor
{
	private final ActionsMgr ws;

	public ProfileActions(ActionsMgr ws)
	{
		super(ws.getSession());
		this.ws = ws;
	}

	public void imprt(JsonObject jso)
	{
		(ws.getSession().setWorker(new Worker(() -> {
			WebSession session = ws.getSession();
			session.getWorker().progress = new ProgressActions(ws);
			session.getWorker().progress.canCancel(false);
			session.getWorker().progress.setProgress(session.getMsgs().getString("MainFrame.ImportingFromMame"), -1); //$NON-NLS-1$
			try
			{
				JsonObject jsobj = jso.get("params").asObject();
				String filename = FindCmd.findMame();
				if (filename != null)
				{
					final var sl = jsobj.getBoolean("sl", false);
					final var imprt = new Import(session, new File(filename), sl, session.getWorker().progress);
					if (imprt.file != null)
						doImport(session, jsobj, sl, imprt);
					else
						new GlobalActions(ws).warn("Could not import anything from Mame");
				}
				else
					new GlobalActions(ws).warn("Mame not found in system's search path");
			}
			catch (BreakException ex)
			{
				// user cancelled action
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(), e);
				new GlobalActions(ws).warn(e.getMessage());
			}
			finally
			{
				session.getWorker().progress.close();
				session.getWorker().progress = null;
				session.setLastAction(new Date());
			}
		}))).start();
	}

	/**
	 * @param session
	 * @param jsobj
	 * @param sl
	 * @param imprt
	 * @throws SecurityException
	 * @throws IOException
	 */
	private void doImport(WebSession session, JsonObject jsobj, final boolean sl, final Import imprt) throws SecurityException, IOException
	{
		final var parent = getAbsolutePath(Optional.ofNullable(jsobj.get("parent")).filter(JsonValue::isString).map(JsonValue::asString).orElse(session.getUser().getSettings().getWorkPath().toString())).toFile();
		final var file = new File(parent, imprt.file.getName());
		FileUtils.copyFile(imprt.file, file);
		final var pnfo = ProfileNFO.load(session, file);
		pnfo.mame.set(imprt.org_file, sl);
		if (imprt.roms_file != null)
		{
			FileUtils.copyFileToDirectory(imprt.roms_file, parent);
			pnfo.mame.setFileroms(new File(parent, imprt.roms_file.getName()));
			if (sl)
			{
				if (imprt.sl_file != null)
				{
					FileUtils.copyFileToDirectory(imprt.sl_file, parent);
					pnfo.mame.setFilesl(new File(parent, imprt.sl_file.getName()));
				}
				else
					new GlobalActions(ws).warn("Could not import softwares list");
			}
			pnfo.save(session);
			imported(pnfo.file);
		}
		else
		{
			new GlobalActions(ws).warn("Could not import roms list");
			Files.delete(file.toPath());
		}
	}

	public void load(JsonObject jso)
	{
		(ws.getSession().setWorker(new Worker(() -> {
			WebSession session = ws.getSession();
			if (session.getCurrProfile() != null)
				session.getCurrProfile().saveSettings();
			session.getWorker().progress = new ProgressActions(ws);
			try
			{
				JsonObject jsobj = jso.get("params").asObject();
				val file = getAbsolutePath(jsobj.getString("parent", null)).resolve(jsobj.getString("file", null));
				session.setCurrProfile(jrm.profile.Profile.load(session, file.toFile(), session.getWorker().progress));
				if (session.getCurrProfile() != null)
				{
					session.getCurrProfile().getNfo().save(session);
					session.getReport().setProfile(session.getCurrProfile());
					loaded(session.getCurrProfile());
					new CatVerActions(ws).loaded(session.getCurrProfile());
					new NPlayersActions(ws).loaded(session.getCurrProfile());
				}
			}
			catch (BreakException ex)
			{
				// user cancelled action
			}
			finally
			{
				session.getWorker().progress.close();
				session.getWorker().progress = null;
				session.setLastAction(new Date());
			}
		}))).start();
	}

	public void importSettings(JsonObject jso)
	{
		WebSession session = ws.getSession();
		if (session.getCurrProfile() != null)
		{
			final JsonValue jsv = jso.get("params").asObject().get("path");
			if (jsv != null && !jsv.isNull())
			{
				session.getCurrProfile().loadSettings(PathAbstractor.getAbsolutePath(session, jsv.asString()).toFile());
				session.getCurrProfile().loadCatVer(null);
				session.getCurrProfile().loadNPlayers(null);
				loaded(session.getCurrProfile());
				new CatVerActions(ws).loaded(session.getCurrProfile());
				new NPlayersActions(ws).loaded(session.getCurrProfile());
			}
		}
	}

	public void exportSettings(JsonObject jso)
	{
		WebSession session = ws.getSession();
		if (session.getCurrProfile() != null)
		{
			final JsonValue jsv = jso.get("params").asObject().get("path");
			if (jsv != null && !jsv.isNull())
			{
				session.getCurrProfile().saveSettings(PathAbstractor.getAbsolutePath(session, jsv.asString()).toFile());
			}
		}
	}

	public void scan(JsonObject jso, final boolean automate)
	{
		(ws.getSession().setWorker(new Worker(() -> {
			WebSession session = ws.getSession();
			session.getWorker().progress = new ProgressActions(ws);
			try
			{
				session.setCurrScan(new Scan(session.getCurrProfile(), session.getWorker().progress));
			}
			catch (BreakException ex)
			{
				// user cancelled action
			}
			session.getWorker().progress.close();
			session.getWorker().progress = null;
			session.setLastAction(new Date());
			final var automation = ScanAutomation.valueOf(session.getCurrProfile().getSettings().getProperty(SettingsEnum.automation_scan, ScanAutomation.SCAN.toString()));
			scanned(session.getCurrScan(), automation.hasReport());
			if (automate && session.getCurrScan() != null && session.getCurrScan().actions.stream().mapToInt(Collection::size).sum() > 0 && automation.hasFix())
				fix(jso);
		}))).start();
	}

	public void fix(JsonObject jso)
	{
		(ws.getSession().setWorker(new Worker(() -> {
			final var session = ws.getSession();
			session.getWorker().progress = new ProgressActions(ws);
			try
			{
				if (session.getCurrProfile().hasPropsChanged())
				{
					session.setCurrScan(new Scan(session.getCurrProfile(), session.getWorker().progress));
					boolean needfix = session.getCurrScan().actions.stream().mapToInt(Collection::size).sum() > 0;
					if (!needfix)
						return;
				}
				final var fix = new Fix(session.getCurrProfile(), session.getCurrScan(), session.getWorker().progress);
				fixed(fix);
			}
			finally
			{
				final var automation = ScanAutomation.valueOf(session.getCurrProfile().getSettings().getProperty(SettingsEnum.automation_scan, ScanAutomation.SCAN.toString()));
				if (automation.hasScanAgain())
					scan(jso, false);
				session.getWorker().progress.close();
				session.getWorker().progress = null;
				session.setLastAction(new Date());
			}
		}))).start();
	}

	public void setProperty(JsonObject jso)
	{
		final var profile = jso.getString("profile", null);
		ProfileSettings settings = profile != null ? new ProfileSettings() : ws.getSession().getCurrProfile().getSettings();
		JsonObject pjso = jso.get("params").asObject();
		for (Member m : pjso)
		{
			JsonValue value = m.getValue();
			if (value.isBoolean())
				settings.setProperty(m.getName(), value.asBoolean());
			else if (value.isNumber())
				settings.setProperty(m.getName(), value.asInt());
			else if (value.isString())
				settings.setProperty(m.getName(), value.asString());
			else
				settings.setProperty(m.getName(), value.toString());
		}
		try
		{
			if (profile != null)
				ws.getSession().getUser().getSettings().saveProfileSettings(getAbsolutePath(profile).toFile(), settings);
			else
				ws.getSession().getCurrProfile().saveSettings();
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	public void loaded(final jrm.profile.Profile profile)
	{
		try
		{
			if (ws.isOpen())
			{
				final var rjso = new JsonObject();
				rjso.add("cmd", "Profile.loaded");
				final var params = new JsonObject();
				params.add("success", profile != null);
				if (profile != null)
				{
					params.add("name", profile.getName());
					if (profile.getSystems() != null)
					{
						final var systems = new JsonArray();
						profile.getSystems().forEach(s -> {
							final var systm = new JsonObject();
							systm.add("name", s.toString());
							systm.add("selected", s.isSelected(profile));
							systm.add("property", s.getPropertyName());
							systm.add("type", s.getType().toString());
							systems.add(systm);
						});
						params.add("systems", systems);
					}
					final var years = new JsonArray();
					final ArrayList<String> arrlst = new ArrayList<>(profile.getYears());
					arrlst.sort(String::compareTo);
					arrlst.forEach(years::add);
					params.add("years", years);
					if (profile.getSettings() != null)
						params.add("settings", profile.getSettings().asJSO());
				}
				rjso.add("params", params);
				ws.send(rjso.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	void scanned(final Scan scan, final boolean hasReport)
	{
		try
		{
			if (ws.isOpen())
			{
				final var rjso = new JsonObject();
				rjso.add("cmd", "Profile.scanned");
				final var params = new JsonObject();
				params.add("success", scan != null);
				if (scan != null)
				{
					params.add("actions", scan.actions.stream().mapToInt(Collection::size).sum());
					params.add("report", hasReport);
				}
				rjso.add("params", params);
				ws.send(rjso.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	void fixed(final Fix fix)
	{
		try
		{
			if (ws.isOpen())
			{
				final var rjso = new JsonObject();
				rjso.add("cmd", "Profile.fixed");
				final var params = new JsonObject();
				params.add("success", fix != null);
				if (fix != null)
					params.add("actions", fix.getActionsRemain());
				rjso.add("params", params);
				ws.send(rjso.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	void imported(final File file)
	{
		try
		{
			if (ws.isOpen())
			{
				final var rjso = new JsonObject();
				rjso.add("cmd", "Profile.imported");
				final var params = new JsonObject();
				params.add("path", file.getPath());
				params.add("parent", file.getParent());
				params.add("name", file.getName());
				rjso.add("params", params);
				ws.send(rjso.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}
}
