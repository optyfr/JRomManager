/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.ui.basic;

/**
 * Enumeration defining the types of files that can be dropped onto a file drop component.
 * <p>
 * This enum controls the filtering behavior for drag-and-drop operations, determining whether
 * the component accepts any file type, only directories, or only regular files.
 * </p>
 *
 * @see JFileDropList
 * @see JFileDropTextField
 */
public enum JFileDropMode {

    /** Accept any file or directory. */
    ANY,

    /** Accept only directories. */
    DIRECTORY,

    /** Accept only regular files. */
    FILE
}
