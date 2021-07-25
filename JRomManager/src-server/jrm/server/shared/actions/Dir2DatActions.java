package jrm.server.shared.actions;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;

import com.eclipsesource.json.JsonObject;

import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.profile.manager.Export.ExportType;
import jrm.profile.scan.Dir2Dat;
import jrm.profile.scan.DirScan;
import jrm.profile.scan.DirScan.Options;
import jrm.server.shared.WebSession;
import jrm.server.shared.Worker;

public class Dir2DatActions
{
	private final ActionsMgr ws;

	public Dir2DatActions(ActionsMgr ws)
	{
		this.ws = ws;
	}

	public void start(JsonObject jso)
	{
		(ws.getSession().setWorker(new Worker(() -> {
			WebSession session = ws.getSession();
			session.getWorker().progress = new ProgressActions(ws);
			try
			{
				String srcdir = session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_src_dir, null);
				String dstdat = session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_dst_file, null);
				String format = session.getUser().getSettings().getProperty(jrm.misc.SettingsEnum.dir2dat_format, "MAME");
				JsonObject opts = jso.get("params").asObject().get("options").asObject();
				EnumSet<DirScan.Options> options = getOptions(opts);
				HashMap<String, String> headers = new HashMap<>();
				JsonObject hdrs = jso.get("params").asObject().get("headers").asObject();
				hdrs.forEach(m -> {
					if (!m.getValue().isNull())
						headers.put(m.getName(), m.getValue().asString());
				});
				if (srcdir != null && dstdat != null)
					new Dir2Dat(ws.getSession(), new File(srcdir), new File(dstdat), session.getWorker().progress, options, ExportType.valueOf(format), headers);
			}
			catch (BreakException e)
			{
				// user cancelled action
			}
			finally
			{
				Dir2DatActions.this.end();
				session.setCurrProfile(null);
				session.setCurrScan(null);
				session.getWorker().progress.close();
				session.getWorker().progress = null;
				session.setLastAction(new Date());
			}
		}))).start();
	}

	/**
	 * @param opts
	 * @return
	 */
	private EnumSet<DirScan.Options> getOptions(JsonObject opts)
	{
		EnumSet<DirScan.Options> options = EnumSet.of(Options.USE_PARALLELISM, Options.MD5_DISKS, Options.SHA1_DISKS);
		if (opts.getBoolean("dir2dat.scan_subfolders", true)) //$NON-NLS-1$
			options.add(Options.RECURSE);
		if (!opts.getBoolean("dir2dat.deep_scan", false)) //$NON-NLS-1$
			options.add(Options.IS_DEST);
		if (opts.getBoolean("dir2dat.add_md5", false)) //$NON-NLS-1$
			options.add(Options.NEED_MD5);
		if (opts.getBoolean("dir2dat.add_sha1", false)) //$NON-NLS-1$
			options.add(Options.NEED_SHA1);
		if (opts.getBoolean("dir2dat.junk_folders", false)) //$NON-NLS-1$
			options.add(Options.JUNK_SUBFOLDERS);
		if (opts.getBoolean("dir2dat.do_not_scan_archives", false)) //$NON-NLS-1$
			options.add(Options.ARCHIVES_AND_CHD_AS_ROMS);
		if (opts.getBoolean("dir2dat.match_profile", false)) //$NON-NLS-1$
			options.add(Options.MATCH_PROFILE);
		if (opts.getBoolean("dir2dat.include_empty_dirs", false)) //$NON-NLS-1$
			options.add(Options.EMPTY_DIRS);
		return options;
	}

	void end()
	{
		try
		{
			if (ws.isOpen())
			{
				final var msg = new JsonObject();
				msg.add("cmd", "Dir2Dat.end");
				ws.send(msg.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

}
