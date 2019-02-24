package jrm.server.ws;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

import jrm.misc.Log;

public class GlobalWS
{
	private final WebSckt ws;

	public GlobalWS(WebSckt ws)
	{
		this.ws = ws;
	}

	void setProperty(JsonObject jso)
	{
		JsonObject pjso = jso.get("params").asObject();
		for(Member m : pjso)
		{
			JsonValue value = m.getValue();
			if(value.isBoolean())
				ws.session.getUser().settings.setProperty(m.getName(), value.asBoolean());
			else if(value.isString())
				ws.session.getUser().settings.setProperty(m.getName(), value.asString());
			else
				ws.session.getUser().settings.setProperty(m.getName(), value.toString());
		}
		ws.session.getUser().settings.saveSettings();
	}
	
	@SuppressWarnings("serial")
	void setMemory(JsonObject jso)
	{
		try
		{
			if(ws.isOpen())
			{
				final Runtime rt = Runtime.getRuntime();
				String msg = (String.format(ws.session.msgs.getString("MainFrame.MemoryUsage"), String.format("%.2f MiB", rt.totalMemory() / 1048576.0), String.format("%.2f MiB", (rt.totalMemory() - rt.freeMemory()) / 1048576.0), String.format("%.2f MiB", rt.freeMemory() / 1048576.0), String.format("%.2f MiB", rt.maxMemory() / 1048576.0))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
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

	void gc(JsonObject jso)
	{
		System.gc();
		setMemory(jso);
	}
	
	@SuppressWarnings("serial")
	void warn(String msg)
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
