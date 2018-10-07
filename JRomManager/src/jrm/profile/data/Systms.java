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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.AbstractListModel;

/**
 * ListModel of systems
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class Systms extends AbstractListModel<Systm> implements Serializable, Iterable<Systm>
{
	/**
	 * The internal {@link ArrayList} of {@link Systm}s
	 */
	private final ArrayList<Systm> systems = new ArrayList<>();

	@Override
	public int getSize()
	{
		return systems.size();
	}

	@Override
	public Systm getElementAt(final int index)
	{
		return systems.get(index);
	}

	/**
	 * add a {@link Systm} to the list
	 * @param system the {@link Systm} to add
	 * @return return true if successful
	 */
	public boolean add(final Systm system)
	{
		return systems.add(system);
	}

	@Override
	public Iterator<Systm> iterator()
	{
		return systems.iterator();
	}
}
