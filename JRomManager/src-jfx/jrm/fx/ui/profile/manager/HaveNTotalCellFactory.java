package jrm.fx.ui.profile.manager;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import jrm.profile.manager.ProfileNFOStats.HaveNTotal;

public final class HaveNTotalCellFactory<T> extends TableCell<T,HaveNTotal>
{
	@Override
	protected void updateItem(HaveNTotal hnt, boolean empty)
	{
		setAlignment(Pos.CENTER);
		setGraphic(null);
		setStyle("");
		if (hnt == null || empty)
			setText("");
		else if(hnt.getHave() == null)
		{
			if((hnt.getTotal() == null))
			{
				setTextFill(Color.GRAY);
				setText("?/?");
			}
			else
			{
				final var tf = new TextFlow();
				final var have = new Text("?");
				have.setStroke(Color.GRAY);
				tf.getChildren().add(have);
				tf.getChildren().add(new Text(String.format("/%d", hnt.getTotal())));
				setGraphic(tf);
			}
		}
		else
		{
			final var tf = new TextFlow();
			final var have = new Text(hnt.getHave().toString());
			final var total = new Text(String.format("/%d", hnt.getTotal()));
			if(hnt.getHave() == 0 && hnt.getTotal() > 0)
				have.setFill(Color.RED);
			else if(hnt.getHave().equals(hnt.getTotal()))
				have.setFill(Color.GREEN);
			else
				have.setFill(Color.ORANGE);
			tf.setMinWidth(USE_PREF_SIZE);
			tf.setPrefWidth(USE_COMPUTED_SIZE);
			tf.getChildren().addAll(have, total);
			tf.setTextAlignment(TextAlignment.CENTER);
			tf.setPrefHeight(10);
			setGraphic(tf);
		}
	}
}