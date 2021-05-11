/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import JTrrntzip.TrrntZipStatus;
import lombok.Getter;

/**
 * the class for group of entities representation on the filesystem (ie : archive or folder)
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class Container implements Serializable, Comparable<Container>
{
	/**
	 * file or directory of the container
	 */
	private final File file;
	private final File relfile;
	
	/**
	 * Last modified date
	 */
	public @Getter long modified = 0L;
	/**
	 * file size in bytes or 0 if folder
	 */
	public @Getter long size = 0L;

	/**
	 * keep entries by name
	 */
	public final HashMap<String, Entry> entries_byname = new HashMap<>();

	/**
	 * flag for scanning removal
	 */
	public transient boolean up2date = false;
	
	/**
	 * scan load status
	 * - 0 : not scanned
	 * - 1 : quick scanned (only CRC)
	 * - 2 : deep scanned (SHA1 and MD5 for all entries)
	 */
	public int loaded = 0;

	/**
	 * last time the archive was torrentzip checked (only valid for zip archives)
	 */
	public long lastTZipCheck = 0L;
	/**
	 * last torrentzip check status (only valid for zip archives)
	 */
	public Set<TrrntZipStatus> lastTZipStatus = EnumSet.noneOf(TrrntZipStatus.class);

	/**
	 * related {@link AnywareBase} set
	 */
	public transient AnywareBase m;

	/**
	 * The container type
	 */
	public enum Type
	{
		/**
		 * Unknown type (can't be determined)
		 */
		UNK,
		/**
		 * This is a filesystem folder
		 */
		DIR,
		/**
		 * This is a zip archive
		 */
		ZIP,
		/**
		 * This is a SevenZip archive
		 */
		SEVENZIP,
		/**
		 * This is a Rar archive
		 */
		RAR,
		/**
		 * Fake container
		 */
		FAKE
	};

	private @Getter Type type = Type.UNK;

	/**
	 * Construct an archive where set is known
	 * @param type the guessed type
	 * @param file the archive {@link File}
	 * @param m the corresponding {@link AnywareBase} set
	 */
	protected Container(final Type type, final File file, final File relfile, final AnywareBase m)
	{
		this.type = type;
		this.file = file;
		this.relfile = relfile;
		this.m = m;
	}

	/**
	 * Construct an archive file with no related set
	 * @param type the guessed type
	 * @param file the archive {@link File}
	 * @param attr the file attributes (modified time and size are stored)
	 */
	protected Container(final Type type, final File file, final File relfile, final BasicFileAttributes attr)
	{
		this(type, file, relfile, (AnywareBase) null);
		modified = attr.lastModifiedTime().toMillis();
		if(type != Type.DIR)
			size = attr.size();
	}

	public File getRelFile()
	{
		return Optional.ofNullable(relfile).orElse(file);
	}
	
	public File getFile()
	{
		return file;
	}

	/**
	 * add a listed entry by their file name
	 * @param e the {@link Entry} to add
	 * @return the previous entry with the same name
	 */
	public Entry add(final Entry e)
	{
		Entry old_e;
		if(null != (old_e = entries_byname.get(e.getFile())))
			if(old_e.modified == e.modified && old_e.size == e.size)
				return old_e;
		entries_byname.put(e.getFile(), e);
		e.parent = this;
		return e;
	}

	/**
	 * find an {@link Entry} by using its name
	 * @param e the {@link Entry} to be matched
	 * @return the {@link Entry} found or null
	 */
	public Entry find(final Entry e)
	{
		return entries_byname.get(e.getFile());
	}

	/**
	 * get all the available entries as a {@link Collection} of {@link Entry}
	 * @return a {@link Collection}&lt;{@link Entry}&gt; (order not guaranteed)
	 */
	public Collection<Entry> getEntries()
	{
		return entries_byname.values();
	}

	/**
	 * get a {@link Map} of {@link Entry} by {@link String} names 
	 * @return a {@link Map}&lt;{@link String}, {@link Entry}&gt; where String is marshaled using {@link Entry#getName()}
	 */
	public Map<String, Entry> getEntriesByName()
	{
		return entries_byname.values().stream().collect(Collectors.toMap(Entry::getName, Function.identity(), (n, e) -> n));
	}

	/**
	 * get the type of a file
	 * @param file the {@link File} to get {@link Type}
	 * @return {@link Type}
	 */
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
				if(file.getName().contains(".part"))
					if(!file.getName().endsWith(".part001.rar"))
						return Type.UNK;
				return Type.RAR;
		}
		if(file.getName().endsWith(".7z.001"))
			return Type.SEVENZIP;
		return Type.UNK;
	}

	@Override
	public String toString()
	{
		return "Container " + file; //$NON-NLS-1$

	}

	@Override
	public int compareTo(Container o)
	{
		if (size < o.size)
			return -1;
		if (size > o.size)
			return 1;
		return 0;
	}
	
	public static Comparator<Container> comparator()
	{
		return new Comparator<Container>()
		{
			@Override
			public int compare(Container o1, Container o2)
			{
				return o1.compareTo(o2);
			}
		};
	}

	
	public static Comparator<Container> rcomparator()
	{
		return new Comparator<Container>()
		{
			@Override
			public int compare(Container o1, Container o2)
			{
				return -o1.compareTo(o2);
			}
		};
	}
}
