package jrm.fx.ui.controls;

import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public class NameCellFactory<T> extends TextFieldTableCell<T, T>
{
	public NameCellFactory(StringConverter<T> converter)
	{
		super(converter);
		setEditable(true);
	}

	public NameCellFactory()
	{
		super();
		setEditable(true);
	}

	@Override
	public void updateItem(T item, boolean empty)
	{
		super.updateItem(item, empty);
		if (empty)
			setText("");
		else
		{
			setText(item.toString());
			setTooltip(new Tooltip(item.toString()));
		}
		setGraphic(null);
	//	setStyle("");
	}
}