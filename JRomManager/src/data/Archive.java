package data;

import java.io.File;
import java.io.Serializable;

public class Archive extends Container implements Serializable
{
	private static final long serialVersionUID = -4503259479317531840L;

	public Archive(File file, boolean create)
	{
		super(Type.ARC, file, create);
	}

	public Archive(File file)
	{
		super(Type.ARC, file);
	}

}
