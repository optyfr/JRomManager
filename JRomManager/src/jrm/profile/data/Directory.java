package jrm.profile.data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;

@SuppressWarnings("serial")
public class Directory extends Container implements Serializable
{
	public Directory(final File file, final Anyware m)
	{
		super(Type.DIR, file, m);
	}

	public Directory(final File file, final BasicFileAttributes attr)
	{
		super(Type.DIR, file, attr);
	}

}
