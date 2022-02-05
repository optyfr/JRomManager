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
package jrm.aui.progress;

import java.io.InputStream;

/**
 * The Interface ProgressHandler.
 *
 * @author optyfr
 */
public interface ProgressHandler
{
	public enum Option
	{
		LAZY;
	}
	
	public void setOptions(Option first, Option... rest);
	/**
	 * Sets the infos.
	 *
	 * @param threadCnt the thread cnt
	 * @param multipleSubInfos the multiple sub infos
	 */
	public void setInfos(int threadCnt, Boolean multipleSubInfos);
	
	/**
	 * Clear infos.
	 */
	public void clearInfos();
		
	/**
	 * Sets the progress.
	 *
	 * @param msg the new progress
	 */
	public default void setProgress(String msg)
	{
		setProgress(msg, null, null, null);
	}

	/**
	 * Sets the progress.
	 *
	 * @param msg the msg
	 * @param val the val
	 */
	public default void setProgress(String msg, Integer val)
	{
		setProgress(msg, val, null, null);
	}

	/**
	 * Sets the progress.
	 *
	 * @param msg the msg
	 * @param val the val
	 * @param max the max
	 */
	public default void setProgress(String msg, Integer val, Integer max)
	{
		setProgress(msg, val, max, null);
	}

	/**
	 * Sets the progress.
	 *
	 * @param msg the msg
	 * @param val the val
	 * @param max the max
	 * @param submsg the submsg
	 */
	public void setProgress(String msg, Integer val, Integer max, String submsg);

	/**
	 * Sets the progress 2.
	 *
	 * @param msg the msg
	 * @param val the val
	 */
	public default void setProgress2(String msg, Integer val)
	{
		setProgress2(msg, val, null);
	}

	/**
	 * Sets the progress 2.
	 *
	 * @param msg the msg
	 * @param val the val
	 * @param max the max
	 */
	public void setProgress2(String msg, Integer val, Integer max);

	/**
	 * Sets the progress 3.
	 *
	 * @param msg the msg
	 * @param val the val
	 */
	public default void setProgress3(String msg, Integer val)
	{
		setProgress3(msg, val, null);
	}

	/**
	 * Sets the progress 3.
	 *
	 * @param msg the msg
	 * @param val the val
	 * @param max the max
	 */
	public void setProgress3(String msg, Integer val, Integer max);
	
	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public int getCurrent();

	/**
	 * Gets the value 2.
	 *
	 * @return the value 2
	 */
	public int getCurrent2();

	/**
	 * Gets the value 3.
	 *
	 * @return the value 3
	 */
	public int getCurrent3();
	
	/**
	 * Checks if is cancel.
	 *
	 * @return true, if is cancel
	 */
	public boolean isCancel();

	/**
	 * Cancel.
	 */
	public void doCancel();
	
	/**
	 * Set Can Cancel.
	 * @param canCancel set if we can cancel or not
	 */
	public void canCancel(boolean canCancel);
	
	/**
	 * Can Cancel?
	 * @return true if we can cancel
	 */
	public boolean canCancel();
	
	/**
	 * Gets the input stream.
	 *
	 * @param in the in
	 * @param len the len
	 * @return the input stream
	 */
	public InputStream getInputStream(InputStream in, Integer len);
	
	/**
	 * Destroy or hide the progress
	 */
	public void close();
	
	/**
	 * add an error to show when closing progress
	 * @param error
	 */
	public void addError(String error);
}
