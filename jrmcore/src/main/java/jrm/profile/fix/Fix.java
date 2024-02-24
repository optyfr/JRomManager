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
import jrm.misc.MultiThreadingVirtual;
import jrm.misc.ProfileSettingsEnum;
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
	private final Scan currScan;

	/**
	 * The Fix constructor
	 * @param currProfile the current {@link Profile} from which we will get some options and return stats 
	 * @param currScan the current {@link Scan} containing actions to do
	 * @param progress the {@link ProgressHandler} in which we will show fixing progression
	 */
	public Fix(final Profile currProfile, final Scan currScan, final ProgressHandler progress)
	{
		this.currScan = currScan;

		val useParallelism = currProfile.getProperty(ProfileSettingsEnum.use_parallelism, Boolean.class); // $NON-NLS-1$
		val nThreads = useParallelism ? currProfile.getSession().getUser().getSettings().getProperty(SettingsEnum.thread_count, Integer.class) : 1;

		final long start = System.currentTimeMillis();
		
		/*
		 * Initialize global progression
		 */
		final var i = new AtomicInteger(0);
		final var max = new AtomicInteger(0);
		currScan.actions.forEach(actions -> {
			max.addAndGet(actions.size());
			actions.forEach(action->max.addAndGet(action.count() + (int)(action.estimatedSize()>>20)));
		});
		progress.setProgress(currProfile.getSession().getMsgs().getString("Fix.Fixing"), i.get(), max.get()); //$NON-NLS-1$
		
		// foreach ordered action groups
		currScan.actions.forEach(actions -> {
			if(!actions.isEmpty())
			{
				final List<ContainerAction> done = Collections.synchronizedList(new ArrayList<ContainerAction>());
				// resets progression parallelism (needed since thread IDs may change between two parallel streaming)
				progress.setInfos(nThreads, useParallelism);
				try (final var mt = new MultiThreadingVirtual<ContainerAction>("fix", progress, nThreads, action -> doAction(currProfile, progress, i, done, action)))
				{
					mt.start(actions.stream().sorted(ContainerAction.rcomparator()));
				}
				// close all open FS from backup (if the last actions was backup)
				if (!done.isEmpty() && done.get(0) instanceof BackupContainer)
					BackupContainer.closeAllFS();
				// remove all done actions
				actions.removeAll(done);
				// this actions group is finished, clear progression status
				progress.clearInfos();
				if (!actions.isEmpty())
					Log.warn(() -> "Missed " + actions.size() + " actions"); //$NON-NLS-1$
			}
		});		
		
		// reset progression to normal before leaving
		progress.setInfos(1,false);
		// set stats last fixed date to 'now'
		currProfile.getNfo().getStats().setFixed(new Date());
		
		// output to console timing information
		Log.info(() -> "Fix total duration for " + currProfile.getNfo().getName() + " : " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start)); //$NON-NLS-1$
	}


	/**
	 * @param currProfile
	 * @param progress
	 * @param i
	 * @param done
	 * @param action
	 */
	private void doAction(final Profile currProfile, final ProgressHandler progress, final AtomicInteger i, final List<ContainerAction> done, ContainerAction action)
	{
		if (progress.isCancel())
			return;
		try
		{
			if (!action.doAction(currProfile.getSession(), progress)) // do action...
			{
				Log.warn(()-> "Action " + action.toString() +" has failed, remaining actions processing will be cancelled");
				progress.doCancel(); // ... and cancel all if it failed
			}
			else
				done.add(action); // add to "done" list successful action
			progress.setProgress("", i.addAndGet(1 + action.count() + (int) (action.estimatedSize() >> 20))); // update progression
		}
		catch (final BreakException be)
		{	// special catch case from BreakException thrown from underlying streams
			progress.doCancel();
		}
		catch (final Exception e)
		{	// oups! something unexpected happened
			progress.setProgress("");
			Log.err(e.getMessage(), e);
		}
	}

	
	/**
	 * get remaining actions if any
	 * @return the number of actions remaining, or 0 if all successfully done without canceling
	 */
	public int getActionsRemain()
	{
		final var actionsRemain = new AtomicInteger(0);
		currScan.actions.forEach(actions -> actionsRemain.addAndGet(actions.size()));
		return actionsRemain.get();
	}

}
