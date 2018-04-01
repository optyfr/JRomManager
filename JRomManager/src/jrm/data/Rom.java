package jrm.data;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jrm.profiler.scan.MergeOptions;

@SuppressWarnings("serial")
public class Rom extends Entity implements Serializable
{
	public long size = 0;
	public String crc = null;
	public String sha1 = null;
	public String md5 = null;
	public String merge = null;
	public String bios = null;
	public String status = "";
	
	public Rom(Machine parent)
	{
		super(parent);
	}

	@Override
	public String getName()
	{
		if(merge!=null && EnumSet.of(MergeOptions.MERGE,MergeOptions.FULLMERGE).contains(Machine.merge_mode))
			return merge;
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
			if(((Rom)obj).sha1!=null && this.sha1 !=null)
				return ((Rom)obj).sha1.equals(this.sha1);
			if(((Rom)obj).md5!=null && this.md5 !=null)
				return ((Rom)obj).md5.equals(this.md5);
			if(((Rom)obj).crc!=null && this.crc !=null)
				return ((Rom)obj).crc.equals(this.crc) && ((Rom)obj).size==this.size;
		}
		return super.equals(obj);
	}

	public static Map<String,Rom> getRomsByName(List<Rom> roms)
	{
		return roms.stream().collect(Collectors.toMap(Rom::getName, r -> r, (n, r) -> n));
	}
}
