/**
 * Provides classes and interfaces for profile filtering mechanisms in
 * JRomManager. This includes filters based on categories (catver.ini), number
 * of players (nplayers.ini), and dynamic keyword filtering extracted from ROM
 * descriptions.
 * <p>
 * The main components are:
 * </p>
 * <ul>
 * <li>{@link jrm.profile.filter.CatVer} - Handles category-based filtering
 * using <code>catver.ini</code>.</li>
 * <li>{@link jrm.profile.filter.NPlayers} - Handles player count-based
 * filtering using <code>nplayers.ini</code>.</li>
 * <li>{@link jrm.profile.filter.Keywords} - Analyzes ROM descriptions and
 * offers keyword-based tag filtering.</li>
 * <li>{@link jrm.profile.filter.IniProcessor} - Facilitates parsing of
 * key-value mapping configuration files.</li>
 * </ul>
 *
 * @since 1.0
 * @author optyfr
 */
package jrm.profile.filter;
