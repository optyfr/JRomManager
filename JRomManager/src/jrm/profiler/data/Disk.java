package jrm.profiler.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
		if(merge!=null && Machine.merge_mode.isMerge())
		{
			if(isCollisionMode())
				return parent.name+"/"+merge+".chd";
			return merge+".chd";
		}
		if(isCollisionMode())
			return parent.name+"/"+name+".chd";
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
	
	@Override
	public int hashCode()
	{
		if(this.sha1!=null)
			return this.sha1.hashCode();
		if(this.md5!=null)
			return this.md5.hashCode();
		return super.hashCode();
	}

	public String hashString()
	{
		if(this.sha1!=null)
			return this.sha1;
		if(this.md5!=null)
			return this.md5;
		return this.getName();
	}
	
	public static Map<String,Disk> getDisksByName(List<Disk> disks)
	{
		return disks.stream().collect(Collectors.toMap(Disk::getName, Function.identity(), (n, r) -> n));
	}
}
