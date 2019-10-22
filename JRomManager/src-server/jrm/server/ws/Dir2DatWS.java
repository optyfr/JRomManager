package jrm.server.ws;

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
import jrm.server.WebSession;

public class Dir2DatWS
{
	private final WebSckt ws;

	public Dir2DatWS(WebSckt ws)
	{
		this.ws = ws;
	}

	void start(JsonObject jso)
	{
		(ws.session.worker = new Worker(() -> {
			WebSession session = ws.session;
			session.worker.progress = new ProgressWS(ws);
			try
			{
				String srcdir = session.getUser().settings.getProperty(jrm.misc.Options.dir2dat_src_dir, null);
				String dstdat = session.getUser().settings.getProperty(jrm.misc.Options.dir2dat_dst_file, null);
				String format = session.getUser().settings.getProperty(jrm.misc.Options.dir2dat_format, "MAME");
				JsonObject opts = jso.get("params").asObject().get("options").asObject();
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
				HashMap<String, String> headers = new HashMap<>();
				JsonObject hdrs = jso.get("params").asObject().get("headers").asObject();
				hdrs.forEach(m -> {
					if (!m.getValue().isNull())
						headers.put(m.getName(), m.getValue().asString());
				});
				if (srcdir != null && dstdat != null)
					new Dir2Dat(ws.session, new File(srcdir), new File(dstdat), session.worker.progress, options, ExportType.valueOf(format), headers);
			}
			catch (BreakException e)
			{

			}
			finally
			{
				Dir2DatWS.this.end();
				session.curr_profile = null;
				session.curr_scan = null;
				session.worker.progress.close();
				session.worker.progress = null;
				session.lastAction = new Date();
			}
		})).start();
	}

	@SuppressWarnings("serial")
	void end()
	{
		try
		{
			if (ws.isOpen())
			{
				ws.send(new JsonObject()
				{
					{
						add("cmd", "Dir2Dat.end");
					}
				}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

}
