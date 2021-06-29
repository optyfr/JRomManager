package jrm.ui.profile.filter;

import javax.swing.AbstractListModel;

import jrm.profile.filter.NPlayer;
import jrm.profile.filter.NPlayers;

@SuppressWarnings("serial")
public class NPlayersModel extends AbstractListModel<NPlayer>
{
	private final NPlayers nplayers;
	
	public NPlayersModel(final NPlayers nplayers)
	{
		this.nplayers = nplayers;
	}

	@Override
	public int getSize()
	{
		if(nplayers!=null)
			return nplayers.getListNPlayers().size();
		return 0;
	}

	@Override
	public NPlayer getElementAt(int index)
	{
		if(nplayers!=null)
			return nplayers.getListNPlayers().get(index);
		return null;
	}

}
