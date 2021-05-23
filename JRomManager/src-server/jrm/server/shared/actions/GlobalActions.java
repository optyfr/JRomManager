package jrm.server.shared.actions;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

import jrm.misc.Log;

public class GlobalActions
{
	private static final String FF_MI_B = "%.2f MiB";
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
		try
		{
			if(ws.isOpen())
			{
				ws.getSession().getUser().getSettings().saveSettings();
				final var rjso = new JsonObject();
				rjso.add("cmd", "Global.updateProperty");
				rjso.add("params", pjso);
				ws.send(rjso.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}
	}
	
	public void setMemory(JsonObject jso)
	{
		try
		{
			if(ws.isOpen())
			{
				final var rt = Runtime.getRuntime();
				final var msg = (String.format(ws.getSession().getMsgs().getString("MainFrame.MemoryUsage"), String.format(FF_MI_B, rt.totalMemory() / 1048576.0), String.format(FF_MI_B, (rt.totalMemory() - rt.freeMemory()) / 1048576.0), String.format(FF_MI_B, rt.freeMemory() / 1048576.0), String.format(FF_MI_B, rt.maxMemory() / 1048576.0))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				final var rjso = new JsonObject();
				rjso.add("cmd", "Global.setMemory");
				final var params = new JsonObject();
				params.add("msg", msg);
				rjso.add("params", params);
				ws.send(rjso.toString());
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
	
	public void warn(String msg)
	{
		try
		{
			if(ws.isOpen())
			{
				final var rjso = new JsonObject();
				rjso.add("cmd", "Global.warn");
				final var params = new JsonObject();
				params.add("msg", msg);
				rjso.add("params", params);
				ws.send(rjso.toString());
			}
		}
		catch (IOException e)
		{
			Log.err(e.getMessage(),e);
		}		
	}
}
