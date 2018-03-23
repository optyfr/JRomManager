package data;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class Container implements Serializable
{
	private static final long serialVersionUID = -6589746482843591315L;
	
	public File file;
	public long modified;
	public ArrayList<Entry> entries = new ArrayList<>();
	
	public transient boolean up2date = false;
	public int loaded = 0;
	
	protected enum Type
	{
		ARC, DIR
	};

	private Type type;
	
	protected Container(Type type, File file, boolean create)
	{
		this.type = type;
		this.file = file;
		if(!create)
			this.modified = file.lastModified();
	}

	protected Container(Type type, File file)
	{
		this(type,file,false);
	}

	public void add(Entry e)
	{
		entries.add(e);
		e.parent = this;
	}
	
	protected Type getType()
	{
		return type;
	}
	
}
