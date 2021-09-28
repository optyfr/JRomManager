package jrm.ui.profile.data;

import javax.swing.AbstractListModel;

import jrm.profile.data.Source;
import jrm.profile.data.Sources;

@SuppressWarnings("serial")
public class SourcesModel extends AbstractListModel<Source>
{

	private final Sources sources;
	
	public SourcesModel(final Sources sources)
	{
		this.sources = sources;
	}

	@Override
	public int getSize()
	{
		return sources.getSrces().size();
	}

	@Override
	public Source getElementAt(int index)
	{
		return sources.getSrces().get(index);
	}

}
