package jrm.fx.ui.controls;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import jrm.fx.ui.status.NeutralToNodeFormatter;

public final class NodeCellFactory<T> extends TableCell<T,String>
{
	@Override
	protected void updateItem(String item, boolean empty)
	{
		setFont(new Font(10));
		setGraphic(null);
		setStyle("");
		if (empty || item == null)
			setText("");
		else
		{
			setAlignment(Pos.CENTER);
			setGraphic(null);
			setStyle("");
			final var tf = new TextFlow();
			tf.getChildren().addAll(NeutralToNodeFormatter.toNodes(item));
			tf.setMinWidth(USE_PREF_SIZE);
			tf.setPrefWidth(USE_COMPUTED_SIZE);
			tf.setTextAlignment(TextAlignment.LEFT);
			tf.setPrefHeight(10);
			setGraphic(tf);
		}
	}
}