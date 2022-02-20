package jrm.aui.status;

public class PlainTextRenderer implements StatusRenderer
{

	@Override
	public String toDocument(CharSequence str)
	{
		return str.toString();
	}

	@Override
	public String toNoBR(CharSequence str)
	{
		return str.toString();
	}

	@Override
	public String toLabel(CharSequence str, String webcolor, boolean bold, boolean italic)
	{
		return str.toString();
	}

	@Override
	public String progress(int width, int i, int max, String msg)
	{
		if(msg==null)
			return toDocument("");
		return toDocument(escape(msg));
	}
	
	@Override
	public String escape(CharSequence str)
	{
		return str.toString();
	}

	@Override
	public boolean hasProgress()
	{
		return false;
	}
	
}
