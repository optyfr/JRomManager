package jrm.profiler.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class Rom extends Entity implements Serializable
{
	public String merge = null;
	public String bios = null;

	public Rom(Anyware parent)
	{
		super(parent);
	}

	@Override
	public String getName()
	{
		if(Machine.merge_mode.isMerge())
		{
			if(merge == null)
			{
				if(isCollisionMode() && parent.isClone())
				{
					return parent.name + "/" + name;
				}
			}
			else
				return merge;
		}
		return name;
	}

	public String getFullName()
	{
		if(Machine.merge_mode.isMerge())
		{
			if(merge != null)
				return parent.name + "/" + merge;
			return parent.name + "/" + name;
		}
		return name;
	}

	public String getOriginalName()
	{
		return name;
	}

	@Override
	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Rom)
		{
			if(((Rom) obj).sha1 != null && this.sha1 != null)
				return ((Rom) obj).sha1.equals(this.sha1);
			if(((Rom) obj).md5 != null && this.md5 != null)
				return ((Rom) obj).md5.equals(this.md5);
			if(((Rom) obj).crc != null && this.crc != null)
				return ((Rom) obj).crc.equals(this.crc) && ((Rom) obj).size == this.size;
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
		if(this.crc != null)
			return this.crc.hashCode();
		return super.hashCode();
	}

	public String hashString()
	{
		if(this.sha1 != null)
			return this.sha1;
		if(this.md5 != null)
			return this.md5;
		if(this.crc != null)
			return this.crc;
		return this.getName();
	}

	public static Map<String, Rom> getRomsByName(List<Rom> roms)
	{
		return roms.stream().collect(Collectors.toMap(Rom::getName, r -> r, (n, r) -> n));
	}

}
