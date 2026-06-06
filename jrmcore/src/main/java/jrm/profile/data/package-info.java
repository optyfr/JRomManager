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

/**
 * Provides the domain model classes and data structures representing arcade and computer systems,
 * machines, software, and their associated sub-entities (such as ROMs, Disks, and Samples) for 
 * JRomManager profiles.
 * 
 * <p>
 * This package forms the core representation of emulator metadata database files (DAT files) inside the application.
 * The primary abstraction is {@link jrm.profile.data.Anyware}, which serves as the common base class for 
 * systems and games (e.g., Arcade Machines or Software List entries).
 * </p>
 * 
 * <h2>Key Concepts and Structure</h2>
 * <ul>
 *   <li><b>Systems and Entities:</b> An {@link jrm.profile.data.Anyware} is composed of zero or more 
 *   {@link jrm.profile.data.Rom}s, {@link jrm.profile.data.Disk}s, and {@link jrm.profile.data.Sample}s. These entities
 *   inherit from {@link jrm.profile.data.EntityBase} and implement status tracking.</li>
 *   
 *   <li><b>Parent-Clone Relationships:</b> Many systems are variations of a parent game. The model keeps 
 *   track of clone states, referencing the parent system to support diverse merging schemes during file scanning and reconstruction.</li>
 *   
 *   <li><b>BIOS and Device Relationships:</b> Some systems depend on shared BIOS files or hardware device components 
 *   which are nested and fetched during validation and file filtering.</li>
 *   
 *   <li><b>Merging Modes support:</b> Support is provided for different rom merging styles, including:
 *     <ul>
 *       <li><i>Split:</i> Clone zip files only contain ROMs that are unique to them, whereas parent zip files contain the common ROMs.</li>
 *       <li><i>Merge:</i> Clone ROMs are merged directly into the parent's zip file.</li>
 *       <li><i>No Merge:</i> Clone zip files contain all required ROMs, repeating files that also exist in the parent.</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * @author optyfr
 * @since 1.0
 */
package jrm.profile.data;
