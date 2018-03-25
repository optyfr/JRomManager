package data;

import java.io.File;
import java.io.Serializable;

@SuppressWarnings("serial")
public class Directory extends Container implements Serializable
{
	public Directory(File file)
	{
		super(Type.DIR, file);
	}

}
