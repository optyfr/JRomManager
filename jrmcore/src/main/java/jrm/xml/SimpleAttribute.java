/*
 * Copyright (C) 2018 optyfr This program is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should
 * have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.xml;

/**
 * A simple immutable representation of an XML attribute containing a name and an associated value.
 * <p>
 * This class serves as a convenient container when serializing XML elements with attributes using {@link EnhancedXMLStreamWriter},
 * allowing developers to supply multiple attributes in a varargs format.
 * </p>
 *
 * @author optyfr
 * 
 * @since 1.0
 */
public final class SimpleAttribute {

    /**
     * The local name of the XML attribute.
     */
    final String name;

    /**
     * The value of the XML attribute, which will be converted to its string representation during writing.
     */
    final Object value;

    /**
     * Constructs a new simple attribute with the specified name and value.
     *
     * @param name the local name of the attribute, typically non-null and non-empty
     * @param value the value associated with the attribute, which can be any object or null
     */
    public SimpleAttribute(final String name, final Object value) {
        this.name = name;
        this.value = value;
    }
}
