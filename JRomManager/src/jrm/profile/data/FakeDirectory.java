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
 * Describe an directory that can eventually be linked to an {@link AnywareBase} set
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class FakeDirectory extends Container implements Serializable
{
	/**
	 * Construct a directory where set is known
	 * @param file the directory {@link File}
	 * @param m the corresponding {@link AnywareBase} set
	 */
	public FakeDirectory(final File file, final File relfile, final AnywareBase m)
	{
		super(Type.FAKE, file, relfile, m);
	}

	/**
	 * Construct a directory with no related set
	 * @param file the directory {@link File}
	 * @param attr the directory attributes
	 */
	public FakeDirectory(final File file, final File relfile, final BasicFileAttributes attr)
	{
		super(Type.FAKE, file, relfile, attr);
	}

}
