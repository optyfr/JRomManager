package jrm.fx.ui.controls;

import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;

public final class NameCellFactory<T> extends TextFieldTableCell<T, String>
{
	public NameCellFactory(StringConverter<String> converter)
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
	public void updateItem(String item, boolean empty)
	{
		super.updateItem(item, empty);
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