package jrm.profile.fix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.DurationFormatUtils;

import jrm.Messages;
import jrm.misc.BreakException;
import jrm.profile.Profile;
import jrm.profile.fix.actions.BackupContainer;
import jrm.profile.fix.actions.ContainerAction;
import jrm.profile.scan.Scan;
import jrm.ui.ProgressHandler;
import one.util.streamex.StreamEx;

public class Fix
{
	private final Scan curr_scan;

	public Fix(final Profile curr_profile, final Scan curr_scan, final ProgressHandler progress)
	{
		this.curr_scan = curr_scan;


		final boolean use_parallelism = curr_profile.getProperty("use_parallelism", false); //$NON-NLS-1$
		final AtomicInteger i = new AtomicInteger(0), max = new AtomicInteger(0);
		curr_scan.actions.forEach(actions -> max.addAndGet(actions.size()));
		progress.setProgress(Messages.getString("Fix.Fixing"), i.get(), max.get()); //$NON-NLS-1$
		final long start = System.currentTimeMillis();
		curr_scan.actions.forEach(actions -> {
			final List<ContainerAction> done = Collections.synchronizedList(new ArrayList<ContainerAction>());
			progress.setInfos(use_parallelism ? Runtime.getRuntime().availableProcessors() : 1, use_parallelism);
			StreamEx.of(use_parallelism ? actions.parallelStream().unordered() : actions.stream()).takeWhile((action) -> !progress.isCancel()).forEach(action -> {
				try
				{
					if(!action.doAction(progress))
						progress.cancel();
					done.add(action);
					progress.setProgress(null, i.incrementAndGet());
				}
				catch(final BreakException be)
				{
					progress.cancel();
				}
				catch (final Throwable e)
				{
					e.printStackTrace();
				}
			});
			if (done.size() > 0 && done.get(0) instanceof BackupContainer)
				BackupContainer.closeAllFS();
			actions.removeAll(done);
			progress.clearInfos();
		});
		progress.setInfos(1,false);
		curr_profile.nfo.stats.fixed = new Date();
		System.out.println("Fix total duration : " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start)); //$NON-NLS-1$
	}

	public int getActionsRemain()
	{
		final AtomicInteger actions_remain = new AtomicInteger(0);
		curr_scan.actions.forEach(actions -> actions_remain.addAndGet(actions.size()));
		return actions_remain.get();
	}

}
