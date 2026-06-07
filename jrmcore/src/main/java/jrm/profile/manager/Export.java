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
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import jrm.aui.progress.ProgressHandler;
import jrm.misc.Log;
import jrm.profile.Profile;
import jrm.profile.data.ExportMode;
import jrm.profile.data.SoftwareList;
import jrm.xml.EnhancedXMLStreamWriter;

/**
 * Utility class for exporting profiles into various external formats. Supports
 * exporting to standard MAME XML format, standard Datafile/Logiqx XML format,
 * or MAME Software List formats.
 * 
 * @author optyfr
 */
public final class Export {
    /**
     * Default private constructor to prevent instantiation of this utility class.
     */
    private Export() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * The supported export target formats.
     */
    public enum ExportType {
        /**
         * Export into the latest standard MAME XML database format.
         */
        MAME,
        /**
         * Export into Logiqx/Datafile XML format.
         */
        DATAFILE,
        /**
         * Export Software List database files using MAME software list layout.
         */
        SOFTWARELIST
    }

    /**
     * Exports a profile to a physical file on disk in the specified format,
     * applying any chosen filters and configurations.
     * 
     * @param profile   the {@link Profile} metadata containing romset structures to
     *                  export
     * @param file      the destination {@link File} on disk
     * @param type      the target format defined in {@link ExportType}
     * @param modes     the set of active filtering and exporting options defined in
     *                  {@link ExportMode}
     * @param selection when exporting in {@link ExportType#SOFTWARELIST} mode,
     *                  specifies a single {@link SoftwareList} to export, or
     *                  {@code null} to export all of them into a single file
     * @param progress  an optional {@link ProgressHandler} to monitor and render
     *                  export progress
     */
    public static void export(final Profile profile, final File file, final ExportType type, final Set<ExportMode> modes, final SoftwareList selection,
            final ProgressHandler progress) {
        EnhancedXMLStreamWriter writer = null;
        try (FileOutputStream fos = new FileOutputStream(file)) {
            writer = new EnhancedXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(fos, "UTF-8")); //$NON-NLS-1$
            switch (type) {
                case MAME:
                    profile.getMachineListList().export(writer, progress, true, modes);
                    break;
                case DATAFILE:
                    profile.getMachineListList().export(writer, progress, false, modes);
                    break;
                case SOFTWARELIST:
                    profile.getMachineListList().getSoftwareListList().export(writer, progress, modes, selection);
                    break;
            }
            writer.close();
        } catch (FactoryConfigurationError | XMLStreamException | IOException e) {
//			JOptionPane.showMessageDialog(null, e, "Exception", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
            Log.err(e.getMessage(), e);
        }
    }

}
