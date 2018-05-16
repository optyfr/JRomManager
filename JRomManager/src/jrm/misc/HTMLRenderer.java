package jrm.misc;

public interface HTMLRenderer
{
	public default String getHTML()
	{
		return toString();
	}

	public default String toHTML(final CharSequence str)
	{
		return "<html>"+str+"</html>";
	}

	public default String toNoBR(final CharSequence str)
	{
		return "<nobr>"+str+"</nobr>";
	}

	public default String toBlue(final CharSequence str)
	{
		return "<span color='blue'>"+str+"</span>";
	}

	public default String toRed(final CharSequence str)
	{
		return "<span color='red'>"+str+"</span>";
	}

	public default String toGreen(final CharSequence str)
	{
		return "<span color='green'>"+str+"</span>";
	}

	public default String toGray(final CharSequence str)
	{
		return "<span color='gray'>"+str+"</span>";
	}

	public default String toOrange(final CharSequence str)
	{
		return "<span color='orange'>"+str+"</span>";
	}

	public default String toPurple(final CharSequence str)
	{
		return "<span color='purple'>"+str+"</span>";
	}

	public default String toBold(final CharSequence str)
	{
		return "<b>"+str+"</b>";
	}

	public default String toItalic(final CharSequence str)
	{
		return "<i>"+str+"</i>";
	}
}
