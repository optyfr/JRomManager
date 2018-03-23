package data;

import java.io.Serializable;

public class Entry implements Serializable
{
	private static final long serialVersionUID = 833774163282972425L;

	public String file;
	public long size = 0;
	public String crc = null;
	public String sha1 = null;
	public Container parent = null; 
	
	public Entry(String file)
	{
		this.file = file;
	}

}
