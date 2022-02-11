package jrm.fx.ui.controls;

import javafx.scene.control.Tooltip;
import javafx.util.StringConverter;
import jrm.locale.Messages;
import jrm.profile.manager.ProfileNFO;

public class ProfileCellFactory extends NameCellFactory<ProfileNFO>
{
	public ProfileCellFactory()
	{
		super(new StringConverter<>() {

			ProfileNFO nfo;
			
			@Override
			public String toString(ProfileNFO nfo)
			{
				this.nfo = nfo;
				if(nfo!=null)
					return nfo.getName();
				return null;
			}

			@Override
			public ProfileNFO fromString(String string)
			{
				if(nfo!=null)
					nfo.setNewName(string);
				return nfo;
			}
			
		});
	}
	
	
	@Override
	public void updateItem(ProfileNFO item, boolean empty)
	{
		super.updateItem(item, empty);
		if (!empty)
			switch (item.getMame().getStatus())
			{
				case UPTODATE:
					setTextFill(javafx.scene.paint.Color.valueOf("#00aa00")); //$NON-NLS-1$
					setTooltip(new Tooltip(String.format(Messages.getString("FileTableCellRenderer.IsUpToDate"), item.getName()))); //$NON-NLS-1$
					break;
				case NEEDUPDATE:
					setTextFill(javafx.scene.paint.Color.valueOf("#cc8800")); //$NON-NLS-1$
					setTooltip(new Tooltip(String.format(Messages.getString("FileTableCellRenderer.NeedUpdateFromMame"), item.getName()))); //$NON-NLS-1$
					break;
				case NOTFOUND:
					setTextFill(javafx.scene.paint.Color.valueOf("#cc0000")); //$NON-NLS-1$
					setTooltip(new Tooltip(String.format(Messages.getString("FileTableCellRenderer.StatusUnknownMameNotFound"), item.getName()))); //$NON-NLS-1$
					break;
				default:
					setTextFill(javafx.scene.paint.Color.BLACK);
					setTooltip(new Tooltip(getText()));
					break;
			}
	}
}
