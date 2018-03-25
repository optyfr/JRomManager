package data;

import java.io.File;
import java.io.Serializable;

@SuppressWarnings("serial")
public class Archive extends Container implements Serializable
{
	public Archive(File file, boolean create)
	{
		super(Type.ARC, file, create);
	}

	public Archive(File file)
	{
		super(Type.ARC, file);
	}

}
