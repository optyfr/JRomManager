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
import java.util.Set;

import jrm.compressors.zipfs.ZipFileSystemProvider;
import jrm.locale.Messages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The supported format options
 * @author optyfr
 *
 */
public @RequiredArgsConstructor enum FormatOptions
{
	/**
	 * Zip (internally handled via {@link ZipFileSystemProvider})
	 */
	ZIP(Messages.getString("FormatOptions.Zip"), Ext.ZIP), //$NON-NLS-1$
	/**
	 * Zip (either handled by SevenzipJBinding or by an external program)
	 */
	ZIPE(Messages.getString("FormatOptions.ZipExternal"), Ext.ZIP), //$NON-NLS-1$
	/**
	 * SevenZip (either handled by SevenzipJBinding or by an external program)
	 */
	SEVENZIP(Messages.getString("FormatOptions.SevenZip"), Ext.SEVENZIP), //$NON-NLS-1$
	/**
	 * Zip (torrentzipped by jtrrntzip)
	 */
	TZIP(Messages.getString("FormatOptions.TorrentZip"), Ext.ZIP), //$NON-NLS-1$
	/**
	 * Standard folder
	 */
	DIR(Messages.getString("FormatOptions.Directories"), Ext.DIR), //$NON-NLS-1$
	/**
	 * Single file (fake folder)
	 */
	FAKE(Messages.getString("FormatOptions.SingleFile"), Ext.FAKE); //$NON-NLS-1$


	/**
	 * Supported file container extensions
	 */
	public @RequiredArgsConstructor enum Ext
	{
		/**
		 * folder (no extension)
		 */
		DIR(""), //$NON-NLS-1$
		/**
		 * .zip for zip format
		 */
		ZIP(".zip"), //$NON-NLS-1$
		/**
		 * .7z for sevenzip format
		 */
		SEVENZIP(".7z"), //$NON-NLS-1$
		/**
		 * fake folder (fake extension)
		 */
		FAKE(".$$$"); //$NON-NLS-1$

		private final String value;

		@Override
		public String toString()
		{
			return value;
		}

		/**
		 * get all extensions except the current one (and {@link #DIR})
		 * @return an {@link EnumSet} of {@link Ext}
		 */
		public Set<Ext> allExcept()
		{
			return EnumSet.complementOf(EnumSet.of(this, DIR));
		}

		/**
		 * is it a directory
		 * @return true if it's a directory
		 */
		public boolean isDir()
		{
			return this == DIR;
		}
	}

	/**
	 * Format description 
	 */
	private final @Getter String desc;
	/**
	 * Format extension
	 */
	private final @Getter Ext ext;

	/**
	 * get all formats except current one (and {@link #DIR})
	 * @return an {@link EnumSet} of {@link FormatOptions}
	 */
	public Set<FormatOptions> allExcept()
	{
		return EnumSet.complementOf(EnumSet.of(this, DIR));
	}
}
