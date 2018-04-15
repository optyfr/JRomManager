package jrm.profiler.data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

@SuppressWarnings("serial")
public class Container implements Serializable
{
	public File file;
	public long modified;
	public long size = 0;
	public HashMap<String, Entry> entries_byname = new HashMap<>();

	public transient boolean up2date = false;
	public int loaded = 0;

	public transient Anyware m;

	public enum Type
	{
		UNK,
		DIR,
		ZIP,
		SEVENZIP,
		RAR
	};

	private Type type = Type.UNK;

	protected Container(Type type, File file, Anyware m)
	{
		this.type = type;
		this.file = file;
		this.m = m;
	}

	protected Container(Type type, File file, BasicFileAttributes attr)
	{
		this(type, file, (Machine) null);
		this.modified = attr.lastModifiedTime().toMillis();
		if(type != Type.DIR)
			this.size = attr.size();
	}

	public Entry add(Entry e)
	{
		Entry old_e;
		if(null != (old_e = entries_byname.get(e.file)))
			if(old_e.modified == e.modified && old_e.size == e.size)
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

	public Map<String, Entry> getEntriesByName()
	{
		return entries_byname.values().stream().collect(Collectors.toMap(Entry::getName, Function.identity(), (n, e) -> n));
	}

	public Type getType()
	{
		return type;
	}

	public static Type getType(File file)
	{
		String ext = FilenameUtils.getExtension(file.getName());
		switch(ext.toLowerCase())
		{
			case "zip":
				return Type.ZIP;
			case "7z":
				return Type.SEVENZIP;
			case "rar":
				return Type.RAR;
		}
		return Type.UNK;
	}

	@Override
	public String toString()
	{
		return "Container " + file;

	}
}
