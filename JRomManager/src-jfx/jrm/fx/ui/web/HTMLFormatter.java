package jrm.fx.ui.web;

import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import lombok.experimental.UtilityClass;

public final @UtilityClass class HTMLFormatter
{
	private static Pattern pattern = Pattern.compile("<html>(.*)</html>", Pattern.CASE_INSENSITIVE);
	
	private static String head = """
<head>
	<style>
		body{
			font:normal 12px sans-serif;
			margin:0 0;
			padding:1 1;
			border:1px inset;
			border-radius:2px;
		}
	</style>
</head>""";
	
	public static String toHTML(String any)
	{
		final var matcher = pattern.matcher(any);
		if(matcher.matches()) // it is html
			return "<html>" + head + matcher.group(1) + "</html>"; 
		else	// assumed as simple text
			return "<html>" + head + StringEscapeUtils.escapeHtml4(any) + "</html>"; 
	}
}
