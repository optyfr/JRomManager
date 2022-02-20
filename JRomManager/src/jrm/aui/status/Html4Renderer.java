package jrm.aui.status;

import java.util.Optional;

import org.apache.commons.text.StringEscapeUtils;

public class Html4Renderer implements StatusRenderer
{

	@Override
	public String toDocument(CharSequence str)
	{
		return "<html><body>" + str + "</body></html>";
	}

	@Override
	public String toNoBR(CharSequence str)
	{
		return "<nobr>" + str + "</nobr>";
	}

	@Override
	public String toLabel(CharSequence str, String webcolor, boolean bold, boolean italic)
	{
		String fstr = str.toString();
		if (italic)
			fstr = "<i>" + fstr + "</i>";
		if (bold)
			fstr = "<b>" + fstr + "</b>";
		return "<span style='color:%s'>%s</span>".formatted(Optional.ofNullable(webcolor).orElse("black"), fstr);
	}

	@Override
	public String progress(int width, int i, int max, String msg)
	{
		if(msg==null)
			return String.format("<html><table cellpadding=2 cellspacing=0><tr><td valign='middle'>%s</td></table></html>", internalProgress(width, i, max));
		return String.format("<html><table cellpadding=2 cellspacing=0><tr><td valign='middle'>%s</td><td style='font-size:95%%;white-space:nowrap'>%s</td></table></html>", internalProgress(width, i, max), escape(msg));
	}

	protected String internalProgress(final int width, final long i, final long max)
	{
		return String.format("<table cellpadding=0 cellspacing=0 style='width:%dpx;font-size:2px;border:1px solid gray;table-layout:fixed'><tr><td style='width:%dpx;height:2px;background-color:#00ff00'></td><td></td></table>", width + 8, i * width / max);
	}
	

	@Override
	public String escape(CharSequence str)
	{
		return StringEscapeUtils.escapeHtml4(str.toString());
	}

	@Override
	public boolean hasProgress()
	{
		return true;
	}
}
