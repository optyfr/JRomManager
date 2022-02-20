package jrm.aui.status;

import lombok.Setter;

public interface StatusRendererFactory extends StatusRenderer
{
	public static class Factory
	{
		private static @Setter StatusRenderer instance = new Html5Renderer();
		
		private Factory() {}
	}
	
	default StatusRenderer getFactory()
	{
		return Factory.instance;
	}
	
	@Override
	default String toDocument(CharSequence str)
	{
		return getFactory().toDocument(str);
	}

	@Override
	default String toNoBR(CharSequence str)
	{
		return getFactory().toNoBR(str);
	}
	
	@Override
	default String toLabel(CharSequence str, String webcolor, boolean bold, boolean italic)
	{
		return getFactory().toLabel(str, webcolor, bold, italic);
	}
	
	@Override
	default String progress(int width, int i, int max, String msg)
	{
		return getFactory().progress(width, i, max, msg);
	}
	
	@Override
	default String escape(CharSequence str)
	{
		return getFactory().escape(str);
	}
	
	@Override
	default boolean hasProgress()
	{
		return getFactory().hasProgress();
	}
}
