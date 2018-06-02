package jrm.profile.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import jrm.profile.Export.EnhancedXMLStreamWriter;
import jrm.profile.Export.SimpleAttribute;
import jrm.profile.Profile;

@SuppressWarnings("serial")
public class Rom extends Entity implements Serializable
{
	public String bios = null;
	public Integer offset = null;
	public LoadFlag loadflag = null;
	public String value = null;
	public boolean optional = false;
	public String region = null;
	public String date = null;

	public enum LoadFlag implements Serializable
	{
		LOAD16_BYTE, LOAD16_WORD, LOAD16_WORD_SWAP, LOAD32_BYTE, LOAD32_WORD, LOAD32_WORD_SWAP, LOAD32_DWORD, LOAD64_WORD, LOAD64_WORD_SWAP, RELOAD, FILL, CONTINUE, RELOAD_PLAIN, IGNORE;

		@Override
		public String toString()
		{
			return name().toLowerCase();
		}

		public static LoadFlag getEnum(final String value)
		{
			for (final LoadFlag v : LoadFlag.values())
				if (v.name().equalsIgnoreCase(value))
					return v;
			throw new IllegalArgumentException();
		}
	}

	public Rom(final Anyware parent)
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
				if (isCollisionMode(false) && getParent().isClone())
				{
					return parent.name + "/" + name; //$NON-NLS-1$
				}
			}
			else if (!Profile.curr_profile.getProperty("ignore_merge_name_roms", false))
				return merge;
		}
		return name;
	}

	public String getFullName()
	{
		if (Anyware.merge_mode.isMerge())
		{
			if (merge != null && !Profile.curr_profile.getProperty("ignore_merge_name_roms", false))
				return parent.name + "/" + merge; //$NON-NLS-1$
			return parent.name + "/" + name; //$NON-NLS-1$
		}
		return name;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof Rom)
		{
			if (((Rom) obj).sha1 != null && sha1 != null)
				return ((Rom) obj).sha1.equals(sha1);
			if (((Rom) obj).md5 != null && md5 != null)
				return ((Rom) obj).md5.equals(md5);
			if (((Rom) obj).crc != null && crc != null)
				return ((Rom) obj).crc.equals(crc) && ((Rom) obj).size == size;
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
		if (crc != null)
			return crc.hashCode();
		return super.hashCode();
	}

	public String hashString()
	{
		if (sha1 != null)
			return sha1;
		if (md5 != null)
			return md5;
		if (crc != null)
			return crc;
		return getName();
	}

	public static Map<String, Rom> getRomsByName(final List<Rom> roms)
	{
		return roms.stream().collect(Collectors.toMap(Rom::getName, r -> r, (n, r) -> n));
	}

	private EntityStatus findRomStatus(final Anyware parent, final Rom rom)
	{
		for (final Rom r : parent.roms)
			if (rom != r && rom.equals(r) && r.own_status != EntityStatus.UNKNOWN)
				return r.own_status;
		if (parent.parent != null) // find same rom in parent clone (if any and recursively)
		{
			if (Anyware.merge_mode.isMerge())
			{
				for (final Anyware clone : parent.getParent().clones.values())
					if (clone != parent)
						for (final Rom r : clone.roms)
							if (rom.equals(r) && r.own_status != EntityStatus.UNKNOWN)
								return r.own_status;
			}
			for (final Rom r : parent.getParent().roms)
			{
				if (rom.equals(r))
					return r.getStatus();
			}
			if (parent.parent.parent != null)
				return findRomStatus(parent.getParent(), rom);
		}
		else if (parent.isRomOf() && merge != null)
			return EntityStatus.OK;
		return null;
	}

	@Override
	public EntityStatus getStatus()
	{
		if (name.isEmpty())
			return EntityStatus.OK;
		if (status == Status.nodump)
			return EntityStatus.OK;
		if (own_status == EntityStatus.UNKNOWN)
		{
			final EntityStatus status = findRomStatus(getParent(), this);
			if (status != null)
				return status;
		}
		return own_status;
	}

	public void export(final EnhancedXMLStreamWriter writer, final boolean is_mame) throws XMLStreamException, IOException
	{
		if (parent instanceof Software)
		{
			writer.writeElement("rom", //$NON-NLS-1$
					new SimpleAttribute("name", name), //$NON-NLS-1$
					new SimpleAttribute("size", size), //$NON-NLS-1$
					new SimpleAttribute("crc", crc), //$NON-NLS-1$
					new SimpleAttribute("sha1", sha1), //$NON-NLS-1$
					new SimpleAttribute("merge", merge), //$NON-NLS-1$
					new SimpleAttribute("status", status.getXML(is_mame)), //$NON-NLS-1$
					new SimpleAttribute("value", value), //$NON-NLS-1$
					new SimpleAttribute("loadflag", loadflag), //$NON-NLS-1$
					new SimpleAttribute("offset", offset == null ? null : ("0x" + Integer.toHexString(offset))) //$NON-NLS-1$ //$NON-NLS-2$
			);
		}
		else if (is_mame)
		{
			writer.writeElement("rom", //$NON-NLS-1$
					new SimpleAttribute("name", name), //$NON-NLS-1$
					new SimpleAttribute("bios", bios), //$NON-NLS-1$
					new SimpleAttribute("size", size), //$NON-NLS-1$
					new SimpleAttribute("crc", crc), //$NON-NLS-1$
					new SimpleAttribute("sha1", sha1), //$NON-NLS-1$
					new SimpleAttribute("merge", merge), //$NON-NLS-1$
					new SimpleAttribute("status", status.getXML(is_mame)), //$NON-NLS-1$
					new SimpleAttribute("optional", optional ? "yes" : null), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleAttribute("region", region), //$NON-NLS-1$
					new SimpleAttribute("offset", offset == null ? null : ("0x" + Integer.toHexString(offset))) //$NON-NLS-1$ //$NON-NLS-2$
			);
		}
		else
		{
			writer.writeElement("rom", //$NON-NLS-1$
					new SimpleAttribute("name", name), //$NON-NLS-1$
					new SimpleAttribute("size", size), //$NON-NLS-1$
					new SimpleAttribute("crc", crc), //$NON-NLS-1$
					new SimpleAttribute("sha1", sha1), //$NON-NLS-1$
					new SimpleAttribute("md5", md5), //$NON-NLS-1$
					new SimpleAttribute("merge", merge), //$NON-NLS-1$
					new SimpleAttribute("status", status.getXML(is_mame)), //$NON-NLS-1$
					new SimpleAttribute("date", date) //$NON-NLS-1$
			);
		}
	}

}
