package data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;

import org.apache.commons.io.FilenameUtils;

@SuppressWarnings("serial")
public class Archive extends Container implements Serializable
{
	public Archive(File file)
	{
		super(getType(file), file);
	}

	public Archive(File file, BasicFileAttributes attr)
	{
		super(getType(file), file, attr);
	}

	private static Type getType(File file)
	{
		String ext = FilenameUtils.getExtension(file.getName());
		switch(ext.toLowerCase())
		{
			case "zip": return Type.ZIP;
		}
		return null;
	}

}
