/**
 * 
 */
package jrm.batch;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.FilenameUtils;

import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.scan.DirScan;
import jrm.profile.scan.Scan;
import jrm.security.PathAbstractor;
import jrm.security.Session;
import jrm.ui.basic.ResultColUpdater;
import jrm.ui.basic.SrcDstResult;
import jrm.ui.progress.ProgressHandler;
import one.util.streamex.StreamEx;

/**
 * @author optyfr
 *
 */
public class DirUpdater
{

	/**
	 * Update dir list from dat list
	 * @param sdrl the data obtained from SDRTableModel
	 * @param progress the progression handler
	 * @param srcdirs the list of source directory containing new roms
	 * @param result the result interfaceo
	 * @param dryrun tell not to fix
	 */
	public DirUpdater(Session session, List<SrcDstResult> sdrl, final ProgressHandler progress, List<File> srcdirs, ResultColUpdater result, boolean dryrun)
	{
		final Map<String, DirScan> scancache = new HashMap<>();
		StreamEx.of(sdrl).filter(sdr->sdr.selected).forEach(sdr->{
			result.updateResult(sdrl.indexOf(sdr), ""); //$NON-NLS-1$
		});
		StreamEx.of(sdrl).filter(sdr->sdr.selected).takeWhile(p->!progress.isCancel()).forEach(sdr->{
			int row = sdrl.indexOf(sdr);
			result.updateResult(row, "In progress..."); //$NON-NLS-1$
			final File dat = PathAbstractor.getAbsolutePath(session, sdr.src).toFile();
			final File dst = PathAbstractor.getAbsolutePath(session, sdr.dst).toFile();
			DirUpdaterResults dur = new DirUpdaterResults();
			dur.dat = dat;
			try
			{
				File[] datlist = { dat };
				File[] dstlist = { dst };
				if (dat.isDirectory())
				{
					datlist = dat.listFiles((sdir, sfilename) -> Sets.newHashSet("xml", "dat").contains(FilenameUtils.getExtension(sfilename).toLowerCase())); //$NON-NLS-1$ //$NON-NLS-2$
					Arrays.sort(datlist, (a, b) -> a.getAbsolutePath().compareTo(b.getAbsolutePath()));
					for (File d : datlist)
						Files.copy(session.getUser().getSettings().getProfileSettingsFile(dat).toPath(), session.getUser().getSettings().getProfileSettingsFile(d).toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
					dstlist = StreamEx.of(datlist).map(datfile -> new File(dst, FilenameUtils.removeExtension(datfile.getName()))).toArray(File.class);
					for (File d : dstlist)
						d.mkdir();
				}
				long total = 0, ok = 0;
				for (int j = 0; j < datlist.length; j++)
				{
					session.report.setProfile(Profile.load(session, datlist[j], progress));
					if(session.curr_profile.softwares_list_cnt>0 && dat.isDirectory())
						session.curr_profile.setProperty(SettingsEnum.roms_dest_dir, dstlist[j].getParentFile().getAbsolutePath()); //$NON-NLS-1$
					else
						session.curr_profile.setProperty(SettingsEnum.roms_dest_dir, dstlist[j].getAbsolutePath()); //$NON-NLS-1$
					session.curr_profile.setProperty(SettingsEnum.src_dir, String.join("|", srcdirs.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList()))); //$NON-NLS-1$ //$NON-NLS-2$
					if (!dryrun)
						new Fix(session.curr_profile, new Scan(session.curr_profile, progress, scancache), progress);
					new Scan(session.curr_profile, progress, scancache);
					total += session.report.stats.set_create + session.report.stats.set_found + session.report.stats.set_missing;
					ok += session.report.stats.set_create_complete + session.report.stats.set_found_fixcomplete + session.report.stats.set_found_ok;
					dur.add(datlist[j],session.report.stats.clone());
					session.report.save(session);
					result.updateResult(row, String.format(session.msgs.getString("DirUpdater.Result"), ok * 100.0 / total, total - ok, total)); //$NON-NLS-1$
				}
				dur.save(session);
			}
			catch (BreakException e)
			{
				throw e;
			}
			catch (Throwable e)
			{
				Log.err(e.getMessage(),e);
			}
		});
	}
}
