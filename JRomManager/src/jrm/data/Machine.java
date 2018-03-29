package jrm.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

	public List<Disk> filterDisks()
	{
		return disks.stream().filter(d -> {
			if(d.status.equals("nodump"))
				return false;
			return isbios || romof == null || d.merge == null;
		}).collect(Collectors.toList());
	}
	
	public List<Rom> filterRoms()
	{
		return roms.stream().filter(r -> {
			if(r.status.equals("nodump"))
				return false;
			if (r.crc == null)
				return false;
			return isbios || romof == null || r.merge == null;
		}).collect(Collectors.toList());		
	}
}
