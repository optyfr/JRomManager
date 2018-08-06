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

/**
 * Standard system
 * @author optyfr
 */
@SuppressWarnings("serial")
public class SystmStandard implements Systm, Serializable
{
	/**
	 * the static STANDARD object
	 */
	public final static SystmStandard STANDARD = new SystmStandard();

	@Override
	public Type getType()
	{
		return Type.STANDARD;
	}

	@Override
	public Systm getSystem()
	{
		return SystmStandard.STANDARD;
	}

	@Override
	public String toString()
	{
		return "["+getType()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getName()
	{
		return "standard"; //$NON-NLS-1$
	}
}
