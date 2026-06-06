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

import jrm.locale.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * All possible merge options governing how parent/clone relations, BIOS systems,
 * and device resources are grouped together in file containers during scanning and rebuilding.
 * 
 * @author optyfr
 * @since 1.0
 */
public @RequiredArgsConstructor enum MergeOptions implements Descriptor
{
	/**
	 * Merge clones and BIOS roms into the parent romset's archive container.
	 */
	FULLMERGE(Messages.getString("MergeOptions.FullMerge")), //$NON-NLS-1$
	/**
	 * Merge clone roms into parent romset's container, but keep BIOS files separated.
	 */
	MERGE(Messages.getString("MergeOptions.Merge")), //$NON-NLS-1$
	/**
	 * No merge (keep romsets separate), and include BIOS + device files.
	 */
	SUPERFULLNOMERGE(Messages.getString("MergeOptions.NoMergeInclBiosAndDevices")), //$NON-NLS-1$
	/**
	 * No merge (keep romsets separate), and include BIOS files but not device resources.
	 */
	FULLNOMERGE(Messages.getString("MergeOptions.NoMergeInclBios")), //$NON-NLS-1$
	/**
	 * No merge (keep romsets separate), excluding BIOS and device files entirely.
	 */
	NOMERGE(Messages.getString("MergeOptions.NoMerge")), //$NON-NLS-1$
	/**
	 * Split all files (each ROM remains stored in its respective target clone or parent container specifically).
	 */
	SPLIT(Messages.getString("MergeOptions.Split")); //$NON-NLS-1$

	/**
	 * Localized name and description of the merge option.
	 * 
	 * @return a {@link String} with the localized option description.
	 */
	private final @Getter String desc;

	/**
	 * Determines whether the current option indicates a merged storage style.
	 * 
	 * @return {@code true} if current option is either {@link #MERGE} or {@link #FULLMERGE}, otherwise {@code false}.
	 */
	public boolean isMerge()
	{
		return this == MERGE || this == MergeOptions.FULLMERGE;
	}
}
