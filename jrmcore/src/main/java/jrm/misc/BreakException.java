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
package jrm.misc;

/**
 * The only way to break out from a lambda loop in Java 8 is throwing a RuntimeException
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class BreakException extends RuntimeException
{

	public BreakException()
	{
	}

	public BreakException(final String message)
	{
		super(message);
	}

	public BreakException(final Throwable cause)
	{
		super(cause);
	}

	public BreakException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public BreakException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
