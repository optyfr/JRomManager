package jrm.fx.ui.controls;

import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;

public final class VersionCellFactory<T> extends TableCell<T,String>
{
	@Override
	protected void updateItem(String item, boolean empty)
	{
		if (empty)
			setText("");
		else if(item == null)
		{
			setTextFill(Color.GRAY);
			setText("???");
		}
		else
			setText(item);
		setGraphic(null);
		setStyle("");
	}
}