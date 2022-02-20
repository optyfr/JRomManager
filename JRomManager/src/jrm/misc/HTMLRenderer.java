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

import java.util.Optional;

import org.apache.commons.text.StringEscapeUtils;

import lombok.Setter;
import lombok.experimental.UtilityClass;

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

	static @UtilityClass class Options
	{
		private static @Setter boolean isPlain = false;
		private static @Setter boolean isAbstract = false;
		private static @Setter boolean isHTML5 = true;
	}
	
	public default boolean isPlain()
	{
		return Options.isPlain;
	}
	
	public default boolean isAbstract()
	{
		return Options.isAbstract;
	}
	
	public default boolean isHTML5()
	{
		return Options.isHTML5;
	}
	
	
	/**
	 * Wrap {@code <html>} and {@code </html>} tags around a string
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toHTML(final CharSequence str)
	{
		if (isPlain())
			return str.toString();
		if (isAbstract())
			return "<hbox>" + str + "</hbox>";
		return "<html><body>" + str + "</body></html>";
	}

	public default String toStr(Object any)
	{
		return Optional.ofNullable(any).map(Object::toString).orElse("");
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
		if (isAbstract())
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
		if (isAbstract())
			return "<label color=\"blue\">"+str+"</label>";
		return "<span style='color:blue'>"+str+"</span>";
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
		if (isAbstract())
			return "<label color=\"red\">"+str+"</label>";
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
		if (isAbstract())
			return "<label color=\"green\">"+str+"</label>";
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
		if (isAbstract())
			return "<label color=\"gray\">"+str+"</label>";
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
		if (isAbstract())
			return "<label color=\"orange\">"+str+"</label>";
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
		if (isAbstract())
			return "<label color=\"purple\">"+str+"</label>";
		return "<span style='color:purple'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Make the string <b>bold</b> by wrapping using {@code <b></b>} tag
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toBold(final CharSequence str)
	{
		if (isPlain())
			return str.toString();
		return "<b>" + str + "</b>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Make the string <i>italic</i> by wrapping using {@code <i></i>} tag
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public default String toItalic(final CharSequence str)
	{
		if (isPlain())
			return str.toString();
		return "<i>" + str + "</i>"; //$NON-NLS-1$ //$NON-NLS-2$
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
		return progress(100, i, max, msg);
	}
	
	public default String progress(final int width, final int i, final int max, final String msg)
	{
		if(msg!=null)
		{
			if(isPlain())
				return msg + " " + internalProgress(width, i, max);
			if (isAbstract())
				return toHTML(internalProgress(width, i, max) + escape(msg));
			return String.format("<html><table cellpadding=2 cellspacing=0><tr><td valign='middle'>%s</td><td style='font-size:95%%;white-space:nowrap'>%s</td></table></html>", internalProgress(width, i, max), escape(msg));
		}
		else
		{
			if(isPlain())
				return internalProgress(width, i, max);
			if (isAbstract())
				return toHTML(internalProgress(width, i, max));
			return String.format("<html><table cellpadding=2 cellspacing=0><tr><td valign='middle'>%s</td></table></html>", internalProgress(width, i, max));
		}
	}
	
	
	public default String internalProgress(final int width, final long i, final long max)
	{
		if(isPlain())
			return String.format("(%d/%d)", i, max);
		if (isAbstract())
			return String.format("<progress width=\"%d\" value=\"%d\" max=\"%d\"></progress>", width, i, max);
		if(isHTML5())
			return String.format("<progress style=\"width:%dpx\" value=\"%d\" max=\"%d\"></progress>", width, i, max);
		return String.format("<table cellpadding=0 cellspacing=0 style='width:%dpx;font-size:2px;border:1px solid gray;table-layout:fixed'><tr><td style='width:%dpx;height:2px;background-color:#00ff00'></td><td></td></table>", width + 8, i * width / max);
	}
	
	public default String escape(CharSequence str)
	{
		if(isPlain())
			return str.toString();
		if(isAbstract())
			return StringEscapeUtils.escapeXml10(str.toString());
		return StringEscapeUtils.escapeHtml4(str.toString());
	}
}
