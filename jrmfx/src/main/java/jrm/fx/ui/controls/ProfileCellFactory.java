package jrm.fx.ui.controls;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
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
		{
			styleProperty().bind(Bindings.when(getTableRow().selectedProperty()).then(new SimpleStringProperty(switch (item.getMame().getStatus())
			{
				case UPTODATE -> "-fx-text-fill: #aaffaa;-fx-font-size: .75em;";
				case NEEDUPDATE -> "-fx-text-fill: #ffaa88;-fx-font-size: .75em;";
				case NOTFOUND -> "-fx-text-fill: #ffaaaa;-fx-font-size: .75em;";
				default -> "-fx-text-fill: #ffffff;-fx-font-size: .75em;";
			})).otherwise(new SimpleStringProperty(switch (item.getMame().getStatus())
			{
				case UPTODATE -> "-fx-text-fill: #00aa00;-fx-font-size: .75em;";
				case NEEDUPDATE -> "-fx-text-fill: #cc8800;-fx-font-size: .75em;";
				case NOTFOUND -> "-fx-text-fill: #cc0000;-fx-font-size: .75em;";
				default -> "-fx-text-fill: #000000;-fx-font-size: .75em;";
			})));
			setTooltip(new Tooltip(switch (item.getMame().getStatus())
			{
				case UPTODATE -> String.format(Messages.getString("FileTableCellRenderer.IsUpToDate"), item.getName());
				case NEEDUPDATE -> String.format(Messages.getString("FileTableCellRenderer.NeedUpdateFromMame"), item.getName());
				case NOTFOUND -> String.format(Messages.getString("FileTableCellRenderer.StatusUnknownMameNotFound"), item.getName());
				default -> getText();
			}));
		}
	}
}
