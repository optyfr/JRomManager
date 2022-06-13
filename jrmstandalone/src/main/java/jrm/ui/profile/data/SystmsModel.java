package jrm.ui.profile.data;

import javax.swing.AbstractListModel;

import jrm.profile.data.Systm;
import jrm.profile.data.Systms;

@SuppressWarnings("serial")
public class SystmsModel extends AbstractListModel<Systm>
{

	private final Systms systms;
	
	public SystmsModel(final Systms systms)
	{
		this.systms = systms;
	}

	@Override
	public int getSize()
	{
		return systms.getSystems().size();
	}

	@Override
	public Systm getElementAt(int index)
	{
		return systms.getSystems().get(index);
	}

}
