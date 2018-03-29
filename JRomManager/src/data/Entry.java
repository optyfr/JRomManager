package data;

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
	public Container parent = null;
	public Type type = Type.UNK;
	
	public enum Type
	{
		UNK,
		CHD
	};

	
	public Entry(String file)
	{
		this.file = file;
		String ext = FilenameUtils.getExtension(file);
		switch(ext.toLowerCase())
		{
			case "chd":
				type = Type.CHD;
				break;
		}
	}

	public Entry(String file, BasicFileAttributes attr)
	{
		this(file);
		this.size = attr.size();
		this.modified = attr.lastModifiedTime().toMillis();
	}

	public String getName()
	{
		Path path = Paths.get(file);
		return path.subpath(0, path.getNameCount()).toString();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof Entry)
		{
			if (((Entry) obj).sha1 != null && this.sha1 != null)
				return ((Entry) obj).sha1.equals(this.sha1);
			if (((Entry) obj).crc != null && this.crc != null)
				return ((Entry) obj).crc.equals(this.crc) && ((Entry) obj).size == this.size;
			if (((Entry) obj).modified != 0 && this.modified != 0)
				return ((Entry) obj).modified == this.modified && ((Entry) obj).size == this.size;
		}
		return super.equals(obj);
	}

}
