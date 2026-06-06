/**
 * Provides comprehensive classes for managing ROM profiles, database file formats, and physical folders.
 * <p>
 * This package handles logical operations related to profile discovery, MAME executable integration,
 * XML metadata import/export, and internal profile representation. Key features include:
 * </p>
 * <ul>
 *   <li><b>Directory Trees:</b> Hierarchical physical filesystem discovery and custom rendering via {@link jrm.profile.manager.Dir} and {@link jrm.profile.manager.DirTree}.</li>
 *   <li><b>Import &amp; Export:</b> Automatic generation of JRomManager profile definitions from physical MAME executables using command-line query extraction as well as structured export capabilities into standard MAME XML, Datafile/Logiqx XML, and Software Lists via {@link jrm.profile.manager.Import} and {@link jrm.profile.manager.Export}.</li>
 *   <li><b>Profile Metadata Management:</b> Serializing, tracking, and representing profile metrics, scan statistics, and MAME-specific update requirements via {@link jrm.profile.manager.ProfileNFO}, {@link jrm.profile.manager.ProfileNFOMame}, and {@link jrm.profile.manager.ProfileNFOStats}.</li>
 *   <li><b>Format Conversions:</b> Parallel parser pipelines reading standard ROM database formats.</li>
 * </ul>
 * 
 * @author optyfr
 */
package jrm.profile.manager;
