package data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;

@SuppressWarnings("serial")
public class Container implements Serializable
{
	public File file;
	public long modified;
	public long size = 0;
	public HashMap<String, Entry> entries_byname = new HashMap<>();

	public transient boolean up2date = false;
	public int loaded = 0;

	public enum Type
	{
		ZIP, DIR
	};

	private Type type;

	protected Container(Type type, File file)
	{
		this.type = type;
		this.file = file;
	}

	protected Container(Type type, File file, BasicFileAttributes attr)
	{
		this(type, file);
		this.modified = attr.lastModifiedTime().toMillis();
		if (type == Type.ZIP)
			this.size = attr.size();
	}

	public Entry add(Entry e)
	{
		Entry old_e;
		if(null!=(old_e=entries_byname.get(e.file)))
			if(old_e.modified  == e.modified && old_e.size == e.size)
				return old_e;
		entries_byname.put(e.file, e);
		e.parent = this;
		return e;
	}

	public Entry find(Entry e)
	{
		return entries_byname.get(e.file);
	}

	public Collection<Entry> getEntries()
	{
		return entries_byname.values();
	}
	
	public Type getType()
	{
		return type;
	}

}
