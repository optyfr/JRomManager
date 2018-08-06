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
 * Describe a machine Driver (used for filtering)
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class Driver implements Serializable
{
	/**
	 * global {@link StatusType} of the machine (default to preliminary)
	 */
	private StatusType status = StatusType.preliminary;
	/**
	 * emulation {@link StatusType} of the machine (default to preliminary)
	 */
	private StatusType emulation = StatusType.preliminary;
	/**
	 * cocktail mode {@link StatusType} of the machine  (default to preliminary)
	 */
	private StatusType cocktail = StatusType.preliminary;
	/**
	 * save state as {@link SaveStateType} (default to unsupported)
	 */
	private SaveStateType savestate = SaveStateType.unsupported;

	/**
	 * Enumerate the various status type of a machine driver
	 */
	public enum StatusType
	{
		good,
		imperfect,
		preliminary
	};

	/**
	 * Enumerate the save state types of a machine driver
	 */
	public enum SaveStateType
	{
		supported,
		unsupported
	};

	/**
	 * Constructor is only authorized in package
	 */
	Driver()
	{
	}

	/**
	 * set the global status from a string
	 * @param status the status as a {@link String}, must match a {@link StatusType} value name
	 */
	public void setStatus(final String status)
	{
		this.status = StatusType.valueOf(status);
	}

	/**
	 * set the emulation status from a string
	 * @param status the status as a {@link String}, must match a {@link StatusType} value name
	 */
	public void setEmulation(final String status)
	{
		emulation = StatusType.valueOf(status);
	}

	/**
	 * set the cocktail status from a string
	 * @param status the status as a {@link String}, must match a {@link StatusType} value name
	 */
	public void setCocktail(final String status)
	{
		cocktail = StatusType.valueOf(status);
	}

	/**
	 * set the save state from a string
	 * @param status the save state as a {@link String}, must match a {@link SaveStateType} value name
	 */
	public void setSaveState(final String status)
	{
		savestate = SaveStateType.valueOf(status);
	}

	/**
	 * get the current global status
	 * @return {@link StatusType}
	 */
	public StatusType getStatus()
	{
		return status;
	}

	/**
	 * get the current emulation status
	 * @return {@link StatusType}
	 */
	public StatusType getEmulation()
	{
		return emulation;
	}

	/**
	 * get the current cocktail status
	 * @return {@link StatusType}
	 */
	public StatusType getCocktail()
	{
		return cocktail;
	}

	/**
	 * get the current save state
	 * @return {@link SaveStateType}
	 */
	public SaveStateType getSaveState()
	{
		return savestate;
	}
}
