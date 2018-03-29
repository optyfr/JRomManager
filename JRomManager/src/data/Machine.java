package data;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class Machine implements Serializable
{
	public String name;
	public StringBuffer description = new StringBuffer();
	public String romof = null;
	public String cloneof = null;
	public String sampleof = null;
	public boolean isbios =false;
	public boolean ismechanical =false;
	public boolean isdevice =false;
	
	public ArrayList<Rom> roms = new ArrayList<>();
	public ArrayList<Disk> disks = new ArrayList<>();
	
	public Machine()
	{
	}

}
