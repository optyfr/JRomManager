package jrm.profiler.fix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.DurationFormatUtils;

import jrm.Messages;
import jrm.misc.BreakException;
import jrm.profiler.Profile;
import jrm.profiler.fix.actions.ContainerAction;
import jrm.profiler.scan.Scan;
import jrm.ui.ProgressHandler;
import one.util.streamex.StreamEx;

public class Fix
{
	private Scan curr_scan;

	public Fix(Profile curr_profile, Scan curr_scan, ProgressHandler progress)
	{
		this.curr_scan = curr_scan;

		boolean use_parallelism = curr_profile.getProperty("use_parallelism", false); //$NON-NLS-1$
		AtomicInteger i = new AtomicInteger(0), max = new AtomicInteger(0);
		curr_scan.actions.forEach(actions -> max.addAndGet(actions.size()));
		progress.setProgress(Messages.getString("Fix.Fixing"), i.get(), max.get()); //$NON-NLS-1$
		long start = System.currentTimeMillis();
		curr_scan.actions.forEach(actions -> {
			List<ContainerAction> done = Collections.synchronizedList(new ArrayList<ContainerAction>());
			StreamEx.of(use_parallelism ? actions.parallelStream().unordered() : actions.stream()).takeWhile((action) -> !progress.isCancel()).forEach(action -> {
				try
				{
					// System.out.println(action);
					if(!action.doAction(progress))
						progress.cancel();
					done.add(action);
					progress.setProgress(null, i.incrementAndGet());
				}
				catch(BreakException be)
				{
					progress.cancel();
				}
			});
			actions.removeAll(done);
		});
		System.out.println("Fix total duration : " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start)); //$NON-NLS-1$
	}

	public int getActionsRemain()
	{
		AtomicInteger actions_remain = new AtomicInteger(0);
		curr_scan.actions.forEach(actions -> actions_remain.addAndGet(actions.size()));
		return actions_remain.get();
	}

}
