package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import jrm.profile.Export.EnhancedXMLStreamWriter;
import jrm.profile.Export.SimpleAttribute;

@SuppressWarnings("serial")
public class Disk extends Entity implements Serializable
{
	public boolean writeable = false;
	public Integer index = null;
	public boolean optional = false;
	public String region = null;
	
	public Disk(Anyware parent)
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
					return parent.name + "/" + name + ".chd";
				}
			}
			else
				return merge + ".chd";
		}
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
			for(Disk d : parent.parent.disks)
			{
				if(disk.equals(d))
					return d.getStatus();
			}
		}
		return null;
	}
	
	public EntityStatus getStatus()
	{
		if(status == Status.nodump)
			return EntityStatus.OK;
		if(own_status==EntityStatus.UNKNOWN)
		{
			EntityStatus status = findDiskStatus(parent, this);
			if(status != null)
				return status;
		}
		return own_status;
	}

	
	public void export(EnhancedXMLStreamWriter writer, boolean is_mame) throws XMLStreamException, IOException
	{
		if(parent instanceof Software)
		{
			writer.writeElement("disk",
				new SimpleAttribute("name",name),
				new SimpleAttribute("sha1",sha1),
				new SimpleAttribute("status",status.getXML(is_mame)),
				new SimpleAttribute("writeable",writeable?"yes":null)
			);
		}
		else if(is_mame)
		{
			writer.writeElement("disk",
				new SimpleAttribute("name",name),
				new SimpleAttribute("sha1",sha1),
				new SimpleAttribute("merge",merge),
				new SimpleAttribute("status",status.getXML(is_mame)),
				new SimpleAttribute("optional",optional),
				new SimpleAttribute("region",region),
				new SimpleAttribute("writable",writeable?"yes":null),
				new SimpleAttribute("index",index)
			);
		}
		else
		{
			writer.writeElement("disk",
				new SimpleAttribute("name",name),
				new SimpleAttribute("sha1",sha1),
				new SimpleAttribute("md5",md5),
				new SimpleAttribute("merge",merge),
				new SimpleAttribute("status",status.getXML(is_mame))
			);
		}
	}
}
