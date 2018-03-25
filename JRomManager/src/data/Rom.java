package data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Rom implements Serializable
{
	public String name;
	public long size = 0;
	public String crc = null;
	public String sha1 = null;
	public String merge = null;
	public String bios = null;
	public String status = "";
	
	public Rom()
	{
	}

	public String getName()
	{
		return name;
	}
	
}
