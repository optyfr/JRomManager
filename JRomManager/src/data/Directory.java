package data;

import java.io.File;
import java.io.Serializable;

public class Directory extends Container implements Serializable
{
	private static final long serialVersionUID = -7272121238809964552L;

	public Directory(File file)
	{
		super(Type.DIR, file);
	}

}
