package jrm.data;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jrm.profiler.scan.MergeOptions;

@SuppressWarnings("serial")
public class Disk extends Entity implements Serializable
{
	public String sha1 = null;
	public String md5 = null;
	public String merge = null;
	public String status = "";
	
	public Disk(Machine parent)
	{
		super(parent);
	}

	@Override
	public String getName()
	{
		if(merge!=null && EnumSet.of(MergeOptions.MERGE,MergeOptions.FULLMERGE).contains(Machine.merge_mode))
			return merge+".chd";
		return name+".chd";
	}
	
	@Override
	public void setName(String name)
	{
		this.name=name;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Disk)
		{
			if(((Disk)obj).sha1!=null && this.sha1 !=null)
				return ((Disk)obj).sha1.equals(this.sha1);
			if(((Disk)obj).md5!=null && this.md5 !=null)
				return ((Disk)obj).md5.equals(this.md5);
		}
		return super.equals(obj);
	}
	
	public static Map<String,Disk> getDisksByName(List<Disk> disks)
	{
		return disks.stream().collect(Collectors.toMap(Disk::getName, Function.identity(), (n, r) -> n));
	}
}
