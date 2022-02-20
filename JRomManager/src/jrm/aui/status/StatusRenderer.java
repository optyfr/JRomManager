package jrm.aui.status;

import java.util.Optional;

interface StatusRenderer
{
	/**
	 * create a document
	 * @param str the string to wrap
	 * @return {@link String} result
	 */
	public String toDocument(final CharSequence str);

	public default String toStr(Object any)
	{
		return Optional.ofNullable(any).map(Object::toString).orElse("");
	}
	
	public String toNoBR(final CharSequence str);

	public default String toBlue(CharSequence str)
	{
		return toLabel(str, "blue");
	}
	
	public default String toBoldBlue(CharSequence str)
	{
		return toLabel(str, "blue", true);
	}
	
	public default String toRed(CharSequence str)
	{
		return toLabel(str, "red");
	}
	
	public default String toGreen(CharSequence str)
	{
		return toLabel(str, "green");
	}
	
	public default String toBoldGreen(CharSequence str)
	{
		return toLabel(str, "green", true);
	}
	
	public default String toGray(CharSequence str)
	{
		return toLabel(str, "gray");
	}
	
	public default String toOrange(CharSequence str)
	{
		return toLabel(str, "orange");
	}
	
	public default String toPurple(CharSequence str)
	{
		return toLabel(str, "purple");
	}
	
	public default String toBold(CharSequence str)
	{
		return toLabel(str, null, true);
	}
	
	public default String toItalic(CharSequence str)
	{
		return toLabel(str, null, false, true);
	}
	
	public default String toLabel(CharSequence str, String webcolor)
	{
		return toLabel(str, webcolor, false, false);
	}
	
	public default String toLabel(CharSequence str, String webcolor, boolean bold)
	{
		return toLabel(str, webcolor, bold, false);
	}
	
	public String toLabel(CharSequence str, String webcolor, boolean bold, boolean italic);
	
	/**
	 * Create an HTML progress bar (with table and cells) followed by a message
	 * @param i the current {@code int} value
	 * @param max the maximum {@code int} value
	 * @param msg the message to show after progress bar
	 * @return {@link String} result
	 */
	public default String progress(int i, int max, String msg)
	{
		return progress(100, i, max, msg);
	}
	
	public String progress(int width, int i, int max, String msg);
	
	public String escape(CharSequence str);
	
	public default String getDocument()
	{
		return toString();
	}
	
	public boolean hasProgress();
}
