package jrm.server.shared.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.eclipsesource.json.Json;
import com.google.gson.Gson;

import jrm.aui.progress.ProgressHandler;
import jrm.aui.progress.ProgressInputStream;
import jrm.misc.Log;

public class ProgressActions implements ProgressHandler
{
	private ActionsMgr ws;

	/** The thread id offset. */
	private final Map<Long, Integer> threadId_Offset = new HashMap<>();

	/** The cancel. */
	private boolean cancel = false;

	private boolean canCancel = true;

	final static class Cmd
	{
		final static class Data
		{
			final static class PB
			{
				boolean visibility = false;
				boolean stringPainted = false;
				boolean indeterminate = false;
				int max = 0;
				int val = 0;
				float perc = 0f;
				String msg = null;
				String timeleft;

				transient long startTime = 0;
			}

			/** Current thread cnt */
			int threadCnt = 1;

			boolean multipleSubInfos = false;

			String infos[] = { null };
			String subinfos[] = { null };

			final PB pb1 = new PB();
			final PB pb2 = new PB();
		}

		final String cmd = "Progress.setFullProgress";
		final Data params;

		Cmd(Data data)
		{
			this.params = data;
		}
	}

	private final Cmd.Data data = new Cmd.Data();

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
				ws.send(Json.object().add("cmd", "Progress").toString());
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
		sendSetProgress();
	}

	private void sendSetProgress()
	{
		try
		{
			if (data.pb1.val > 0 && data.pb1.max == data.pb1.val)
				ws.send(new Gson().toJson(new Cmd(data)));
			else
				ws.sendOptional(new Gson().toJson(new Cmd(data)));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void setInfos(int threadCnt, boolean multipleSubInfos)
	{
		this.data.threadCnt = threadCnt;
		this.data.multipleSubInfos = multipleSubInfos;
		this.data.infos = new String[threadCnt];
		this.data.subinfos = new String[multipleSubInfos ? threadCnt : 1];
		sendSetInfos();
	}

	private void sendSetInfos()
	{
		try
		{
			if (ws.isOpen())
			{
				ws.send(Json.object().add("cmd", "Progress.setInfos").add("params", Json.object().add("threadCnt", data.threadCnt).add("multipleSubInfos", data.multipleSubInfos)).toString());
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
		for (int i = 0; i < data.infos.length; i++)
			data.infos[i] = null;
		for (int i = 0; i < data.subinfos.length; i++)
			data.subinfos[i] = null;
		data.pb2.msg = null;
		sendClearInfos();
	}

	private void sendClearInfos()
	{
		try
		{
			if (ws.isOpen())
				ws.send(Json.object().add("cmd", "Progress.clearInfos").toString());
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	@Override
	public void setProgress(String msg)
	{
		setProgress(msg, null, null, null);
	}

	@Override
	public void setProgress(String msg, Integer val)
	{
		setProgress(msg, val, null, null);
	}

	@Override
	public void setProgress(String msg, Integer val, Integer max)
	{
		setProgress(msg, val, max, null);
	}

	@Override
	public synchronized void setProgress(String msg, Integer val, Integer max, String submsg)
	{
		if (!threadId_Offset.containsKey(Thread.currentThread().getId()))
		{
			if (threadId_Offset.size() < data.threadCnt)
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
		int offset = threadId_Offset.get(Thread.currentThread().getId());
		if (msg != null)
			data.infos[offset] = msg;
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
			if (val > 0)
				data.pb1.val = val;
			if (val == 0)
				data.pb1.startTime = System.currentTimeMillis();
			if (val > 0)
			{
				data.pb1.perc = data.pb1.val * 100.0f / data.pb1.max;
				data.pb1.msg = String.format("%.02f%%", data.pb1.perc);
				final String left = DurationFormatUtils.formatDuration((System.currentTimeMillis() - data.pb1.startTime) * (data.pb1.max - val) / val, "HH:mm:ss"); //$NON-NLS-1$
				final String total = DurationFormatUtils.formatDuration((System.currentTimeMillis() - data.pb1.startTime) * data.pb1.max / val, "HH:mm:ss"); //$NON-NLS-1$
				data.pb1.timeleft = String.format("%s / %s", left, total); //$NON-NLS-1$
			}
			else
				data.pb1.timeleft = "--:--:-- / --:--:--"; //$NON-NLS-1$
		}
		if (data.subinfos.length == 1)
			data.subinfos[0] = submsg;
		else
			data.subinfos[offset] = submsg;
		sendSetProgress();
	}

	@Override
	public void setProgress2(String msg, Integer val)
	{
		setProgress2(msg, val, null);
	}

	@Override
	public void setProgress2(String msg, Integer val, Integer max)
	{
		if (msg != null && val != null)
		{
			if (!data.pb2.visibility)
				data.pb2.visibility = true;
			data.pb2.stringPainted = val != 0;
			data.pb2.msg = msg;
			data.pb2.indeterminate = val == 0;
			if (max != null)
				data.pb2.max = max;
			if (val > 0)
				data.pb2.val = val;
			if (val == 0)
				data.pb2.startTime = System.currentTimeMillis();
			if (val > 0)
			{
				data.pb2.perc = data.pb2.val * 100.0f / data.pb2.max;
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
		sendSetProgress();
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
				ws.send(Json.object().add("cmd", "Progress.close").toString());
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
				ws.send(Json.object().add("cmd", "Progress.canCancel").add("params", Json.object().add("canCancel", canCancel)).toString());
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

}
