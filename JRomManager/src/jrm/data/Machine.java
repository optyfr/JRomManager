package jrm.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jrm.profiler.scan.MergeOptions;

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
	
	public Machine parent = null;
	
	public static transient MergeOptions merge_mode;
	
	public Machine()
	{
	}

	public List<Disk> filterDisks(MergeOptions merge_mode)
	{
		Machine.merge_mode = merge_mode;
		return disks.stream().filter(d -> {
			if(d.status.equals("nodump"))
				return false;
			if(merge_mode==MergeOptions.SPLIT && d.merge!=null)
				return false;
			if(merge_mode==MergeOptions.NOMERGE && d.merge!=null)
				return true;
			return this.isbios || this.romof == null || d.merge==null;
		}).collect(Collectors.toList());
	}
	
	public List<Rom> filterRoms(MergeOptions merge_mode)
	{
		Machine.merge_mode = merge_mode;
		return roms.stream().filter(r -> {
			if(r.status.equals("nodump"))
				return false;
			if (r.crc == null)
				return false;
			if(merge_mode==MergeOptions.SPLIT && r.merge!=null)
				return false;
			if(merge_mode==MergeOptions.NOMERGE && r.bios!=null)
				return false;
			if(merge_mode==MergeOptions.NOMERGE && r.merge!=null)
				return true;
			if(merge_mode==MergeOptions.FULLNOMERGE && r.bios!=null)
				return true;
			if(merge_mode==MergeOptions.FULLNOMERGE && r.merge!=null)
				return true;
			if(merge_mode==MergeOptions.MERGE && r.bios!=null)
				return false;
			if(merge_mode==MergeOptions.MERGE && r.merge!=null)
				return true;
			if(merge_mode==MergeOptions.FULLMERGE && r.bios!=null)
				return true;
			if(merge_mode==MergeOptions.FULLMERGE && r.merge!=null)
				return true;
			return isbios || romof == null || r.merge==null;
		}).collect(Collectors.toList());		
	}
	
	public Machine getDestMachine(MergeOptions merge_mode)
	{
		Machine.merge_mode = merge_mode;
		switch(merge_mode)
		{
			case SPLIT: 		return this;
			case FULLNOMERGE:	return this;
			case NOMERGE:		return this;
			case MERGE:			return parent!=null&&!parent.isbios?parent.getDestMachine(merge_mode):this;
			case FULLMERGE:		return parent!=null&&!parent.isbios?parent.getDestMachine(merge_mode):this;
		}
		return this;
	}
}
