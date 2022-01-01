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
package jrm.profile.scan.options;

import java.util.EnumSet;

import jrm.locale.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All possible merge options
 * @author optyfr
 *
 */
public @RequiredArgsConstructor enum MergeOptions implements Descriptor
{
	/**
	 * merge clones and bioses into parent
	 */
	FULLMERGE(Messages.getString("MergeOptions.FullMerge")), //$NON-NLS-1$
	/**
	 * merge clones into parent
	 */
	MERGE(Messages.getString("MergeOptions.Merge")), //$NON-NLS-1$
	/**
	 * no merge (keep individual), and include bios + devices
	 */
	SUPERFULLNOMERGE(Messages.getString("MergeOptions.NoMergeInclBiosAndDevices")), //$NON-NLS-1$
	/**
	 * no merge (keep individual), and include bios but not devices
	 */
	FULLNOMERGE(Messages.getString("MergeOptions.NoMergeInclBios")), //$NON-NLS-1$
	/**
	 * no merge (keep individual), excluding bios and devices
	 */
	NOMERGE(Messages.getString("MergeOptions.NoMerge")), //$NON-NLS-1$
	/**
	 * split all
	 */
	SPLIT(Messages.getString("MergeOptions.Split")); //$NON-NLS-1$

	/**
	 * the name of the option
	 */
	private final 
	/**
	 * internal constructor
	 * @param desc the name of the option
	 */
	@Getter String desc;

	/**
	 * Is is a merge option?
	 * @return true if current option is either {@link #MERGE} or {@link #FULLMERGE}
	 */
	public boolean isMerge()
	{
		return this == MERGE || this == MergeOptions.FULLMERGE;
	}
}
