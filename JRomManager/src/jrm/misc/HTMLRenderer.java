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
package jrm.misc;

import org.apache.commons.text.StringEscapeUtils;

import lombok.Setter;

/**
 * An interface with default methods to wrap various HTML tags around {@link String} or {@link StringBuffer}
 * @author optyfr
 *
 */
public interface HTMLRenderer
{
	/**
	 * alias of {@code toString()}
	 * @return {@link String}
	 */
	public default String getHTML()
	{
		return toString();
	}

	static class Options
	{
		private static @Setter boolean isPlain = false;
	}
	
	public default boolean isPlain()
	{
		return Options.isPlain;
	}
	
	
	/**
	 * Wrap {@code <html>} and {@code </html>} tags around a string
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toHTML(final CharSequence str)
	{
		if(isPlain())
			return str.toString();
		return "<html>"+str+"</html>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Wrap {@code <nobr>} and {@code </nobr>} tags around a string
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toNoBR(final CharSequence str)
	{
		if(isPlain())
			return str.toString();
		return "<nobr>"+str+"</nobr>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Make the string <span style='color:blue'>blue</span> by wrapping using {@code <span>} tag
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toBlue(final CharSequence str)
	{
		if(isPlain())
			return str.toString();
		return "<span style='color:blue'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Make the string <span style='color:red'>red</span> by wrapping using {@code <span>} tag
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toRed(final CharSequence str)
	{
		if(isPlain())
			return str.toString();
		return "<span style='color:red'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Make the string <span style='color:green'>green</span> by wrapping using {@code <span>} tag
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toGreen(final CharSequence str)
	{
		if(isPlain())
			return str.toString();
		return "<span style='color:green'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Make the string <span style='color:gray'>gray</span> by wrapping using {@code <span>} tag
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toGray(final CharSequence str)
	{
		if(isPlain())
			return str.toString();
		return "<span style='color:gray'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Make the string <span style='color:orange'>orange</span> by wrapping using {@code <span>} tag
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toOrange(final CharSequence str)
	{
		if(isPlain())
			return str.toString();
		return "<span style='color:orange'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Make the string <span style='color:purple'>purple</span> by wrapping using {@code <span>} tag
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toPurple(final CharSequence str)
	{
		if(isPlain())
			return str.toString();
		return "<span style='color:purple'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Make the string <b>bold</b> by wrapping using {@code <b></b>} tag
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toBold(final CharSequence str)
	{
		if(isPlain())
			return str.toString();
		return "<b>"+str+"</b>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Make the string <i>italic</i> by wrapping using {@code <i></i>} tag
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toItalic(final CharSequence str)
	{
		if(isPlain())
			return str.toString();
		return "<i>"+str+"</i>"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Create an HTML progress bar (with table and cells) followed by a message
	 * @param i the current {@code int} value
	 * @param max the maximum {@code int} value
	 * @param msg the message to show after progress bar
	 * @return {@link String} result
	 */
	public default String progress(final int i, final int max, final String msg)
	{
		if(isPlain())
			return msg + " " + progress(100, i, max);
		return String.format("<html>" //$NON-NLS-1$
				+ "<table cellpadding=2 cellspacing=0><tr>" //$NON-NLS-1$
				+ "	<td valign='middle'>%s</td>" //$NON-NLS-1$
				+ "	<td style='font-size:95%%;white-space:nowrap'>%s</td>" //$NON-NLS-1$
				+ "</table>" //$NON-NLS-1$
			, progress(100, i, max), StringEscapeUtils.escapeHtml4(msg));
	}
	
	
	public default String progress(final int width, final long i, final long max)
	{
		if(isPlain())
			return String.format("(%d/%d)", i, max);
		return String.format("<table cellpadding=0 cellspacing=0 style='width:%dpx;font-size:2px;border:1px solid gray;table-layout:fixed'><tr>"
				+ "<td style='width:%dpx;height:2px;background-color:#00ff00'></td>" //$NON-NLS-1$
				+ "<td></td>" //$NON-NLS-1$
				+ "</table>" //$NON-NLS-1$
			, width + 8, i * width / max);
	}
}
