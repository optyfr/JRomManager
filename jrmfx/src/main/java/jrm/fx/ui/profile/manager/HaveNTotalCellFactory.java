package jrm.fx.ui.profile.manager;

import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
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
		setGraphic(null);
		setStyle("-fx-text-fill: #000000;");
		if (hnt == null || empty)
			setText("");
		else if(hnt.getHave() == null)
		{
			if((hnt.getTotal() == null))
			{
				setTextFill(getTableRow().isSelected()?Color.LIGHTGRAY:Color.GRAY);
				setText("?/?");
			}
			else
			{
				final var tf = new HBox();
				final var have = new Text("?");
				final var total = new Text(String.format("/%d", hnt.getTotal()));
				have.setFill(getTableRow().isSelected()?Color.LIGHTGRAY:Color.GRAY);
				total.setFill(getTableRow().isSelected()?Color.WHITE:Color.BLACK);
				tf.getChildren().add(have);
				tf.getChildren().add(total);
				tf.setMinWidth(USE_PREF_SIZE);
				tf.setPrefWidth(USE_COMPUTED_SIZE);
				//tf.setTextAlignment(TextAlignment.CENTER);
				tf.setPrefHeight(USE_COMPUTED_SIZE);
				//tf.setPrefHeight(USE_PREF_SIZE);
				tf.setAlignment(Pos.CENTER);
				setGraphic(tf);
			}
		}
		else
		{
			final var tf = new HBox();
			final var have = new Text(hnt.getHave().toString());
			final var total = new Text(String.format("/%d", hnt.getTotal()));
			total.setFill(getTableRow().isSelected()?Color.WHITE:Color.BLACK);
			if(hnt.getHave() == 0 && hnt.getTotal() > 0)
				have.setFill(getTableRow().isSelected()?Color.web("#ffaaaa"):Color.web("#cc0000"));
			else if(hnt.getHave().equals(hnt.getTotal()))
				have.setFill(getTableRow().isSelected()?Color.web("#aaffaa"):Color.web("#00aa00"));
			else
				have.setFill(getTableRow().isSelected()?Color.web("#ffaa88"):Color.web("#cc8800"));
			tf.getChildren().addAll(have, total);
			tf.setMinWidth(USE_PREF_SIZE);
			tf.setPrefWidth(USE_COMPUTED_SIZE);
		//	tf.setTextAlignment(TextAlignment.CENTER);
		//	tf.setPrefHeight(10);
			tf.setPrefHeight(USE_COMPUTED_SIZE);
			tf.setAlignment(Pos.CENTER);
			setGraphic(tf);
		}
		setAlignment(Pos.CENTER);
		setMinHeight(USE_PREF_SIZE);
		setPrefHeight(USE_COMPUTED_SIZE);
	}
}