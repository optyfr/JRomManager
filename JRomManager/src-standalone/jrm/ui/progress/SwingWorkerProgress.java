package jrm.ui.progress;

import java.awt.Window;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingWorker;

import jrm.aui.progress.ProgressHandler;
import jrm.aui.progress.ProgressInputStream;

import lombok.RequiredArgsConstructor;

public abstract class SwingWorkerProgress<T,V> extends SwingWorker<T,V> implements ProgressHandler
{
	private final Progress progress;
	
	/** The thread id offset. */
	private Map<Long,Integer> threadId_Offset = new HashMap<>();
	private int threadCnt;
	
	public SwingWorkerProgress(final Window owner)
	{
		super();
		progress = new Progress(owner);
		addPropertyChangeListener(e -> {
			switch(e.getPropertyName())
			{
				case "setProgress":
					if(e.getNewValue() instanceof SetProgress)
					{
						SetProgress props = (SetProgress)e.getNewValue();
						progress.setProgress(props.offset, props.msg, props.val, props.max, props.submsg);
					}
					break;
				case "setProgress2":
					if(e.getNewValue() instanceof SetProgress2)
					{
						SetProgress2 props = (SetProgress2)e.getNewValue();
						progress.setProgress2(props.msg, props.val, props.max);
					}
					break;
				case "setInfos":
					if(e.getNewValue() instanceof SetInfos)
					{
						SetInfos props = (SetInfos)e.getNewValue();
						progress.setInfos(props.threadCnt, props.multipleSubInfos);
					}
					break;
				case "clearInfos":
					progress.clearInfos();
					break;
				case "canCancel":
					progress.canCancel((Boolean)e.getNewValue());
					break;
				case "cancel":
					progress.cancel();
					break;
				case "close":
					progress.close();
					break;
			}
		});
		progress.setVisible(true);
	}

	@RequiredArgsConstructor
	private static class SetInfos
	{
		private final int threadCnt;
		private final boolean multipleSubInfos;
	}
	
	@Override
	public void setInfos(int threadCnt, boolean multipleSubInfos)
	{
		synchronized (threadId_Offset)
		{
			this.threadCnt = threadCnt <= 0 ? Runtime.getRuntime().availableProcessors() : threadCnt;
			threadId_Offset.clear();
		}
		firePropertyChange("setInfos", null, new SetInfos(this.threadCnt, multipleSubInfos));
	}

	@Override
	public void clearInfos()
	{
		firePropertyChange("clearInfos", false, true);
	}

	@RequiredArgsConstructor
	private static class SetProgress
	{
		private final int offset;
		private final String msg;
		private final Integer val;
		private final Integer max;
		private final String submsg;
	}
	
	@Override
	public void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		final int offset;
		synchronized (threadId_Offset)
		{
			if (!threadId_Offset.containsKey(Thread.currentThread().getId()))
			{
				if (threadId_Offset.size() < threadCnt)
					threadId_Offset.put(Thread.currentThread().getId(), threadId_Offset.size());
				else
				{
					ThreadGroup tg = Thread.currentThread().getThreadGroup();
					Thread[] tl = new Thread[tg.activeCount()];
					int tl_count = tg.enumerate(tl, false);
					boolean found = false;
					for (Map.Entry<Long, Integer> e : threadId_Offset.entrySet())
					{
						boolean exists = false;
						for (int i = 0; i < tl_count; i++)
						{
							if (e.getKey() == tl[i].getId())
							{
								exists = true;
								break;
							}
						}
						if (!exists)
						{
							threadId_Offset.remove(e.getKey());
							threadId_Offset.put(Thread.currentThread().getId(), e.getValue());
							found = true;
							break;
						}
					}
					if (!found)
						threadId_Offset.put(Thread.currentThread().getId(), 0);
				}
			}
			offset = threadId_Offset.get(Thread.currentThread().getId());
		}
		firePropertyChange("setProgress", null, new SetProgress(offset, msg, val, max, submsg));
	}

	@RequiredArgsConstructor
	private static class SetProgress2
	{
		private final String msg;
		private final Integer val;
		private final Integer max;
	}
	
	@Override
	public void setProgress2(String msg, Integer val, Integer max)
	{
		firePropertyChange("setProgress2", null, new SetProgress2(msg, val, max));
	}

	@Override
	public int getValue()
	{
		return progress.getValue();
	}

	@Override
	public int getValue2()
	{
		return progress.getValue2();
	}

	@Override
	public boolean isCancel()
	{
		return progress.isCancel();
	}

	@Override
	public void cancel()
	{
		firePropertyChange("cancel", false, true);
	}

	@Override
	public void canCancel(boolean canCancel)
	{
		firePropertyChange("canCancel", canCancel(), canCancel);
	}

	@Override
	public boolean canCancel()
	{
		return progress.canCancel();
	}

	@Override
	public InputStream getInputStream(InputStream in, Integer len)
	{
		return new ProgressInputStream(in, len, this);
	}

	@Override
	public void close()
	{
		firePropertyChange("close", false, true);
	}
}
