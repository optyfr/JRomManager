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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.commons.compress.utils.Sets;
import org.apache.commons.io.FilenameUtils;

import jrm.aui.basic.AbstractSrcDstResult;
import jrm.aui.basic.ResultColUpdater;
import jrm.aui.progress.ProgressHandler;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.Profile;
import jrm.profile.fix.Fix;
import jrm.profile.report.Report;
import jrm.profile.scan.DirScan;
import jrm.profile.scan.Scan;
import jrm.profile.scan.ScanException;
import jrm.security.PathAbstractor;
import jrm.security.Session;

/**
 * @author optyfr
 *
 */
public class DirUpdater
{
	private final Session session;
	private final List<? extends AbstractSrcDstResult> sdrl;
	private final ProgressHandler progress;
	private final List<File> srcdirs;
	private final ResultColUpdater result;
	private final boolean dryrun;	
	
	/**
	 * Update dir list from dat list
	 * @param sdrl the data obtained from SDRTableModel
	 * @param progress the progression handler
	 * @param srcdirs the list of source directory containing new roms
	 * @param result the result interfaceo
	 * @param dryrun tell not to fix
	 */
	public DirUpdater(final Session session, final List<? extends AbstractSrcDstResult> sdrl, final ProgressHandler progress, final List<File> srcdirs, final ResultColUpdater result, final boolean dryrun)
	{
		this.session = session;
		this.sdrl = sdrl;
		this.progress = progress;
		this.srcdirs = srcdirs;
		this.result = result;
		this.dryrun = dryrun;
		
		final Map<String, DirScan> scancache = new HashMap<>();
		sdrl.stream().filter(AbstractSrcDstResult::isSelected).forEach(sdr->
			result.updateResult(sdrl.indexOf(sdr), "") //$NON-NLS-1$
		);
		sdrl.stream().filter(AbstractSrcDstResult::isSelected).takeWhile(p->!progress.isCancel()).forEach(sdr-> update(scancache, sdr));
	}

	/**
	 * @param session
	 * @param sdrl
	 * @param progress
	 * @param srcdirs
	 * @param result
	 * @param dryrun
	 * @param scancache
	 * @param sdr
	 * @throws SecurityException
	 * @throws BreakException
	 */
	private void update(final Map<String, DirScan> scancache, AbstractSrcDstResult sdr) throws SecurityException, BreakException
	{
		final var row = sdrl.indexOf(sdr);
		result.updateResult(row, "In progress..."); //$NON-NLS-1$
		final var dat = PathAbstractor.getAbsolutePath(session, sdr.getSrc()).toFile();
		final var dst = PathAbstractor.getAbsolutePath(session, sdr.getDst()).toFile();
		final var dur = new DirUpdaterResults();
		dur.setDat(dat);
		try
		{
			var datlist = new File[] { dat };
			var dstlist = new File[] { dst };
			if (dat.isDirectory())
			{
				datlist = dat.listFiles((sdir, sfilename) -> Sets.newHashSet("xml", "dat").contains(FilenameUtils.getExtension(sfilename).toLowerCase())); //$NON-NLS-1$ //$NON-NLS-2$
				Arrays.sort(datlist, (a, b) -> a.getAbsolutePath().compareTo(b.getAbsolutePath()));
				for (File d : datlist)
					Files.copy(session.getUser().getSettings().getProfileSettingsFile(dat).toPath(), session.getUser().getSettings().getProfileSettingsFile(d).toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
				dstlist = Stream.of(datlist).map(datfile -> new File(dst, FilenameUtils.removeExtension(datfile.getName()))).toArray(File[]::new);
				for (File d : dstlist)
					d.mkdir();
			}
			update(scancache, row, dat, dur, datlist, dstlist);
			progress.setProgress3(null, null);
			dur.save(session);
		}
		catch (final BreakException e)
		{
			throw e;
		}
		catch (final Exception e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	/**
	 * @param scancache
	 * @param row
	 * @param dat
	 * @param dur
	 * @param datlist
	 * @param dstlist
	 * @throws BreakException
	 * @throws ScanException
	 */
	private void update(final Map<String, DirScan> scancache, final int row, final File dat, final DirUpdaterResults dur, File[] datlist, File[] dstlist) throws BreakException, ScanException
	{
		final var total = new AtomicLong();
		final var ok = new AtomicLong();
		for (var j = 0; j < datlist.length; j++)
		{
			if(dat.isDirectory())
				progress.setProgress3(String.format("%s/%s (%d/%d)", dat.getName(), FilenameUtils.getBaseName(datlist[j].getName()), j , datlist.length), j, datlist.length);
			else
				progress.setProgress3(String.format("%s (%d/%d)", FilenameUtils.getBaseName(datlist[j].getName()), j , datlist.length), j, datlist.length);
			session.getReport().setProfile(Profile.load(session, datlist[j], progress));
			if(session.getCurrProfile().getSoftwaresListCnt()>0 && dat.isDirectory())
				session.getCurrProfile().setProperty(ProfileSettingsEnum.roms_dest_dir, dstlist[j].getParentFile().getAbsolutePath()); //$NON-NLS-1$
			else
				session.getCurrProfile().setProperty(ProfileSettingsEnum.roms_dest_dir, dstlist[j].getAbsolutePath()); //$NON-NLS-1$
			session.getCurrProfile().setProperty(ProfileSettingsEnum.src_dir, String.join("|", srcdirs.stream().map(File::getAbsolutePath).toList())); //$NON-NLS-1$ //$NON-NLS-2$
			final var scan = new Scan(session.getCurrProfile(), progress, scancache);
			if (!dryrun && !scan.actions.isEmpty())
			{
				new Fix(session.getCurrProfile(), scan, progress);
				new Scan(session.getCurrProfile(), progress, scancache);
			}
			final var lTotal = total.addAndGet((long) session.getReport().getStats().getSetCreate() + session.getReport().getStats().getSetFound() + session.getReport().getStats().getSetMissing());
			final var lOk = ok.addAndGet((long) session.getReport().getStats().getSetCreateComplete() + session.getReport().getStats().getSetFoundFixComplete() + session.getReport().getStats().getSetFoundOk());
			dur.add(datlist[j],new Report.Stats(session.getReport().getStats()));
			session.getReport().save(session);
			result.updateResult(row, String.format(session.getMsgs().getString("DirUpdater.Result"), lOk * 100.0 / lTotal, lTotal - lOk, lTotal)); //$NON-NLS-1$
		}
	}
}
