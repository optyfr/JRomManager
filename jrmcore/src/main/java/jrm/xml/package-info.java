/**
 * XML helper classes and streaming utilities for the Retro-Gaming ROM manager.
 * <p>
 * This package provides tools for robust, structured, and well-formatted XML
 * serialization. It contains:
 * </p>
 * <ul>
 * <li>{@link jrm.xml.EnhancedXMLStreamWriter}: A highly configurable,
 * state-tracking decorator implementing
 * {@link javax.xml.stream.XMLStreamWriter} that automates element indentation,
 * carriage returns, and null-safe attribute writing.</li>
 * <li>{@link jrm.xml.SimpleAttribute}: A lightweight, immutable value object
 * representing a key-value XML attribute pair, enabling simplified element
 * creation with varargs attributes.</li>
 * </ul>
 * <p>
 * The classes in this package work in tandem to eliminate boilerplates and
 * manual spacing calculations when writing structured XML documents such as ROM
 * database profiles or metadata lists.
 * </p>
 *
 * @author optyfr
 * @since 1.0
 */
package jrm.xml;
