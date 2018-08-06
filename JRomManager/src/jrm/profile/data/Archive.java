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

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Describe an archive file that can eventually be linked to an {@link AnywareBase} set
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class Archive extends Container implements Serializable
{
	/**
	 * Construct an archive where set is known
	 * @param file the archive {@link File}
	 * @param m the corresponding {@link AnywareBase} set
	 */
	public Archive(final File file, final AnywareBase m)
	{
		super(Container.getType(file), file, m);
	}

	/**
	 * Construct an archive file with no related set
	 * @param file the archive {@link File}
	 * @param attr the file attributes
	 */
	public Archive(final File file, final BasicFileAttributes attr)
	{
		super(Container.getType(file), file, attr);
	}

}
