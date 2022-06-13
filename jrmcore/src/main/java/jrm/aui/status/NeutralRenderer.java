package jrm.aui.status;

import java.util.Optional;

import org.apache.commons.text.StringEscapeUtils;

public class NeutralRenderer implements StatusRenderer
{

	@Override
	public String toDocument(CharSequence str)
	{
		return "<document>" + str + "</document>";
	}

	@Override
	public String toNoBR(CharSequence str)
	{
		return str.toString();
	}

	@Override
	public String toLabel(CharSequence str, String webcolor, boolean bold, boolean italic)
	{
		return "<label color=\"%s\" bold=\"%b\" italic=\"%b\">%s</label>".formatted(Optional.ofNullable(webcolor).orElse("black"), bold, italic, str);
	}

	@Override
	public String progress(int width, int i, int max, String msg)
	{
		if(msg==null)
			return toDocument(internalProgress(width, i, max));
		return toDocument(internalProgress(width, i, max) + " " + escape(msg));
	}

	protected String internalProgress(final int width, final long i, final long max)
	{
		return String.format("<progress width=\"%d\" value=\"%d\" max=\"%d\"></progress>", width, i, max);
	}
	

	@Override
	public String escape(CharSequence str)
	{
		return StringEscapeUtils.escapeXml10(str.toString());
	}

	@Override
	public boolean hasProgress()
	{
		return true;
	};
}
