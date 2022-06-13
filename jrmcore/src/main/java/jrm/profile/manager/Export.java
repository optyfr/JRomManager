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
package jrm.profile.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import jrm.aui.progress.ProgressHandler;
import jrm.misc.Log;
import jrm.profile.Profile;
import jrm.profile.data.SoftwareList;
import jrm.xml.EnhancedXMLStreamWriter;

/**
 * Export a profile into one of the {@link ExportType}
 * @author optyfr
 *
 */
public class Export
{
	/**
	 * The supported export types enum
	 */
	public enum ExportType
	{
		/**
		 * Export into latest Mame format
		 */
		MAME,
		/**
		 * Export into Logiqx datfile format
		 */
		DATAFILE,
		/**
		 * Export Software list(s) using Mame software list format
		 */
		SOFTWARELIST
	}

	/**
	 * Will export a {@code profile} to a {@code file} in the {@code type} format, {@code filtered} (or not), only a {@code selection} SoftwareList (or none), and show a {@code progress} bar
	 * @param profile the {@link Profile} to export
	 * @param file the destination {@link File}
	 * @param type the {@link ExportType} format
	 * @param filtered whether we should use selected filter or not
	 * @param selection if {@link ExportType#SOFTWARELIST} type, will export only the selected {@link SoftwareList}, null to export all software lists in a single file
	 * @param progress optional {@link ProgressHandler} to show export progression
	 */
	public static void export(final Profile profile, final File file, final ExportType type, final boolean filtered, final SoftwareList selection, final ProgressHandler progress)
	{
		EnhancedXMLStreamWriter writer = null;
		try(FileOutputStream fos = new FileOutputStream(file))
		{
			writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(fos, "UTF-8")); //$NON-NLS-1$
			switch(type)
			{
				case MAME:
					profile.getMachineListList().export(writer, progress, true, filtered);
					break;
				case DATAFILE:
					profile.getMachineListList().export(writer, progress, false, filtered);
					break;
				case SOFTWARELIST:
					profile.getMachineListList().getSoftwareListList().export(writer, progress, filtered, selection);
					break;
			}
			writer.close();
		}
		catch(FactoryConfigurationError | XMLStreamException | IOException e)
		{
//			JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			Log.err(e.getMessage(),e);
		}
	}

}
