package jrm.aui.basic;

import java.util.ArrayList;
import java.util.UUID;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import lombok.Getter;

public class SrcDstResult
{
	public String id = null;
	public String src = null;
	public String dst = null;
	public String result = ""; //$NON-NLS-1$
	public boolean selected = true;

	@SuppressWarnings("serial")
	public static class SDRList extends ArrayList<SrcDstResult>
	{
		private @Getter boolean needSave = false;
		
		public SDRList()
		{
			super();
		}
	}
	
	public SrcDstResult()
	{
	}

	public SrcDstResult(String src)
	{
		this.id = UUID.randomUUID().toString();
		this.src= src;
	}

	public SrcDstResult(String src, String dst)
	{
		this.id = UUID.randomUUID().toString();
		this.src= src;
		this.dst = dst;
	}

	public SrcDstResult(JsonObject jso)
	{
		fromJSONObject(jso);
	}
	
	public JsonObject toJSONObject()
	{
		JsonObject jso = Json.object();
		jso.add("id", id != null ? id : UUID.randomUUID().toString()); //$NON-NLS-1$
		jso.add("src", src != null ? src.toString() : null); //$NON-NLS-1$
		jso.add("dst", dst != null ? dst.toString() : null); //$NON-NLS-1$
		jso.add("result", result); //$NON-NLS-1$
		jso.add("selected", selected); //$NON-NLS-1$
		return jso;
	}
	
	public void fromJSONObject(JsonObject jso)
	{
		JsonValue id = jso.get("id"); //$NON-NLS-1$
		if (id != null && id != Json.NULL)
			this.id = id.asString();
		JsonValue src = jso.get("src"); //$NON-NLS-1$
		if (src != Json.NULL)
			this.src = src.asString();
		JsonValue dst = jso.get("dst"); //$NON-NLS-1$
		if (dst != Json.NULL)
			this.dst = dst.asString();
		this.result = jso.get("result").asString(); //$NON-NLS-1$
		this.selected = jso.getBoolean("selected", true); //$NON-NLS-1$
	}
	
	public static String toJSON(SDRList list)
	{
		JsonArray array = Json.array();
		for (SrcDstResult sdr : list)
			array.add(sdr.toJSONObject());
		return array.toString();
	}
	
	public static SDRList fromJSON(String json)
	{
		SDRList sdrl = new SDRList();
		for (JsonValue arrv : Json.parse(json).asArray()) //$NON-NLS-1$ //$NON-NLS-2$
		{
			SrcDstResult sdr = new SrcDstResult(arrv.asObject());
			if(sdr.id==null)
			{
				sdrl.needSave = true;
				sdr.id = UUID.randomUUID().toString();
			}
			sdrl.add(sdr);
		}
		return sdrl;
	}
}