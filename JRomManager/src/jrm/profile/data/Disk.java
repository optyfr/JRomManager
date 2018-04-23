package jrm.profile.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class Disk extends Entity implements Serializable
{
	public String merge = null;

	public Disk(Anyware parent)
	{
		super(parent);
	}

	@Override
	public String getName()
	{
		if(merge != null && Machine.merge_mode.isMerge())
		{
			if(isCollisionMode())
				return parent.name + "/" + merge + ".chd";
			return merge + ".chd";
		}
		if(isCollisionMode())
			return parent.name + "/" + name + ".chd";
		return name + ".chd";
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Disk)
		{
			if(((Disk) obj).sha1 != null && this.sha1 != null)
				return ((Disk) obj).sha1.equals(this.sha1);
			if(((Disk) obj).md5 != null && this.md5 != null)
				return ((Disk) obj).md5.equals(this.md5);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		if(this.sha1 != null)
			return this.sha1.hashCode();
		if(this.md5 != null)
			return this.md5.hashCode();
		return super.hashCode();
	}

	public String hashString()
	{
		if(this.sha1 != null)
			return this.sha1;
		if(this.md5 != null)
			return this.md5;
		return this.getName();
	}

	public static Map<String, Disk> getDisksByName(List<Disk> disks)
	{
		return disks.stream().collect(Collectors.toMap(Disk::getName, Function.identity(), (n, r) -> n));
	}

	private EntityStatus findDiskStatus(Anyware parent, Disk disk)
	{
		if(parent.parent!=null)	// find same disk in parent clone (if any and recursively)
		{
			for(Disk d : parent.parent.disks.values())
			{
				if(disk.equals(d))
					return d.getStatus();
			}
		}
		return null;
	}
	
	public EntityStatus getStatus()
	{
		if(status != Status.good)
			return EntityStatus.OK;
		if(own_status==EntityStatus.UNKNOWN)
		{
			if(this.merge != null)
			{
				EntityStatus status = findDiskStatus(parent, this);
				if(status != null)
					return status;
			}
		}
		return own_status;
	}

}
