package jrm.server.shared.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.google.gson.Gson;

import jrm.aui.progress.ProgressHandler;
import jrm.aui.progress.ProgressInputStream;
import jrm.misc.Log;

public class ProgressActions implements ProgressHandler
{
	private ActionsMgr ws;
	private final List<String> errors = new ArrayList<>();

	/** The thread id offset. */
	private final Map<Long, Integer> threadIdOffset = new HashMap<>();

	/** The cancel. */
	private boolean cancel = false;

	private boolean canCancel = true;
	
	private Gson gson = new Gson();

	static final class SetFullProgress
	{
		static final class Data
		{
			static final class PB
			{
				boolean visibility = false;
				boolean stringPainted = false;
				boolean indeterminate = false;
				int max = 100;
				int val = 0;
				float perc = 0f;
				String msg = null;
				String timeleft;

				transient long startTime = 0;
			}

			/** Current thread cnt */
			int threadCnt = 1;

			Boolean multipleSubInfos = false;

			String[] infos = { null };
			String[] subinfos = { null };

			final PB pb1 = new PB();
			final PB pb2 = new PB();
			final PB pb3 = new PB();
		}

		static final String cmd = "Progress.setFullProgress";
		final Data params;

		SetFullProgress(Data data)
		{
			this.params = data;
		}
	}

	static final class SetInfos
	{
		static final String cmd = "Progress.setInfos";
		final Data params;

		static final class Data
		{
			/** Current thread cnt */
			int threadCnt = 1;

			Boolean multipleSubInfos = false;

			Data(int threadCnt, Boolean multipleSubInfos)
			{
				this.threadCnt = threadCnt;
				this.multipleSubInfos = multipleSubInfos;
			}
		}

		SetInfos(Data data)
		{
			this.params = data;
		}

	}

	static final class CanCancel
	{
		static final String cmd = "Progress.canCancel";
		final Data params;

		static final class Data
		{
			final boolean canCancel;

			Data(boolean canCancel)
			{
				this.canCancel = canCancel;
			}
		}

		CanCancel(boolean canCancel)
		{
			this.params = new Data(canCancel);
		}

	}

	static final class ClearInfos
	{
		private ClearInfos()
		{
			
		}

		static final String cmd = "Progress.clearInfos";
	}

	static final class Open
	{
		private Open()
		{
			
		}
		
		static final String cmd = "Progress";
	}

	static final class Close
	{
		static final String cmd = "Progress.close";
		final Data params;

		static final class Data
		{
			String[] errors = null;
			
			public Data(List<String> errors)
			{
				this.errors = errors.toArray(String[]::new); 
			}
		}
		
		public Close(List<String> errors)
		{
			this.params = new Data(errors);
		}
	}

	private final SetFullProgress.Data data = new SetFullProgress.Data();

	public ProgressActions(ActionsMgr ws)
	{
		this.ws = ws;
		sendOpen();
	}

	private void sendOpen()
	{
		try
		{
			if (ws.isOpen())
				ws.send(gson.toJson(new Open()));
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	public void reload(ActionsMgr ws)
	{
		this.ws = ws;
		sendOpen();
		sendSetInfos();
		sendSetProgress(0, true);
	}

	private synchronized void cleanup()
	{
		final var ct = Thread.currentThread();
		if (threadIdOffset.containsKey(ct.getId()))
		{
			final var tg = ct.getThreadGroup();
			if (threadIdOffset.size() != tg.activeCount())
			{
				final var tl = new Thread[tg.activeCount()];
				final int tl_count = tg.enumerate(tl, false);
				final var itr = threadIdOffset.entrySet().iterator();
				while (itr.hasNext())
				{
					final var e = itr.next();
					var exists = false;
					for (var i = 0; i < tl_count; i++)
					{
						if (e.getKey() == tl[i].getId())
						{
							exists = true;
							break;
						}
					}
					if (!exists)
					{
						data.infos[e.getValue()] = "";
						if (data.infos.length == data.subinfos.length)
							data.subinfos[e.getValue()] = "";
						itr.remove();
					}
				}
			}
		}
	}

	private void sendSetProgress(final int pb, final boolean force)
	{
		try
		{
			if (pb == 1)
				cleanup();
			if (force)
				ws.send(gson.toJson(new SetFullProgress(data)));
			else if (!data.pb1.visibility && !data.pb2.visibility && !data.pb3.visibility)
				ws.send(gson.toJson(new SetFullProgress(data)));
			else if (pb == 1 && data.pb1.visibility && !data.pb1.indeterminate && data.pb1.val > 0 && data.pb1.max == data.pb1.val)
				ws.send(gson.toJson(new SetFullProgress(data)));
			else if (pb == 2 && data.pb2.visibility && !data.pb2.indeterminate && data.pb2.val > 0 && data.pb2.max == data.pb2.val)
				ws.send(gson.toJson(new SetFullProgress(data)));
			else if (pb == 3 && data.pb3.visibility && !data.pb3.indeterminate && data.pb3.val > 0 && data.pb3.max == data.pb3.val)
				ws.send(gson.toJson(new SetFullProgress(data)));
			else
				ws.sendOptional(gson.toJson(new SetFullProgress(data)));
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	@Override
	public synchronized void setInfos(int threadCnt, Boolean multipleSubInfos)
	{
		threadIdOffset.clear();
		this.data.threadCnt = threadCnt <= 0 ? Runtime.getRuntime().availableProcessors() : threadCnt;
		this.data.multipleSubInfos = multipleSubInfos;
		this.data.infos = new String[this.data.threadCnt];
		if(multipleSubInfos == null)
			this.data.subinfos = new String[0];
		else
			this.data.subinfos = new String[multipleSubInfos.booleanValue() ? this.data.threadCnt : 1];
		sendSetInfos();
	}

	private void sendSetInfos()
	{
		try
		{
			if (ws.isOpen())
			{
				ws.send(gson.toJson(new SetInfos(new SetInfos.Data(data.threadCnt, data.multipleSubInfos))));
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	@Override
	public void clearInfos()
	{
		for (var i = 0; i < data.infos.length; i++)
			data.infos[i] = null;
		for (var i = 0; i < data.subinfos.length; i++)
			data.subinfos[i] = null;
		data.pb2.msg = null;
		sendClearInfos();
	}

	private void sendClearInfos()
	{
		try
		{
			if (ws.isOpen())
				ws.send(gson.toJson(new ClearInfos()));
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	@Override
	public void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		int offset = getOffset();
		if (msg != null)
			data.infos[offset] = msg;
		var force = false;
		if (val != null)
		{
			if (val < 0 && data.pb1.visibility)
			{
				data.pb1.visibility = false;
			}
			else if (val >= 0 && !data.pb1.visibility)
			{
				data.pb1.visibility = true;
			}
			data.pb1.stringPainted = val != 0;
			data.pb1.indeterminate = val == 0;
			if (max != null)
				data.pb1.max = max;
			if (val >= 0)
				data.pb1.val = val;
			if (val == 0)
				data.pb1.startTime = System.currentTimeMillis();
			if (data.pb1.val >= 0 && data.pb1.max > 0)
			{
				final var perc = data.pb1.val * 100.0f / data.pb1.max;
				force = (int) data.pb1.perc != (int) perc;
				data.pb1.perc = perc;
			}
			if (val > 0)
			{
				data.pb1.msg = String.format("%.02f%%", data.pb1.perc);
				final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - data.pb1.startTime) * (data.pb1.max - val) / val, "HH:mm:ss"); //$NON-NLS-1$
				final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - data.pb1.startTime) * data.pb1.max / val, "HH:mm:ss"); //$NON-NLS-1$
				data.pb1.timeleft = String.format("%s / %s", left, total); //$NON-NLS-1$
			}
			else
				data.pb1.timeleft = "--:--:-- / --:--:--"; //$NON-NLS-1$
		}
		if (submsg != null || (val != null && val == -1))
		{
			if (data.subinfos.length == 1)
				data.subinfos[0] = submsg;
			else if (data.subinfos.length > 1)
				data.subinfos[offset] = submsg;
		}
		sendSetProgress(1, force);
	}

	/**
	 * @return
	 */
	private synchronized int getOffset()
	{
		if (!threadIdOffset.containsKey(Thread.currentThread().getId()))
		{
			if (threadIdOffset.size() < data.threadCnt)
				threadIdOffset.put(Thread.currentThread().getId(), threadIdOffset.size());
			else
			{
				final var tg = Thread.currentThread().getThreadGroup();
				final var tl = new Thread[tg.activeCount()];
				final var tl_count = tg.enumerate(tl, false);
				var found = false;
				for (Map.Entry<Long, Integer> e : threadIdOffset.entrySet())
				{
					var exists = false;
					for (var i = 0; i < tl_count; i++)
					{
						if (e.getKey() == tl[i].getId())
						{
							exists = true;
							break;
						}
					}
					if (!exists)
					{
						threadIdOffset.remove(e.getKey());
						threadIdOffset.put(Thread.currentThread().getId(), e.getValue());
						found = true;
						break;
					}
				}
				if (!found)
					threadIdOffset.put(Thread.currentThread().getId(), 0);
			}
		}
		return threadIdOffset.get(Thread.currentThread().getId());
	}

	@Override
	public void setProgress2(String msg, Integer val, Integer max)
	{
		var force = false;
		if (msg != null && val != null)
		{
			if (!data.pb2.visibility)
				data.pb2.visibility = true;
			data.pb2.stringPainted = true/*msg != null || val > 0*/;
			data.pb2.msg = msg;
			data.pb2.indeterminate = val == 0 && msg == null;
			if (max != null)
				data.pb2.max = max;
			if (val >= 0)
				data.pb2.val = val;
			if (val == 0)
				data.pb2.startTime = System.currentTimeMillis();
			if (data.pb2.val >= 0 && data.pb2.max > 0)
			{
				final var perc = data.pb2.val * 100.0f / data.pb2.max;
				force = (int) data.pb2.perc != (int) perc;
				data.pb2.perc = perc;
			}
			if (val > 0)
			{
				if (data.pb2.msg == null)
					data.pb2.msg = String.format("%.02f", data.pb2.perc);
				final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - data.pb2.startTime) * (data.pb2.max - val) / val, "HH:mm:ss"); //$NON-NLS-1$
				final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - data.pb2.startTime) * data.pb2.max / val, "HH:mm:ss"); //$NON-NLS-1$
				data.pb2.timeleft = String.format("%s / %s", left, total); //$NON-NLS-1$
			}
			else
				data.pb2.timeleft = "--:--:-- / --:--:--"; //$NON-NLS-1$
		}
		else if (data.pb2.visibility)
			data.pb2.visibility = false;
		sendSetProgress(2, force);
	}

	@Override
	public void setProgress3(String msg, Integer val, Integer max)
	{
		var force = false;
		if (msg != null && val != null)
		{
			if (!data.pb3.visibility)
				data.pb3.visibility = true;
			data.pb3.stringPainted = true/*msg != null || val > 0*/;
			data.pb3.msg = msg;
			data.pb3.indeterminate = val == 0 && msg == null;
			if (max != null)
				data.pb3.max = max;
			if (val >= 0)
				data.pb3.val = val;
			if (val == 0)
				data.pb3.startTime = System.currentTimeMillis();
			if (data.pb3.val >= 0 && data.pb3.max > 0)
			{
				final var perc = data.pb3.val * 100.0f / data.pb3.max;
				force = (int) data.pb3.perc != (int) perc;
				data.pb3.perc = perc;
			}
			if (val > 0)
			{
				if (data.pb3.msg == null)
					data.pb3.msg = String.format("%.02f", data.pb3.perc);
				final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - data.pb3.startTime) * (data.pb3.max - val) / val, "HH:mm:ss"); //$NON-NLS-1$
				final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - data.pb3.startTime) * data.pb3.max / val, "HH:mm:ss"); //$NON-NLS-1$
				data.pb3.timeleft = String.format("%s / %s", left, total); //$NON-NLS-1$
			}
			else
				data.pb3.timeleft = "--:--:-- / --:--:--"; //$NON-NLS-1$
		}
		else if (data.pb3.visibility)
			data.pb3.visibility = false;
		sendSetProgress(3, force);
	}

	@Override
	public int getValue()
	{
		return data.pb1.val;
	}

	@Override
	public int getValue2()
	{
		return data.pb2.val;
	}

	@Override
	public int getValue3()
	{
		return data.pb3.val;
	}

	@Override
	public boolean isCancel()
	{
		return cancel;
	}

	@Override
	public void cancel()
	{
		cancel = true;
	}

	@Override
	public InputStream getInputStream(InputStream in, Integer len)
	{
		return new ProgressInputStream(in, len, this);
	}

	@Override
	public void close()
	{
		try
		{
			if (ws.isOpen())
				ws.send(gson.toJson(new Close(errors)));
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	public boolean canCancel()
	{
		return canCancel;
	}

	public void canCancel(boolean canCancel)
	{
		this.canCancel = canCancel;
		sendCanCancel();
	}

	private void sendCanCancel()
	{
		try
		{
			if (ws.isOpen())
				ws.send(gson.toJson(new CanCancel(canCancel)));
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	@Override
	public void addError(String error)
	{
		errors.add(error);
	}

}
