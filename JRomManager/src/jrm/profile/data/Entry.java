package jrm.profile.data;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FilenameUtils;

@SuppressWarnings("serial")
public class Entry implements Serializable
{
	public String file;
	public long size = 0;
	public long modified = 0;
	public String crc = null;
	public String sha1 = null;
	public String md5 = null;
	public Container parent = null;
	public Type type = Type.UNK;

	public enum Type
	{
		UNK,
		CHD
	};

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

	public Entry(final String file, final BasicFileAttributes attr)
	{
		this(file);
		size = attr.size();
		modified = attr.lastModifiedTime().toMillis();
	}

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
