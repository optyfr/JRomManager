package jrm.fx.ui.controls;

import javafx.scene.control.TableCell;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public final class VersionCellFactory<T> extends TableCell<T,String>
{
	@Override
	protected void updateItem(String item, boolean empty)
	{
		setFont(new Font(10));
		if (empty)
			setText("");
		else if(item == null)
		{
			setTextFill(getTableRow().isSelected()?Color.LIGHTGRAY:Color.GRAY);
			setText("???");
		}
		else
		{
			setText(item);
			setTooltip(new Tooltip(item));
		}
		setGraphic(null);
		setStyle("");
	}
}