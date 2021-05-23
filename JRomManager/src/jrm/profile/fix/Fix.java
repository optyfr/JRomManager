/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.fix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.time.DurationFormatUtils;

import jrm.aui.progress.ProgressHandler;
import jrm.misc.BreakException;
import jrm.misc.Log;
import jrm.misc.MultiThreading;
import jrm.misc.SettingsEnum;
import jrm.profile.Profile;
import jrm.profile.fix.actions.BackupContainer;
import jrm.profile.fix.actions.ContainerAction;
import jrm.profile.scan.Scan;
import lombok.val;

/**
 * The class that fix apply all the fixes on your set
 */
public class Fix
{
	/**
	 * Retain the scan result from which this class will apply fixes from defined actions
	 */
	private final Scan curr_scan;

	/**
	 * The Fix constructor
	 * @param curr_profile the current {@link Profile} from which we will get some options and return stats 
	 * @param curr_scan the current {@link Scan} containing actions to do
	 * @param progress the {@link ProgressHandler} in which we will show fixing progression
	 */
	public Fix(final Profile curr_profile, final Scan curr_scan, final ProgressHandler progress)
	{
		this.curr_scan = curr_scan;

		val use_parallelism = curr_profile.getProperty(SettingsEnum.use_parallelism, curr_profile.getSession().server); // $NON-NLS-1$
		val nThreads = use_parallelism ? curr_profile.getSession().getUser().getSettings().getProperty(SettingsEnum.thread_count, -1) : 1;

		final long start = System.currentTimeMillis();
		
		/*
		 * Initialize global progression
		 */
		final AtomicInteger i = new AtomicInteger(0), max = new AtomicInteger(0);
		curr_scan.actions.forEach(actions -> {
			max.addAndGet(actions.size());
			actions.forEach(action->max.addAndGet(action.count() + (int)(action.estimatedSize()>>20)));
		});
		progress.setProgress(curr_profile.getSession().msgs.getString("Fix.Fixing"), i.get(), max.get()); //$NON-NLS-1$
		
		// foreach ordered action groups
		curr_scan.actions.forEach(actions -> {
			if(actions.size() > 0)
			{
				final List<ContainerAction> done = Collections.synchronizedList(new ArrayList<ContainerAction>());
				// resets progression parallelism (needed since thread IDs may change between to parallel streaming)
				progress.setInfos(nThreads, use_parallelism);
				new MultiThreading<ContainerAction>(nThreads, action -> {
					if (progress.isCancel())
						return;
					try
					{
						if (!action.doAction(curr_profile.getSession(), progress)) // do action...
							progress.cancel(); // ... and cancel all if it failed
						else
							done.add(action); // add to "done" list successful action
						progress.setProgress("", i.addAndGet(1 + action.count() + (int) (action.estimatedSize() >> 20))); // update progression
					}
					catch (final BreakException be)
					{	// special catch case from BreakException thrown from underlying streams
						progress.cancel();
					}
					catch (final Throwable e)
					{	// oups! something unexpected happened
						progress.setProgress("");
						Log.err(e.getMessage(), e);
					}
					return;
				}).start(actions.stream().sorted(ContainerAction.rcomparator()));
				// close all open FS from backup (if the last actions was backup)
				if (done.size() > 0 && done.get(0) instanceof BackupContainer)
					BackupContainer.closeAllFS();
				// remove all done actions
				actions.removeAll(done);
				// this actions group is finished, clear progression status
				progress.clearInfos();
			}
		});		
		
		// reset progression to normal before leaving
		progress.setInfos(1,false);
		// set stats last fixed date to 'now'
		curr_profile.getNfo().stats.fixed = new Date();
		
		// output to console timing information
		Log.info(()->"Fix total duration : " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start)); //$NON-NLS-1$
	}

	
	/**
	 * get remaining actions if any
	 * @return the number of actions remaining, or 0 if all successfully done without canceling
	 */
	public int getActionsRemain()
	{
		final AtomicInteger actions_remain = new AtomicInteger(0);
		curr_scan.actions.forEach(actions -> actions_remain.addAndGet(actions.size()));
		return actions_remain.get();
	}

}
