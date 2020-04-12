package jrm.server.shared.actions;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

import jrm.misc.Log;

public class GlobalActions
{
	private final ActionsMgr ws;

	public GlobalActions(ActionsMgr ws)
	{
		this.ws = ws;
	}

	public void setProperty(JsonObject jso)
	{
		JsonObject pjso = jso.get("params").asObject();
		for(Member m : pjso)
		{
			JsonValue value = m.getValue();
			if(value.isBoolean())
				ws.getSession().getUser().getSettings().setProperty(m.getName(), value.asBoolean());
			else if(value.isString())
				ws.getSession().getUser().getSettings().setProperty(m.getName(), value.asString());
			else
				ws.getSession().getUser().getSettings().setProperty(m.getName(), value.toString());
		}
		ws.getSession().getUser().getSettings().saveSettings();
	}
	
	@SuppressWarnings("serial")
	public void setMemory(JsonObject jso)
	{
		try
		{
			if(ws.isOpen())
			{
				final Runtime rt = Runtime.getRuntime();
				String msg = (String.format(ws.getSession().getMsgs().getString("MainFrame.MemoryUsage"), String.format("%.2f MiB", rt.totalMemory() / 1048576.0), String.format("%.2f MiB", (rt.totalMemory() - rt.freeMemory()) / 1048576.0), String.format("%.2f MiB", rt.freeMemory() / 1048576.0), String.format("%.2f MiB", rt.maxMemory() / 1048576.0))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				ws.send(new JsonObject() {{
					add("cmd", "Global.setMemory");
					add("params", new JsonObject() {{
						add("msg", msg);
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}

	public void gc(JsonObject jso)
	{
		System.gc();
		setMemory(jso);
	}
	
	@SuppressWarnings("serial")
	public void warn(String msg)
	{
		try
		{
			if(ws.isOpen())
			{
				ws.send(new JsonObject() {{
					add("cmd", "Global.warn");
					add("params", new JsonObject() {{
						add("msg", msg);
					}});
				}}.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}		
	}
}
