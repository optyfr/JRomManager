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
import java.util.stream.Collectors;

import javax.xml.stream.XMLStreamException;

import jrm.misc.ProfileSettingsEnum;
import jrm.xml.EnhancedXMLStreamWriter;
import jrm.xml.SimpleAttribute;
import lombok.Getter;
import lombok.Setter;

/**
 * Rom entity definition
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class Rom extends Entity implements Serializable
{
	private static final String OFFSET_STR = "offset";
	private static final String NAME_STR = "name";
	private static final String ROM_STR = "rom";
	/**
	 * the bios name otherwise null
	 */
	protected @Getter @Setter String bios = null;
	/**
	 * the in memory offset (kept for export)
	 */
	protected @Getter @Setter Long offset = null;
	/**
	 * the in memory load flag (kept for export)
	 */
	protected @Getter @Setter LoadFlag loadflag = null;
	/**
	 * the value to fill according load flag (only software rom, kept for export)
	 */
	protected @Getter @Setter String value = null;
	/**
	 * is this rom is optional?
	 */
	protected @Getter @Setter boolean optional = false;
	/**
	 * the memory region (kept for export)
	 */
	protected @Getter @Setter String region = null;
	/**
	 * the dump date (kept for export)
	 */
	protected @Getter @Setter String date = null;

	/**
	 * Possibles Load Flags definitions
	 * Definitions here are all uppercase because there would be collision with java keywords otherwise... but in fact, this should be lowercase
	 */
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

	/**
	 * The constructor
	 * @param parent a required {@link Anyware} parent
	 */
	public Rom(final Anyware parent)
	{
		super(parent);
	}

	/**
	 * get the rom name, will include the parent name if we are using a merge mode and we are a clone collisioning with its parent
	 */
	@Override
	public String getName()
	{
		if (parent.getProfile().getSettings()!=null && parent.getProfile().getSettings().getMergeMode().isMerge())
		{
			if (merge == null)
			{
				if (isCollisionMode(false) && getParent().isClone())
				{
					return parent.name + "/" + name; //$NON-NLS-1$
				}
			}
			else if (!parent.getProfile().getProperty(ProfileSettingsEnum.ignore_merge_name_roms, Boolean.class)) //$NON-NLS-1$
				return merge;
		}
		return name;
	}

	/**
	 * get the full rom name, will include the parent name if we are using a merge mode, even if there is no collision
	 * @return the full name of the entity
	 */
	public String getFullName()
	{
		if (getParent().profile.getSettings()!=null && getParent().profile.getSettings().getMergeMode().isMerge())
		{
			if (merge != null && !parent.getProfile().getProperty(ProfileSettingsEnum.ignore_merge_name_roms, Boolean.class)) //$NON-NLS-1$
				return parent.name + "/" + merge; //$NON-NLS-1$
			return parent.name + "/" + name; //$NON-NLS-1$
		}
		return name;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof Rom rom)
		{
			if (rom.sha1 != null && sha1 != null)
				return rom.sha1.equals(sha1);
			if (rom.md5 != null && md5 != null)
				return rom.md5.equals(md5);
			if (rom.crc != null && crc != null)
				return rom.crc.equals(crc) && rom.size == size;
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

	/**
	 * get the latest available hash string value for this ROM
	 * @return the hash {@link String} value
	 */
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

	/**
	 * convert a {@link List} of {@link Rom}s to a {@link Map} of {@link Rom} with {@link Rom#getName()} as keys
	 * @param roms the {@link List}&lt;{@link Rom}&gt; to convert
	 * @return a {@link Map}&lt;{@link String}, {@link Rom}&gt;
	 */
	public static Map<String, Rom> getRomsByName(final List<Rom> roms)
	{
		return roms.stream().collect(Collectors.toMap(Rom::getNormalizedName, r -> r, (n, r) -> n));
	}

	/**
	 * Try to find the {@link Rom} status recursively across parents and also in clones (only if we are in merged mode)
	 * @param parent the {@link Anyware} parent 
	 * @param rom the {@link Rom} to test
	 * @return the {@link EntityStatus} found for this disk
	 */
	private EntityStatus findRomStatus(final Anyware parent, final Rom rom)
	{
		for (final Rom r : parent.getRoms())
		{
			if (rom != r && rom.equals(r))
			{
				if(r.ownStatus != EntityStatus.UNKNOWN)
					return r.ownStatus;
				break;
			}
		}
		if (parent.parent == null)
		{
			if (parent.isRomOf() && rom.merge != null)
				return EntityStatus.OK;
			return null;
		}
		// find same rom in parent clone (if any and recursively)
		final var status = findRomStatusMerge(parent, rom);
		if (status != null)
			return status;
		for (final Rom r : parent.getParent().getRoms())
		{
			if (rom.equals(r))
				return r.getStatus();
		}
		if (parent.parent.parent != null)
			return findRomStatus(parent.getParent(), rom);
		return null;
	}

	/**
	 * @param parent
	 * @param rom
	 */
	private EntityStatus findRomStatusMerge(final Anyware parent, final Rom rom)
	{
		if (!getParent().profile.getSettings().getMergeMode().isMerge())
			return null;
		for (final Anyware clone : parent.getParent().clones.values())
		{
			if (clone != parent)
			{
				for (final Rom r : clone.getRoms())
				{
					if (rom.equals(r))
					{
						if(r.ownStatus != EntityStatus.UNKNOWN)
							return r.ownStatus;
						break;
					}
					
				}
			}
		}
		return null;
	}

	@Override
	public EntityStatus getStatus()
	{
		if (name.isEmpty())
			return EntityStatus.OK;
		if (dumpStatus == Status.nodump)
			return EntityStatus.OK;
		if (ownStatus == EntityStatus.UNKNOWN)
		{
			final EntityStatus status = findRomStatus(getParent(), this);
			if (status != null)
				return status;
		}
		return ownStatus;
	}

	/**
	 * Export as dat
	 * @param writer the {@link EnhancedXMLStreamWriter} used to write output file
	 * @param is_mame is it mame (true) or logqix (false) format ?
	 * @throws XMLStreamException
	 */
	public void export(final EnhancedXMLStreamWriter writer, final boolean is_mame) throws XMLStreamException
	{
		if (parent instanceof Software)
		{
			writer.writeElement(ROM_STR, //$NON-NLS-1$
					new SimpleAttribute(NAME_STR, name), //$NON-NLS-1$
					new SimpleAttribute(SIZE_STR, size), //$NON-NLS-1$
					new SimpleAttribute(CRC_STR, crc), //$NON-NLS-1$
					new SimpleAttribute(SHA1_STR, sha1), //$NON-NLS-1$
					new SimpleAttribute(MERGE_STR, merge), //$NON-NLS-1$
					new SimpleAttribute(STATUS_STR, dumpStatus.getXML(is_mame)), //$NON-NLS-1$
					new SimpleAttribute("value", value), //$NON-NLS-1$
					new SimpleAttribute("loadflag", loadflag), //$NON-NLS-1$
					new SimpleAttribute(OFFSET_STR, offset == null ? null : ("0x" + Long.toHexString(offset))) //$NON-NLS-1$ //$NON-NLS-2$
			);
		}
		else if (is_mame)
		{
			writer.writeElement(ROM_STR, //$NON-NLS-1$
					new SimpleAttribute(NAME_STR, name), //$NON-NLS-1$
					new SimpleAttribute("bios", bios), //$NON-NLS-1$
					new SimpleAttribute(SIZE_STR, size), //$NON-NLS-1$
					new SimpleAttribute(CRC_STR, crc), //$NON-NLS-1$
					new SimpleAttribute(SHA1_STR, sha1), //$NON-NLS-1$
					new SimpleAttribute(MERGE_STR, merge), //$NON-NLS-1$
					new SimpleAttribute(STATUS_STR, dumpStatus.getXML(is_mame)), //$NON-NLS-1$
					new SimpleAttribute("optional", optional ? "yes" : null), //$NON-NLS-1$ //$NON-NLS-2$
					new SimpleAttribute("region", region), //$NON-NLS-1$
					new SimpleAttribute(OFFSET_STR, offset == null ? null : ("0x" + Long.toHexString(offset))) //$NON-NLS-1$ //$NON-NLS-2$
			);
		}
		else
		{
			writer.writeElement(ROM_STR, //$NON-NLS-1$
					new SimpleAttribute(NAME_STR, name), //$NON-NLS-1$
					new SimpleAttribute(SIZE_STR, size), //$NON-NLS-1$
					new SimpleAttribute(CRC_STR, crc), //$NON-NLS-1$
					new SimpleAttribute(SHA1_STR, sha1), //$NON-NLS-1$
					new SimpleAttribute(MD5_STR, md5), //$NON-NLS-1$
					new SimpleAttribute(MERGE_STR, merge), //$NON-NLS-1$
					new SimpleAttribute(STATUS_STR, dumpStatus.getXML(is_mame)), //$NON-NLS-1$
					new SimpleAttribute("date", date) //$NON-NLS-1$
			);
		}
	}

}
