package jrm.misc;

import org.apache.commons.text.StringEscapeUtils;

public interface HTMLRenderer
{
	public default String getHTML()
	{
		return toString();
	}

	public default String toHTML(final CharSequence str)
	{
		return "<html>"+str+"</html>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public default String toNoBR(final CharSequence str)
	{
		return "<nobr>"+str+"</nobr>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public default String toBlue(final CharSequence str)
	{
		return "<span color='blue'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public default String toRed(final CharSequence str)
	{
		return "<span color='red'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public default String toGreen(final CharSequence str)
	{
		return "<span color='green'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public default String toGray(final CharSequence str)
	{
		return "<span color='gray'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public default String toOrange(final CharSequence str)
	{
		return "<span color='orange'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public default String toPurple(final CharSequence str)
	{
		return "<span color='purple'>"+str+"</span>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public default String toBold(final CharSequence str)
	{
		return "<b>"+str+"</b>"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public default String toItalic(final CharSequence str)
	{
		return "<i>"+str+"</i>"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public default String progress(final int i, final int max, final String msg)
	{
		return String.format("<html>"
				+ "<table cellpadding=2 cellspacing=0><tr>"
				+ "	<td valign='middle'><table cellpadding=0 cellspacing=0 style='width:%dpx;font-size:2px;border:1px solid gray'><tr>"
				+ "		<td style='width:%dpx;background:#ff00'></td>"
				+ "		<td></td>"
				+ "	</table></td>"
				+ "	<td style='font-size:95%%;white-space:nowrap'>%s</td>"
				+ "</table>"
			, 108, i*100/max, StringEscapeUtils.escapeHtml4(msg));
	}
}
