package jrm.aui.basic;

import java.util.UUID;

import com.eclipsesource.json.JsonObject;

public class SrcDstResult extends AbstractSrcDstResult
{
	private String id;
	private String src;
	private String dst;
	private String result;
	private boolean selected;

	public SrcDstResult()
	{
		super();
	}

	public SrcDstResult(String src)
	{
		super();
		this.id = UUID.randomUUID().toString();
		this.src= src;
	}

	public SrcDstResult(String src, String dst)
	{
		super();
		this.id = UUID.randomUUID().toString();
		this.src= src;
		this.dst = dst;
	}

	public SrcDstResult(JsonObject jso)
	{
		super(jso);
	}
	
	public static SDRList<SrcDstResult> fromJSON(String json)
	{
		return fromJSON(json, SrcDstResult.class);
	}

	@Override
	public String getId()
	{
		return id;
	}

	@Override
	protected void setId(String id)
	{
		this.id = id;
	}

	@Override
	public String getSrc()
	{
		return src;
	}

	@Override
	public void setSrc(String src)
	{
		this.src = src;
	}

	@Override
	public String getDst()
	{
		return dst;
	}

	@Override
	public void setDst(String dst)
	{
		this.dst = dst;
	}

	@Override
	public String getResult()
	{
		return result;
	}

	@Override
	public void setResult(String result)
	{
		this.result = result;
	}

	@Override
	public boolean isSelected()
	{
		return selected;
	}

	@Override
	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}
}