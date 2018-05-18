package jrm.profile.data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import JTrrntzip.TrrntZipStatus;

@SuppressWarnings("serial")
public class  Container implements Serializable
{
	public final File file;
	public long modified = 0L;
	public long size = 0L;
	public final HashMap<String, Entry> entries_byname = new HashMap<>();

	public transient boolean up2date = false;
	public int loaded = 0;

	public long lastTZipCheck = 0L;
	public EnumSet<TrrntZipStatus> lastTZipStatus = EnumSet.noneOf(TrrntZipStatus.class);

	public transient AnywareBase m;

	public enum Type
	{
		UNK,
		DIR,
		ZIP,
		SEVENZIP,
		RAR
	};

	private Type type = Type.UNK;

	protected Container(final Type type, final File file, final AnywareBase m)
	{
		this.type = type;
		this.file = file;
		this.m = m;
	}

	protected Container(final Type type, final File file, final BasicFileAttributes attr)
	{
		this(type, file, (AnywareBase) null);
		modified = attr.lastModifiedTime().toMillis();
		if(type != Type.DIR)
			size = attr.size();
	}

	public Entry add(final Entry e)
	{
		Entry old_e;
		if(null != (old_e = entries_byname.get(e.file)))
			if(old_e.modified == e.modified && old_e.size == e.size)
				return old_e;
		entries_byname.put(e.file, e);
		e.parent = this;
		return e;
	}

	public Entry find(final Entry e)
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

	public static Type getType(final File file)
	{
		final String ext = FilenameUtils.getExtension(file.getName());
		switch(ext.toLowerCase())
		{
			case "zip": //$NON-NLS-1$
				return Type.ZIP;
			case "7z": //$NON-NLS-1$
				return Type.SEVENZIP;
			case "rar": //$NON-NLS-1$
				return Type.RAR;
		}
		return Type.UNK;
	}

	@Override
	public String toString()
	{
		return "Container " + file; //$NON-NLS-1$

	}
}
