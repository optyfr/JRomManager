package jrm.data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;

@SuppressWarnings("serial")
public class Directory extends Container implements Serializable
{
	public Directory(File file, Machine m)
	{
		super(Type.DIR, file, m);
	}

	public Directory(File file, BasicFileAttributes attr)
	{
		super(Type.DIR, file, attr);
	}

}
