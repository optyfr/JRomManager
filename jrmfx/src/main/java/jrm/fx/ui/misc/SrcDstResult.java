package jrm.fx.ui.misc;

import com.eclipsesource.json.JsonObject;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jrm.aui.basic.AbstractSrcDstResult;
import jrm.aui.basic.SDRList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class SrcDstResult extends AbstractSrcDstResult
{
	private @Getter @Setter(value = AccessLevel.PROTECTED) String id = null;
	private StringProperty src;
	private StringProperty dst;
	private StringProperty result;
	private BooleanProperty selected;

	public SrcDstResult()
	{
		super();
	}

	public SrcDstResult(JsonObject jso)
	{
		super(jso);
	}

	public final StringProperty srcProperty()
	{
		if (this.src == null)
			this.src = new SimpleStringProperty();
		return this.src;
	}
	

	public final String getSrc()
	{
		return this.srcProperty().get();
	}
	

	public final void setSrc(final String src)
	{
		this.srcProperty().set(src);
	}
	

	public final StringProperty dstProperty()
	{
		if (this.dst == null)
			this.dst = new SimpleStringProperty();
		return this.dst;
	}
	

	public final String getDst()
	{
		return this.dstProperty().get();
	}
	

	public final void setDst(final String dst)
	{
		this.dstProperty().set(dst);
	}
	

	public final StringProperty resultProperty()
	{
		if (this.result == null)
			this.result = new SimpleStringProperty("");
		return this.result;
	}
	

	public final String getResult()
	{
		return this.resultProperty().get();
	}
	

	public final void setResult(final String result)
	{
		this.resultProperty().set(result);
	}
	

	public final BooleanProperty selectedProperty()
	{
		if (this.selected == null)
			this.selected = new SimpleBooleanProperty(true);
		return this.selected;
	}
	

	public final boolean isSelected()
	{
		return this.selectedProperty().get();
	}
	

	public final void setSelected(final boolean selected)
	{
		this.selectedProperty().set(selected);
	}
	
	public static SDRList<SrcDstResult> fromJSON(String json)
	{
		return fromJSON(json, SrcDstResult.class);
	}
}
