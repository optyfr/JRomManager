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

import jrm.locale.Messages;
import jrm.misc.BreakException;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.scan.DirScan;
import jrm.profile.scan.Scan;
import jrm.ui.basic.ResultColUpdater;
import jrm.ui.basic.SDRTableModel;
import jrm.ui.basic.SDRTableModel.SrcDstResult;
import jrm.ui.progress.Progress;
import one.util.streamex.StreamEx;

/**
 * @author optyfr
 *
 */
public class DirUpdater
{

	/**
	 * Update dir list from dat list
	 * @param sdrl the data obtained from {@link SDRTableModel}
	 * @param progress the progression handler
	 * @param srcdirs the list of source directory containing new roms
	 * @param result the result interface
	 */
	public DirUpdater(List<SrcDstResult> sdrl, final Progress progress, List<File> srcdirs, ResultColUpdater result)
	{
		final Map<String, DirScan> scancache = new HashMap<>();
		for (int i = 0; i < sdrl.size(); i++)
		{
			final File dat = sdrl.get(i).src;
			final File dst = sdrl.get(i).dst;
			try
			{
				File[] datlist = { dat };
				File[] dstlist = { dst };
				if (dat.isDirectory())
				{
					datlist = dat.listFiles((sdir, sfilename) -> Sets.newHashSet("xml", "dat").contains(FilenameUtils.getExtension(sfilename).toLowerCase())); //$NON-NLS-1$ //$NON-NLS-2$
					Arrays.sort(datlist, (a, b) -> a.getAbsolutePath().compareTo(b.getAbsolutePath()));
					for (File d : datlist)
						Files.copy(Profile.getSettingsFile(dat).toPath(), Profile.getSettingsFile(d).toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
					dstlist = StreamEx.of(datlist).map(datfile -> new File(dst, FilenameUtils.removeExtension(datfile.getName()))).toArray(File.class);
					for (File d : dstlist)
						d.mkdir();
				}
				long total = 0, ok = 0;
				for (int j = 0; j < datlist.length; j++)
				{
					Scan.report.setProfile(Profile.curr_profile = Profile.load(datlist[j], progress));
					Profile.curr_profile.setProperty("roms_dest_dir", dstlist[j].getAbsolutePath()); //$NON-NLS-1$
					Profile.curr_profile.setProperty("src_dir", String.join("|", srcdirs.stream().map(f -> f.getAbsolutePath()).collect(Collectors.toList()))); //$NON-NLS-1$ //$NON-NLS-2$
					Scan scan = new Scan(Profile.curr_profile, progress, scancache);
					total += Scan.report.stats.set_create + Scan.report.stats.set_found + Scan.report.stats.set_missing;
					ok += Scan.report.stats.set_create_complete + Scan.report.stats.set_found_fixcomplete + Scan.report.stats.set_found_ok;
					new Fix(Profile.curr_profile, scan, progress);
					result.updateResult(i, String.format(Messages.getString("DirUpdater.Result"), (float) ok * 100.0 / (float) total, total-ok)); //$NON-NLS-1$
				}
			}
			catch (BreakException e)
			{
				throw e;
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}
}
