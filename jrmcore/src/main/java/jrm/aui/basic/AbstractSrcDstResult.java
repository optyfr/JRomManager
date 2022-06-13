package jrm.aui.basic;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public abstract class AbstractSrcDstResult
{
	protected AbstractSrcDstResult()
	{
	}
	
	protected AbstractSrcDstResult(JsonObject jso)
	{
		fromJSONObject(jso);
	}

	public static String toJSON(List<? extends AbstractSrcDstResult> list)
	{
		JsonArray array = Json.array();
		for (final var sdr : list)
			array.add(sdr.toJSONObject());
		return array.toString();
	}

	public abstract void setSelected(boolean selected);

	public abstract boolean isSelected();

	public abstract void setResult(String result);

	public abstract String getResult();

	public abstract void setDst(String dst);

	public abstract String getDst();

	public abstract void setSrc(String src);

	public abstract String getSrc();

	protected abstract void setId(String id);

	public abstract String getId();

	@SuppressWarnings("exports")
	public JsonObject toJSONObject()
	{
		JsonObject jso = Json.object();
		jso.add("id", Optional.ofNullable(getId()).orElse(UUID.randomUUID().toString())); //$NON-NLS-1$
		jso.add("src", getSrc()); //$NON-NLS-1$
		jso.add("dst", getDst()); //$NON-NLS-1$
		jso.add("result", getResult()); //$NON-NLS-1$
		jso.add("selected", isSelected()); //$NON-NLS-1$
		return jso;
	}

	@SuppressWarnings("exports")
	public void fromJSONObject(JsonObject jso)
	{
		JsonValue lId = jso.get("id"); //$NON-NLS-1$
		if (lId != null && lId != Json.NULL)
			setId(lId.asString());
		JsonValue lSrc = jso.get("src"); //$NON-NLS-1$
		if (lSrc != Json.NULL)
			setSrc(lSrc.asString());
		JsonValue lDst = jso.get("dst"); //$NON-NLS-1$
		if (lDst != Json.NULL)
			setDst(lDst.asString());
		setResult(jso.get("result").asString()); //$NON-NLS-1$
		setSelected(jso.getBoolean("selected", true)); //$NON-NLS-1$
	}
	
	public static <T extends AbstractSrcDstResult> SDRList<T> fromJSON(String json, Class<T> cls)
	{
		final var sdrl = new SDRList<T>();
		for (JsonValue arrv : Json.parse(json).asArray()) //$NON-NLS-1$ //$NON-NLS-2$
		{
			try
			{
				final var sdr = cls.getDeclaredConstructor(JsonObject.class).newInstance(arrv.asObject());
				if(sdr.getId()==null)
				{
					sdrl.needSave = true;
					sdr.setId(UUID.randomUUID().toString());
				}
				sdrl.add(sdr);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return sdrl;
	}
}
