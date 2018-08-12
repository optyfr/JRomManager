/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import jrm.profile.Profile;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;

/**
 * Describe a disk entity 
 * @author optyfr
 */
@SuppressWarnings("serial")
public class Disk extends Entity implements Serializable
{
	/**
	 * is the disk writable? default to false
	 */
	public boolean writeable = false;
	/**
	 * what's the disk index? default to null
	 */
	public Integer index = null;
	/**
	 * Is the disk optional? default to false
	 */
	public boolean optional = false;
	/**
	 * What's the disk region?
	 */
	public String region = null;

	/**
	 * Constructor 
	 * @param parent the {@link Anyware} parent containing the disk
	 */
	public Disk(final Anyware parent)
	{
		super(parent);
	}

	@Override
	public String getName()
	{
		if (Anyware.merge_mode.isMerge())
		{
			if (merge == null)
			{
				if (isCollisionMode(true) && getParent().isClone())
				{
					return parent.name + "/" + name + ".chd"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			else if (!Profile.curr_profile.getProperty("ignore_merge_name_disks", false))
				return merge + ".chd"; //$NON-NLS-1$
		}
		return name + ".chd"; //$NON-NLS-1$
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof Disk)
		{
			if (((Disk) obj).sha1 != null && sha1 != null)
				return ((Disk) obj).sha1.equals(sha1);
			if (((Disk) obj).md5 != null && md5 != null)
				return ((Disk) obj).md5.equals(md5);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		if (sha1 != null)
			return sha1.hashCode();
		if (md5 != null)
			return md5.hashCode();
		return super.hashCode();
	}

	/**
	 * the disk hash value ({@link Entity#sha1} or {@link Entity#md5} ) as a {@link String}, or {@link #getName()} if no hash available
	 * @return the hash value as a {@link String} 
	 */
	public String hashString()
	{
		if (sha1 != null)
			return sha1;
		if (md5 != null)
			return md5;
		return getName();
	}

	/**
	 * convert a {@link List} of {@link Disk}s to a {@link Map} of {@link Disk} with {@link Disk#getName()} as keys
	 * @param disks the {@link List}&lt;{@link Disk}&gt; to convert
	 * @return a {@link Map}&lt;{@link String}, {@link Disk}&gt;
	 */
	public static Map<String, Disk> getDisksByName(final List<Disk> disks)
	{
		return disks.stream().collect(Collectors.toMap(Disk::getNormalizedName, Function.identity(), (n, r) -> n));
	}

	/**
	 * Try to find the {@link Disk} status recursively across parents and also in clones (only if we are in merged mode)
	 * @param parent the {@link Anyware} parent 
	 * @param disk the {@link Disk} to test
	 * @return the {@link EntityStatus} found for this disk
	 */
	private static EntityStatus findDiskStatus(final Anyware parent, final Disk disk)
	{
		if (parent.parent != null) // find same disk in parent clone (if any and recursively)
		{
			if (Anyware.merge_mode.isMerge())
			{
				for (final Anyware clone : parent.getParent().clones.values())
				{
					if (clone != parent)
						for (final Disk d : clone.disks)
						{
							if (disk.equals(d) && d.own_status != EntityStatus.UNKNOWN)
								return d.own_status;
						}
				}
			}
			for (final Disk d : parent.getParent().disks)
			{
				if (disk.equals(d))
					return d.getStatus();
			}
			if (parent.parent.parent != null)
				return findDiskStatus(parent.getParent(), disk);
		}
		else if (parent.isRomOf() && disk.merge != null)
			return EntityStatus.OK;
		return null;
	}

	@Override
	public EntityStatus getStatus()
	{
		if (status == Status.nodump)
			return EntityStatus.OK;
		if (own_status == EntityStatus.UNKNOWN)
		{
			final EntityStatus status = findDiskStatus(getParent(), this);
			if (status != null)
				return status;
		}
		return own_status;
	}

	/**
	 * Export as dat
	 * @param writer the {@link EnhancedXMLStreamWriter} used to write output file
	 * @param is_mame is it mame (true) or logqix (false) format ?
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void export(final EnhancedXMLStreamWriter writer, final boolean is_mame) throws XMLStreamException, IOException
	{
		if (parent instanceof Software)
		{
			writer.writeElement("disk", //$NON-NLS-1$
					new SimpleAttribute("name", name), //$NON-NLS-1$
					new SimpleAttribute("sha1", sha1), //$NON-NLS-1$
					new SimpleAttribute("status", status.getXML(is_mame)), //$NON-NLS-1$
					new SimpleAttribute("writeable", writeable ? "yes" : null) //$NON-NLS-1$ //$NON-NLS-2$
			);
		}
		else if (is_mame)
		{
			writer.writeElement("disk", //$NON-NLS-1$
					new SimpleAttribute("name", name), //$NON-NLS-1$
					new SimpleAttribute("sha1", sha1), //$NON-NLS-1$
					new SimpleAttribute("merge", merge), //$NON-NLS-1$
					new SimpleAttribute("status", status.getXML(is_mame)), //$NON-NLS-1$
					new SimpleAttribute("optional", optional), //$NON-NLS-1$
					new SimpleAttribute("region", region), //$NON-NLS-1$
					new SimpleAttribute("writable", writeable ? "yes" : null), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleAttribute("index", index) //$NON-NLS-1$
			);
		}
		else
		{
			writer.writeElement("disk", //$NON-NLS-1$
					new SimpleAttribute("name", name), //$NON-NLS-1$
					new SimpleAttribute("sha1", sha1), //$NON-NLS-1$
					new SimpleAttribute("md5", md5), //$NON-NLS-1$
					new SimpleAttribute("merge", merge), //$NON-NLS-1$
					new SimpleAttribute("status", status.getXML(is_mame)) //$NON-NLS-1$
			);
		}
	}
}
