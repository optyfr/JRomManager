package data;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("serial")
public class Entry implements Serializable
{
	public String file;
	public long size = 0;
	public String crc = null;
	public String sha1 = null;
	public Container parent = null; 
	
	public Entry(String file)
	{
		this.file = file;
	}

	public String getName()
	{
		Path path = Paths.get(file);
		return path.subpath(0, path.getNameCount()).toString();
	}
	
}
