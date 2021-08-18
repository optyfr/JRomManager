package jrm.aui.basic;

import java.util.ArrayList;
import java.util.UUID;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import lombok.Getter;
import lombok.Setter;

public class SrcDstResult
{
	private @Getter String id = null;
	private @Getter @Setter String src = null;
	private @Getter @Setter String dst = null;
	private @Getter @Setter String result = ""; //$NON-NLS-1$
	private @Getter @Setter boolean selected = true;

	@SuppressWarnings("serial")
	public static class SDRList extends ArrayList<SrcDstResult>
	{
		private @Getter boolean needSave = false;
		
		public SDRList()
		{
			super();
		}
		
		@Override
		public boolean equals(Object o)
		{
			return super.equals(o);
		}
		
		@Override
		public int hashCode()
		{
			return super.hashCode();
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

	@SuppressWarnings("exports")
	public SrcDstResult(JsonObject jso)
	{
		fromJSONObject(jso);
	}
	
	@SuppressWarnings("exports")
	public JsonObject toJSONObject()
	{
		JsonObject jso = Json.object();
		jso.add("id", id != null ? id : UUID.randomUUID().toString()); //$NON-NLS-1$
		jso.add("src", src != null ? src : null); //$NON-NLS-1$
		jso.add("dst", dst != null ? dst : null); //$NON-NLS-1$
		jso.add("result", result); //$NON-NLS-1$
		jso.add("selected", selected); //$NON-NLS-1$
		return jso;
	}
	
	@SuppressWarnings("exports")
	public void fromJSONObject(JsonObject jso)
	{
		JsonValue lId = jso.get("id"); //$NON-NLS-1$
		if (lId != null && lId != Json.NULL)
			this.id = lId.asString();
		JsonValue lSrc = jso.get("src"); //$NON-NLS-1$
		if (lSrc != Json.NULL)
			this.src = lSrc.asString();
		JsonValue lDst = jso.get("dst"); //$NON-NLS-1$
		if (lDst != Json.NULL)
			this.dst = lDst.asString();
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