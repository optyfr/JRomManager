package jrm.fx.ui.controls;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.text.Font;

public final class ButtonCellFactory<S, T> extends TableCell<S, T>
{
	private final Button btn;

	public interface Action<S, T>
	{
		void doAction(TableCell<S, T> cell);
	}
	
	public ButtonCellFactory(String name, Action<S, T> action)
	{
		super();
		btn = new Button(name);
		btn.setFont(new Font(10));
		btn.setMinHeight(15);
		btn.setPadding(new Insets(0));
		btn.setMaxWidth(Double.MAX_VALUE);
		btn.setOnAction(e -> action.doAction(this));
		setMinHeight(15);
	}

	@Override
	protected void updateItem(T item, boolean empty)
	{
		super.updateItem(item, empty);
		setGraphic(empty ? null : btn);
	}
}
