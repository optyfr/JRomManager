package jrm.aui.basic;

import java.util.UUID;

import com.eclipsesource.json.JsonObject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class SrcDstResult extends AbstractSrcDstResult
{
	private @Getter @Setter(value = AccessLevel.PROTECTED) String id = null;
	private @Getter @Setter String src = null;
	private @Getter @Setter String dst = null;
	private @Getter @Setter String result = ""; //$NON-NLS-1$
	private @Getter @Setter boolean selected = true;

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
}