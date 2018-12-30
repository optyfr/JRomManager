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

/**
 * All possible hash collision options
 * @author optyfr
 *
 */
public enum HashCollisionOptions
{
	/**
	 * only collisioned entries in clone subfolder
	 */
	SINGLEFILE(Messages.getString("HashCollisionOptions.SingleFile")), //$NON-NLS-1$
	/**
	 * all entries in collisioning clones subfolder
	 */
	SINGLECLONE(Messages.getString("HashCollisionOptions.SingleClone")), //$NON-NLS-1$
	/**
	 * all clones in subfolder as soon there is a collision
	 */
	ALLCLONES(Messages.getString("HashCollisionOptions.AllClones")), //$NON-NLS-1$
	/**
	 * all clones in subfolder as soon there is a collision, with some optimisations
	 */
	HALFDUMB(Messages.getString("HashCollisionOptions.AllClonesHalfDumb")), //$NON-NLS-1$
	/**
	 * all clones in subfolder even if there is no hash collision
	 */
	DUMB(Messages.getString("HashCollisionOptions.AllClonesDumb")), //$NON-NLS-1$
	/**
	 * all clones in subfolder even if there is no hash collision (disk included)
	 */
	DUMBER(Messages.getString("HashCollisionOptions.AllClonesDumber")); //$NON-NLS-1$

	/**
	 * The name of the option
	 */
	private @Getter String desc;

	/**
	 * internal constructor
	 * @param name the name of the option
	 */
	private HashCollisionOptions(String desc)
	{
		this.desc = desc;
	}

}
