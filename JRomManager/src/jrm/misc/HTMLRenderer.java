package jrm.misc;

public interface HTMLRenderer
{
	public default String getHTML()
	{
		return toString();
	}
	
	public default String toHTML(CharSequence str)
	{
		return "<html>"+str+"</html>";
	}

	public default String toNoBR(CharSequence str)
	{
		return "<nobr>"+str+"</nobr>";
	}

	public default String toBlue(CharSequence str)
	{
		return "<span color='blue'>"+str+"</span>";
	}

	public default String toPurple(CharSequence str)
	{
		return "<span color='purple'>"+str+"</span>";
	}

	public default String toBold(CharSequence str)
	{
		return "<b>"+str+"</b>";
	}

	public default String toItalic(CharSequence str)
	{
		return "<i>"+str+"</i>";
	}
}
