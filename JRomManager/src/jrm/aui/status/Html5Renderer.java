package jrm.aui.status;

public class Html5Renderer extends Html4Renderer
{
	@Override
	protected String internalProgress(final int width, final long i, final long max)
	{
		return String.format("<progress style=\"width:%dpx\" value=\"%d\" max=\"%d\"></progress>", width, i, max);
	}
}
