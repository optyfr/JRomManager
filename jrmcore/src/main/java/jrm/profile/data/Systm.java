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
package jrm.profile.data;

import java.io.Serializable;

/**
 * Interface defining supported System types inside a profile workspace. Linked
 * selectable systems expose a unique property mapping to properties stores.
 * 
 * @author optyfr
 * @since 1.0
 */
public interface Systm extends Serializable, PropertyStub {
    /**
     * System classification types.
     * 
     * @author optyfr
     * @since 1.0
     */
    public enum Type {
        /**
         * Standard arcade or console machine.
         */
        STANDARD,

        /**
         * Electro-Mechanical machine.
         */
        MECHANICAL,

        /**
         * Device pseudo-machine (such as controllers, floppy disk controllers, memory
         * card units).
         */
        DEVICE,

        /**
         * Basic Input/Output System image.
         */
        BIOS,

        /**
         * MESS or MAME Software list collections.
         */
        SOFTWARELIST
    }

    /**
     * Get the type of the system.
     * 
     * @return the system {@link Type} representation
     */
    public Type getType();

    /**
     * Get the system self-reference.
     * 
     * @return the resolved {@link Systm} instance
     */
    public Systm getSystem();

    /**
     * Get the name of the system.
     * 
     * @return the system name as a String
     */
    public String getName();

    /**
     * Get the default unique preference property key according to its type and
     * name.
     * 
     * @return the preference properties key String
     */
    @Override
    public default String getPropertyName() {
        if (getType() == Type.SOFTWARELIST)
            return "filter.systems.swlist." + getName();
        return "filter.systems." + getName(); //$NON-NLS-1$
    }
}
