package jrm.profile.data;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FilenameUtils;

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
			return path.getFileName().toString();
		return path.subpath(0, path.getNameCount()).toString().replace('\\', '/');
	}

	/**
	 * This version will test against hash values and also support comparison with {@link Rom} / {@link Disk} / {@link Sample} classes
	 */
	@Override
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
			return ((Sample)obj).getName().equals(this.getName());
		}
		return super.equals(obj);
	}

	@Override
	public String toString()
	{
		return parent.file + "::" + file; //$NON-NLS-1$
	}
}
