/*
 * Copyright (C) 2018 optyfr
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.misc;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import lombok.experimental.UtilityClass;

/**
 * Utility class used to locate executable commands in the system path.
 * <p>
 * This class uses standard platform-specific search tools: {@code where.exe} on
 * Windows and {@code which} on Unix-like operating systems.
 * </p>
 * 
 * @author optyfr
 */
public @UtilityClass class FindCmd {
    /**
     * Searches the system path for the specified command. If the command is not
     * found or if any error occurs, the specified default value is returned.
     *
     * @param cmd the command name to locate (without path or extension)
     * @param def the default fallback value to return if the command cannot be
     *            located
     * @return the full absolute path to the command if successful, or {@code def}
     *         if not found or on failure
     */
    public static String findCmd(final String cmd, final String def) {
        ProcessBuilder pb;
        if (OSValidator.isWindows())
            pb = new ProcessBuilder("where.exe", cmd); //$NON-NLS-1$
        else
            pb = new ProcessBuilder("which", cmd); //$NON-NLS-1$
        try {
            pb.redirectError();
            final var process = pb.start();
            final String output = IOUtils.toString(process.getInputStream(), (Charset) null).trim();
            if (process.waitFor() == 0)
                return output;
        } catch (IOException exp) {
            Log.err(exp.getMessage(), exp);
        } catch (InterruptedException exp) {
            Log.err(exp.getMessage(), exp);
            Thread.currentThread().interrupt();
        }
        return def;
    }

    /**
     * Searches the system path for the specified command. If not found, the
     * original command name is returned.
     *
     * @param cmd the command name to locate (without path or extension)
     * @return the full absolute path to the command if successful, or {@code cmd}
     *         if not found
     */
    public static String findCmd(final String cmd) {
        return findCmd(cmd, cmd);
    }

    /**
     * Locates the 7z (7-Zip) command utility used for ZIP compression.
     *
     * @return the full absolute path to {@code 7z} if successful, otherwise
     *         {@code "7z"}
     */
    public static String findZip() {
        return findCmd("7z"); //$NON-NLS-1$
    }

    /**
     * Locates the {@code torrentzip} or {@code trrntzip} command utility.
     *
     * @return the full absolute path to {@code trrntzip} if successful, otherwise
     *         {@code "trrntzip"}
     */
    public static String findTZip() {
        return findCmd("trrntzip"); //$NON-NLS-1$
    }

    /**
     * Locates the standard 7-Zip executable command utility.
     *
     * @return the full absolute path to {@code 7z} if successful, otherwise
     *         {@code "7z"}
     */
    public static String find7z() {
        return findCmd("7z"); //$NON-NLS-1$
    }

    /**
     * Locates the MAME executable command utility by scanning known names in order
     * of preference: {@code mame}, {@code mame64}, {@code xmame}, {@code advmame}.
     *
     * @return the full absolute path to the located MAME executable, or
     *         {@code null} if none are found
     */
    public static String findMame() {
        for (String cmd : new String[] { "mame", "mame64", "xmame", "advmame" })
            if ((cmd = findCmd(cmd, null)) != null)
                return cmd;
        return null;
    }
}
