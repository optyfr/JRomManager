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

	public Disk(final Anyware parent)
	{
		super(parent);
	}

	@Override
	public String getName()
	{
		if(Anyware.merge_mode.isMerge())
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
	public void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if(obj instanceof Disk)
		{
			if(((Disk) obj).sha1 != null && sha1 != null)
				return ((Disk) obj).sha1.equals(sha1);
			if(((Disk) obj).md5 != null && md5 != null)
				return ((Disk) obj).md5.equals(md5);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		if(sha1 != null)
			return sha1.hashCode();
		if(md5 != null)
			return md5.hashCode();
		return super.hashCode();
	}

	public String hashString()
	{
		if(sha1 != null)
			return sha1;
		if(md5 != null)
			return md5;
		return getName();
	}

	public static Map<String, Disk> getDisksByName(final List<Disk> disks)
	{
		return disks.stream().collect(Collectors.toMap(Disk::getName, Function.identity(), (n, r) -> n));
	}

	private EntityStatus findDiskStatus(final Anyware parent, final Disk disk)
	{
		if(parent.parent!=null)	// find same disk in parent clone (if any and recursively)
		{
			for(final Disk d : parent.parent.disks)
			{
				if(disk.equals(d))
					return d.getStatus();
			}
		}
		return null;
	}

	@Override
	public EntityStatus getStatus()
	{
		if(status == Status.nodump)
			return EntityStatus.OK;
		if(own_status==EntityStatus.UNKNOWN)
		{
			final EntityStatus status = findDiskStatus(parent, this);
			if(status != null)
				return status;
		}
		return own_status;
	}


	public void export(final EnhancedXMLStreamWriter writer, final boolean is_mame) throws XMLStreamException, IOException
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
