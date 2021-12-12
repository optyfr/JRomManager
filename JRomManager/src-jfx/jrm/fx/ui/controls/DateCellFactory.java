package jrm.fx.ui.controls;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import jrm.profile.manager.ProfileNFO;

public final class DateCellFactory extends TableCell<ProfileNFO, Date>
{
	@Override
	protected void updateItem(Date item, boolean empty)
	{
		if (empty)
			setText("");
		else if (item == null)
		{
			setTextFill(Color.GRAY);
			setText("????-??-?? ??:??:??");
		}
		else
			setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item));
		setGraphic(null);
		setStyle("");
	}
}
