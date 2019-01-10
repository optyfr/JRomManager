package jrm.server.ws;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

import jrm.misc.Log;
import jrm.ui.progress.ProgressHandler;
import jrm.ui.progress.ProgressInputStream;

public class ProgressWS implements ProgressHandler
{
	private WebSckt ws;
	
	/** The thread id offset. */
	private final Map<Long,Integer> threadId_Offset = new HashMap<>();
	
	/** Current thread cnt */
	private int threadCnt = 1;
	
	private boolean multipleSubInfos = false;

	/** The cancel. */
	private boolean cancel = false;
	
	private int val = 0, val2 = 0, max = 0, max2 = 0;
	
	private String infos[] = {null}, subinfos[] = {null}, msg2;
	
	public ProgressWS(WebSckt ws)
	{
		this.ws = ws;
		sendOpen();
	}
	
	private void sendOpen()
	{
		try
		{
			if(ws.isOpen())
				ws.send(Json.object().add("cmd", "Progress").toString());
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	public void reload(WebSckt ws)
	{
		this.ws = ws;
		sendOpen();
		sendSetInfos();
		for(int i = 0; i < threadCnt; i++)
			sendSetProgress(i, i==0?val:null, i==0?max:null);
		sendSetProgress2(val2, max2);
	}
	
	@Override
	public synchronized void setInfos(int threadCnt, boolean multipleSubInfos)
	{
		this.threadCnt = threadCnt;
		this.multipleSubInfos = multipleSubInfos;
		this.infos = new String[threadCnt];
		this.subinfos = new String[multipleSubInfos?threadCnt:1];
		sendSetInfos();
	}
	
	private void sendSetInfos()
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(Json.object()
					.add("cmd", "Progress.setInfos")
					.add("params", Json.object()
						.add("threadCnt", threadCnt)
						.add("multipleSubInfos", multipleSubInfos)
					).toString()
				);
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	@Override
	public void clearInfos()
	{
		for(int i = 0; i < infos.length; i++)
			infos[i] = null;			
		for(int i = 0; i < subinfos.length; i++)
			subinfos[i] = null;		
		msg2 = null;
		sendClearInfos();
	}
	
	private void sendClearInfos()
	{
		try
		{
			if(ws.isOpen())
				ws.send(Json.object().add("cmd", "Progress.clearInfos").toString());
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
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
		
		infos[offset] = msg;
		subinfos[subinfos.length==1?0:offset] = submsg;
/*		if(val!=null)
			this.val = val;*/
		if(max!=null)
			this.max = max;
		sendSetProgress(offset, val, max);
	}
	
	private long lastSetProgress = 0L;
	
	@SuppressWarnings("serial")
	private void sendSetProgress(int offset, Integer val, Integer max)
	{
		try
		{
			if(ws.isOpen())
			{
				if (System.currentTimeMillis() - lastSetProgress > 500 || (val != null && (val == 0 || val == this.max)))
				{
					ws.send(
						new JsonObject() {{
							add("cmd", "Progress.setProgress");
							add("params", new JsonObject() {{
									add("offset", offset);
									add("msg", infos[offset]);
									if(val==null)
										add("val", (String)null);
									else
										add("val", val);
									if(max==null)
										add("max", (String)null);
									else
										add("max", max);
									add("submsg", subinfos[subinfos.length==1?0:offset]);
								}}
							);
						}}.toString()
					);
					lastSetProgress = System.currentTimeMillis();
				}
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
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
		if (val != null)
			this.val2 = val;
		if (max != null)
			this.max2 = max;
		this.msg2 = msg;
		sendSetProgress2(val, max);
	}
	
	private long lastSetProgress2 = 0L;

	@SuppressWarnings("serial")
	private void sendSetProgress2(Integer val, Integer max)
	{
		try
		{
			if(ws.isOpen())
			{
				if (System.currentTimeMillis() - lastSetProgress2 > 500 || (val != null && (val == 0 || val == this.max2)))
				{
					ws.send(new JsonObject() {{
						add("cmd", "Progress.setProgress2");
						add("params", new JsonObject() {{
							add("msg", msg2);
							if (val != null)
								add("val", val);
							else
								add("val", (String)null);
							if (max != null)
								add("max", max);
							else
								add("max", (String)null);
						}});
					}}.toString());
					lastSetProgress2 = System.currentTimeMillis();
				}
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
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

	@Override
	public void close()
	{
		try
		{
			if(ws.isOpen())
				ws.send(Json.object().add("cmd", "Progress.close").toString());
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

}
