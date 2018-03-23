package data;

import java.io.Serializable;

public class Rom implements Serializable
{
	private static final long serialVersionUID = 1L;

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

}
