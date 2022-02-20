package jrm.fx.ui.web;

import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import javafx.scene.paint.Color;
import lombok.experimental.UtilityClass;

public final @UtilityClass class HTMLFormatter
{
	private static Pattern html = Pattern.compile("<html>(.*)</html>", Pattern.CASE_INSENSITIVE);
	
	private static String head = """
<head>
	<style>
		body {
			padding:1 1;
			border:1px inset;
			border-radius:2px;
			overflow:hidden;
			white-space:nowrap;
			text-overflow:ellipsis;
		}
		table,body {
			font:normal 12px sans-serif;
			margin:0 0;
			background: %s;
		}
		td {
			padding:1 1;
			overflow:hidden;
			white-space:nowrap;
			text-overflow:ellipsis;
			text-align: center;
			vertical-align: middle;
			height:16px;
		}
		progress {
			height:8px;
			width:100px;
			border:1px solid #777777;
			border-radius:4px;
			color:#7799CC;
			background-color:#DDDDDD;
		}
		progress::-moz-progress-bar { background: #7799CC; }
		progress::-webkit-progress-value { background: #7799CC; }
		progress::-webkit-progress-bar {background-color: #DDDDDD; width: 100%%;}
	</style>
</head>""";
	
	public static String toHTML(String any)
	{
		return toHTML(any, null);
	}
	
	public static String toHTML(String any, Color color)
	{
		final var head = HTMLFormatter.head.formatted(color == null ? "none" : String.format("rgb(%d,%d,%d)", (int) (color.getRed() * 255), (int) (color.getGreen() * 255), (int) (color.getBlue() * 255)));
		if(any==null)
			return "<html>" + head + "</html>";
		final var matcher = html.matcher(any);
		if(matcher.matches()) // it is html
			return "<html>" + head + matcher.group(1) + "</html>";
		else	// assumed as simple text
			return "<html>" + head + StringEscapeUtils.escapeHtml4(any) + "</html>";
	}
}
