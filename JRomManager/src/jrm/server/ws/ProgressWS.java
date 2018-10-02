package jrm.server.ws;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import jrm.ui.progress.ProgressHandler;
import jrm.ui.progress.ProgressInputStream;

public class ProgressWS implements ProgressHandler
{
	private final WebSckt ws;
	
	/** The thread id offset. */
	private final Map<Long,Integer> threadId_Offset = new HashMap<>();
	
	/** Current thread cnt */
	private int threadCnt = 1;

	/** The cancel. */
	private boolean cancel = false;
	
	private int val = 0, val2 = 0;
	
	public ProgressWS(WebSckt ws)
	{
		this.ws = ws;
		try
		{
			ws.send(Json.object().add("cmd", "Progress").toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void setInfos(int threadCnt, boolean multipleSubInfos)
	{
		this.threadCnt = threadCnt;
		try
		{
			ws.send(Json.object()
				.add("cmd", "Progress.setInfos")
				.add("params", Json.object()
					.add("threadCnt", threadCnt)
					.add("multipleSubInfos", multipleSubInfos)
				).toString()
			);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void clearInfos()
	{
		try
		{
			ws.send(Json.object().add("cmd", "Progress.clearInfos").toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
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

	@SuppressWarnings("serial")
	@Override
	public synchronized void setProgress(String msg, Integer val, Integer max, String submsg)
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
		int offset = threadId_Offset.get(Thread.currentThread().getId());
		if (val != null && val > 0)
			this.val = val;
		try
		{
			ws.send(
				new JsonObject() {{
					add("cmd", "Progress.setProgress");
					add("params", new JsonObject() {{
							add("offset", offset);
							add("msg", msg);
							if(val==null)
								add("val", (String)null);
							else
								add("val", val);
							if(max==null)
								add("max", (String)null);
							else
								add("max", max);
							add("submsg", submsg);
						}}
					);
				}}.toString()
			);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setProgress2(String msg, Integer val)
	{
		setProgress2(msg, val, null);
	}

	@Override
	public void setProgress2(String msg, Integer val, Integer max)
	{
		if (val != null && val > 0)
			this.val2 = val;
		try
		{
			ws.send(Json.object()
				.add("cmd", "Progress.setProgress2")
				.add("params", Json.object()
					.add("msg", msg)
					.add("val", val)
					.add("max", max)
				).toString()
			);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public int getValue()
	{
		return val;
	}

	@Override
	public int getValue2()
	{
		return val2;
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

}
