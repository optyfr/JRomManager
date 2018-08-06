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
package jrm.compressors;

import jrm.Messages;

/**
 * Zip supported levels of compression
 * @author optyfr
 *
 */
public enum ZipOptions
{
	STORE(Messages.getString("ZipOptions.STORE"), 0), //$NON-NLS-1$
	FASTEST(Messages.getString("ZipOptions.FASTEST"), 1), //$NON-NLS-1$
	FAST(Messages.getString("ZipOptions.FAST"), 3), //$NON-NLS-1$
	NORMAL(Messages.getString("ZipOptions.NORMAL"), 5), //$NON-NLS-1$
	MAXIMUM(Messages.getString("ZipOptions.MAXIMUM"), 7), //$NON-NLS-1$
	ULTRA(Messages.getString("ZipOptions.ULTRA"), 9); //$NON-NLS-1$

	private String desc;
	private int level;

	private ZipOptions(final String desc, final int level)
	{
		this.desc = desc;
		this.level = level;
	}

	public String getName()
	{
		return desc;
	}

	public int getLevel()
	{
		return level;
	}
}
