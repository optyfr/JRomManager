package jrm.misc;

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
}
