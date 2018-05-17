package jrm.profile.data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;

@SuppressWarnings("serial")
public class Archive extends Container implements Serializable
{
	public Archive(final File file, final AnywareBase m)
	{
		super(Container.getType(file), file, m);
	}

	public Archive(final File file, final BasicFileAttributes attr)
	{
		super(Container.getType(file), file, attr);
	}

}
