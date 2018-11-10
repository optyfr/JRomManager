package jrm.ui.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class SrcDstResult
{
	public File src = null;
	public File dst = null;
	public String result = ""; //$NON-NLS-1$
	public boolean selected = true;

	public JsonObject toJSONObject()
	{
		JsonObject jso = Json.object();
		jso.add("src", src != null ? src.getAbsolutePath() : null); //$NON-NLS-1$
		jso.add("dst", dst != null ? dst.getAbsolutePath() : null); //$NON-NLS-1$
		jso.add("result", result); //$NON-NLS-1$
		jso.add("selected", selected); //$NON-NLS-1$
		return jso;
	}
	
	public SrcDstResult()
	{
		
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj != null && obj instanceof SrcDstResult)
			return src.equals(((SrcDstResult)obj).src);
		return false;
	}
	
	public SrcDstResult(JsonObject jso)
	{
		fromJSONObject(jso);
	}
	
	public void fromJSONObject(JsonObject jso)
	{
		JsonValue src = jso.get("src"); //$NON-NLS-1$
		if (src != Json.NULL)
			this.src = new File(src.asString());
		JsonValue dst = jso.get("dst"); //$NON-NLS-1$
		if (dst != Json.NULL)
			this.dst = new File(dst.asString());
		this.result = jso.get("result").asString(); //$NON-NLS-1$
		this.selected = jso.getBoolean("selected", true); //$NON-NLS-1$
	}
	
	public static String toJSON(List<SrcDstResult> list)
	{
		JsonArray array = Json.array();
		for (SrcDstResult sdr : list)
			array.add(sdr.toJSONObject());
		return array.toString();
	}
	
	public static List<SrcDstResult> fromJSON(String json)
	{
		List<SrcDstResult> sdrl = new ArrayList<>();
		for (JsonValue arrv : Json.parse(json).asArray()) //$NON-NLS-1$ //$NON-NLS-2$
			sdrl.add(new SrcDstResult(arrv.asObject()));
		return sdrl;
	}
}