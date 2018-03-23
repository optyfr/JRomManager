package data;

import java.io.Serializable;
import java.util.ArrayList;

public class Machine implements Serializable
{
	private static final long serialVersionUID = 1L;

	public String name;
	public String romof = null;
	public String cloneof = null;
	public String sampleof = null;
	public boolean isbios =false;
	public boolean ismechanical =false;
	public boolean isdevice =false;
	
	public ArrayList<Rom> roms = new ArrayList<>();
	
	public Machine()
	{
	}

}
