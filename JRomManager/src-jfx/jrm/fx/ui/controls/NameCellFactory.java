package jrm.fx.ui.controls;

import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;

public final class NameCellFactory<T> extends TableCell<T,String>
{
	@Override
	protected void updateItem(String item, boolean empty)
	{
		if (empty)
			setText("");
		else
		{
			setText(item);
			setTooltip(new Tooltip(item));
		}
		setGraphic(null);
		setStyle("");
	}
}