package jrm.fx.ui.controls;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Font;
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
		styleProperty().bind(new SimpleStringProperty("-fx-font-size: .75em;"));
	//	setStyle("");
	//	setStyle("-fx-text-fill: #000000;-fx-font-size: .75em;");
	//	setStyle("");
	}
}