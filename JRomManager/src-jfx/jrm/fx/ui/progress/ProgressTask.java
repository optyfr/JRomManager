package jrm.fx.ui.progress;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import jrm.aui.progress.ProgressHandler;
import jrm.aui.progress.ProgressInputStream;

public abstract class ProgressTask<V> extends Task<V> implements ProgressHandler
{
	private final Progress progress;

	private final List<String> errors = new ArrayList<>();
	
	/** The thread id offset. */
	private Map<Long, Integer> threadIdOffset = new HashMap<>();
	private int threadCnt;
	private Boolean multipleSubInfos = null;

	protected ProgressTask(Stage owner) throws IOException, URISyntaxException
	{
		super();
		progress = new Progress(owner);
	}

	@Override
	public void setInfos(int threadCnt, Boolean multipleSubInfos)
	{
		synchronized (threadIdOffset)
		{
			this.threadCnt = threadCnt <= 0 ? Runtime.getRuntime().availableProcessors() : threadCnt;
			threadIdOffset.clear();
		}
		this.multipleSubInfos = multipleSubInfos;
		Platform.runLater(() -> progress.getController().setInfos(this.threadCnt, this.multipleSubInfos));
	}

	@Override
	public void clearInfos()
	{
		Platform.runLater(() -> progress.getController().clearInfos());
	}

	private void cleanup()
	{
		synchronized (threadIdOffset)
		{
			final Thread ct = Thread.currentThread();
			if (!threadIdOffset.containsKey(ct.getId()))
				return;
			final ThreadGroup tg = ct.getThreadGroup(); // NOSONAR
			if (threadIdOffset.size() == tg.activeCount())
				return;
			final var tl = new Thread[tg.activeCount()];
			final var tl_count = tg.enumerate(tl, false);
			final var itr = threadIdOffset.entrySet().iterator();
			while (itr.hasNext())
			{
				final var e = itr.next();
				if (!isOffsetExist(e, tl, tl_count))
				{
					Platform.runLater(() -> progress.getController().setProgress(e.getValue(), "", null, null, this.multipleSubInfos != null && this.multipleSubInfos ? "" : null));
					itr.remove();
				}
			}
		}
	}

	/**
	 * @return
	 */
	private int getOffset()
	{
		final int offset;
		synchronized (threadIdOffset)
		{
			if (!threadIdOffset.containsKey(Thread.currentThread().getId()))
			{
				if (threadIdOffset.size() < threadCnt)
					threadIdOffset.put(Thread.currentThread().getId(), threadIdOffset.size());
				else
				{
					final var tg = Thread.currentThread().getThreadGroup(); // NOSONAR
					final var tl = new Thread[tg.activeCount()];
					final var tlCount = tg.enumerate(tl, false);
					if (!isOffsetFound(tl, tlCount))
						threadIdOffset.put(Thread.currentThread().getId(), 0);
				}
			}
			offset = threadIdOffset.get(Thread.currentThread().getId());
		}
		return offset;
	}

	/**
	 * @param tl
	 * @param tlCount
	 * @return
	 */
	private boolean isOffsetFound(final Thread[] tl, final int tlCount)
	{
		var found = false;
		for (Map.Entry<Long, Integer> e : threadIdOffset.entrySet())
		{
			if (!isOffsetExist(e, tl, tlCount))
			{
				threadIdOffset.remove(e.getKey());
				threadIdOffset.put(Thread.currentThread().getId(), e.getValue());
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * @param entry
	 * @param tl
	 * @param tlCount
	 * @return
	 */
	private boolean isOffsetExist(Map.Entry<Long, Integer> entry, Thread[] tl, int tlCount)
	{
		boolean exists = false;
		for (int i = 0; i < tlCount; i++)
		{
			if (entry.getKey() == tl[i].getId())
			{
				exists = true;
				break;
			}
		}
		return exists;
	}

	@Override
	public void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		final int offset = getOffset();
		cleanup();
		Platform.runLater(() -> progress.getController().setProgress(offset, msg, val, max, submsg));
	}

	@Override
	public void setProgress2(String msg, Integer val, Integer max)
	{
		Platform.runLater(() -> progress.getController().setProgress2(msg, val, max));
	}

	@Override
	public void setProgress3(String msg, Integer val, Integer max)
	{
		Platform.runLater(() -> progress.getController().setProgress3(msg, val, max));
	}

	@Override
	public int getCurrent()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrent2()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrent3()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isCancel()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doCancel()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void canCancel(boolean canCancel)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canCancel()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public InputStream getInputStream(InputStream in, Integer len)
	{
		return new ProgressInputStream(in, len, this);
	}

	@Override
	public void close()
	{
		progress.close();
	}

	@Override
	public void addError(String error)
	{
		errors.add(error);
	}
}
