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

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FilenameUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Entry is a container item (ie: a directory file or an archive entry)
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class Entry implements Serializable
{
	/**
	 * the entry name (with relative path)
	 */
	public String file;
	/**
	 * the entry size
	 */
	public long size = 0;
	/**
	 * the modified date (unix time)
	 */
	public long modified = 0;
	/**
	 * the crc value as a lower case hex string
	 */
	public String crc = null;
	/**
	 * the sha1 value as a lower case hex string
	 */
	public String sha1 = null;
	/**
	 * the md5 value as a lower case hex string
	 */
	public String md5 = null;
	/**
	 * the parent {@link Container}
	 */
	public Container parent = null;
	/**
	 * The entry type
	 */
	public Type type = Type.UNK;

	/**
	 * Entry type definition
	 */
	public enum Type
	{
		/**
		 * unknown type
		 */
		UNK,
		/**
		 * mame's CHD disk
		 */
		CHD
	};

	/**
	 * construct based of a file string
	 * @param file the file string (with relative path), extension will be tested against known types
	 */
	public Entry(final String file)
	{
		this.file = file;
		final String ext = FilenameUtils.getExtension(file);
		switch(ext.toLowerCase())
		{
			case "chd": //$NON-NLS-1$
				type = Type.CHD;
				break;
		}
	}

	/**
	 * construct based of a file string and its fetched attributes
	 * @param file the file string (with relative path), extension will be tested against known types
	 * @param attr the attributes as {@link BasicFileAttributes} class, will get file size and last modified time 
	 */
	public Entry(final String file, final BasicFileAttributes attr)
	{
		this(file);
		size = attr.size();
		modified = attr.lastModifiedTime().toMillis();
	}

	/**
	 * get the relativized (against its parent) and normalized name (according parent or file type)
	 * @return the relativized name string
	 */
	public String getName()
	{
		final Path path = Paths.get(file);
		if(parent.getType() == Container.Type.DIR)
		{
			//	System.out.println(parent.file.toPath().relativize(path).toString().replace('\\', '/'));
			return parent.file.toPath().relativize(path).toString().replace('\\', '/');
		}
		if(type == Type.CHD)
		{
			Path fileName = path.getFileName();
			if(fileName==null)
				return null;
			return fileName.toString();
		}
		return path.subpath(0, path.getNameCount()).toString().replace('\\', '/');
	}

	/**
	 * This version will test against hash values and also support comparison with {@link Rom} / {@link Disk} / {@link Sample} classes
	 */
	@Override
	@SuppressFBWarnings
	public boolean equals(final Object obj)
	{
		if(obj instanceof Entry)
		{
			if(((Entry) obj).sha1 != null && sha1 != null)
				return ((Entry) obj).sha1.equals(sha1);
			if(((Entry) obj).md5 != null && md5 != null)
				return ((Entry) obj).md5.equals(md5);
			if(((Entry) obj).crc != null && crc != null)
				return ((Entry) obj).crc.equals(crc) && ((Entry) obj).size == size;
			if(((Entry) obj).modified != 0 && modified != 0)
				return ((Entry) obj).modified == modified && ((Entry) obj).size == size;
		}
		else if(obj instanceof Rom)
		{
			if(((Rom) obj).sha1 != null && sha1 != null)
				return ((Rom) obj).sha1.equals(sha1);
			if(((Rom) obj).md5 != null && md5 != null)
				return ((Rom) obj).md5.equals(md5);
			if(((Rom) obj).crc != null && crc != null)
				return ((Rom) obj).crc.equals(crc) && ((Rom) obj).size == size;
		}
		else if(obj instanceof Disk)
		{
			if(((Disk) obj).sha1 != null && sha1 != null)
				return ((Disk) obj).sha1.equals(sha1);
			if(((Disk) obj).md5 != null && md5 != null)
				return ((Disk) obj).md5.equals(md5);
		}
		else if(obj instanceof Sample)
		{
			return ((Sample)obj).getNormalizedName().equals(this.getName());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return super.hashCode();
	}
	
	@Override
	public String toString()
	{
		return parent.file + "::" + file; //$NON-NLS-1$
	}
}
