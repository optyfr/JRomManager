package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import jrm.profile.Profile;
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
				if(isCollisionMode(true) && getParent().isClone())
				{
					return parent.name + "/" + name + ".chd"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else if(!Profile.curr_profile.getProperty("ignore_merge_name_disks", false))
				return merge + ".chd"; //$NON-NLS-1$
		}
		return name + ".chd"; //$NON-NLS-1$
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
			for(final Disk d : parent.getParent().disks)
			{
				if(disk.equals(d))
					return d.getStatus();
			}
			if(parent.parent.parent != null)
				return findDiskStatus(parent.getParent(), disk);
		}
		else if(parent.isRomOf() && merge != null)
			return EntityStatus.OK;
		return null;
	}

	@Override
	public EntityStatus getStatus()
	{
		if(status == Status.nodump)
			return EntityStatus.OK;
		if(own_status==EntityStatus.UNKNOWN)
		{
			final EntityStatus status = findDiskStatus(getParent(), this);
			if(status != null)
				return status;
		}
		return own_status;
	}


	public void export(final EnhancedXMLStreamWriter writer, final boolean is_mame) throws XMLStreamException, IOException
	{
		if(parent instanceof Software)
		{
			writer.writeElement("disk", //$NON-NLS-1$
					new SimpleAttribute("name",name), //$NON-NLS-1$
					new SimpleAttribute("sha1",sha1), //$NON-NLS-1$
					new SimpleAttribute("status",status.getXML(is_mame)), //$NON-NLS-1$
					new SimpleAttribute("writeable",writeable?"yes":null) //$NON-NLS-1$ //$NON-NLS-2$
					);
		}
		else if(is_mame)
		{
			writer.writeElement("disk", //$NON-NLS-1$
					new SimpleAttribute("name",name), //$NON-NLS-1$
					new SimpleAttribute("sha1",sha1), //$NON-NLS-1$
					new SimpleAttribute("merge",merge), //$NON-NLS-1$
					new SimpleAttribute("status",status.getXML(is_mame)), //$NON-NLS-1$
					new SimpleAttribute("optional",optional), //$NON-NLS-1$
					new SimpleAttribute("region",region), //$NON-NLS-1$
					new SimpleAttribute("writable",writeable?"yes":null), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleAttribute("index",index) //$NON-NLS-1$
					);
		}
		else
		{
			writer.writeElement("disk", //$NON-NLS-1$
					new SimpleAttribute("name",name), //$NON-NLS-1$
					new SimpleAttribute("sha1",sha1), //$NON-NLS-1$
					new SimpleAttribute("md5",md5), //$NON-NLS-1$
					new SimpleAttribute("merge",merge), //$NON-NLS-1$
					new SimpleAttribute("status",status.getXML(is_mame)) //$NON-NLS-1$
					);
		}
	}
}
