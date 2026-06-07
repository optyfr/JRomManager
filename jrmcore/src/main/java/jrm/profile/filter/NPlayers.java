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
package jrm.profile.filter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jrm.locale.Messages;
import lombok.Getter;

/**
 * Manages the parsing, storage, and retrieval of player count configurations
 * from an <code>nplayers.ini</code> metadata file.
 * <p>
 * This class maps games to their corresponding multiplayer mode tags (e.g., "2
 * Players", "4 Players") and implements {@link jrm.profile.filter.IniProcessor}
 * to filter and consume the file entries.
 * </p>
 * 
 * @author optyfr
 * @since 1.0
 */
public final class NPlayers implements Iterable<NPlayer>, IniProcessor {
    /**
     * The sorted list of parsed multiplayer modes.
     * 
     * @return the list containing all initialized {@link NPlayer} instances
     */
    private final @Getter List<NPlayer> listNPlayers = new ArrayList<>();

    /**
     * The source configuration {@link File} from which multiplayer info is read.
     */
    public final File file;

    /**
     * Constructs a new {@code NPlayers} manager by parsing the provided
     * {@code nplayers.ini} file.
     * 
     * @param file the configuration file containing multiplayer assignments
     * @throws IOException if the file cannot be read, or if no valid player data is
     *                     extracted
     */
    private NPlayers(final File file) throws IOException {
        final Map<String, NPlayer> nplayers = new TreeMap<>();
        this.file = file;
        processFile(file, kv -> {
            final var nplayer = nplayers.computeIfAbsent(kv[1].trim(), NPlayer::new);
            nplayer.add(kv[0].trim());
        });
        listNPlayers.addAll(nplayers.values());
        if (listNPlayers.isEmpty())
            throw new IOException(Messages.getString("NPlayers.NoNPlayersData")); //$NON-NLS-1$
    }

    /**
     * Returns the specific section of the INI configuration file targeted for
     * parsing.
     * 
     * @return the section header {@code "[NPlayers]"}
     */
    @Override
    public String getSection() {
        return "[NPlayers]";
    }

    /**
     * Factory method to read and instantiate a new {@code NPlayers} filter registry
     * from the specified file.
     * 
     * @param file the target {@code nplayers.ini} file to parse
     * @return an initialized {@link NPlayers} manager containing mapped multiplayer
     *         categories
     * @throws IOException if a file reading error occurs, or if parsing fails to
     *                     find valid entries
     */
    public static NPlayers read(final File file) throws IOException {
        return new NPlayers(file);
    }

    /**
     * Returns an iterator over all parsed {@link NPlayer} multiplayer configuration
     * categories.
     * 
     * @return an {@link Iterator} of multiplayer capability instances
     */
    @Override
    public Iterator<NPlayer> iterator() {
        return listNPlayers.iterator();
    }

}
