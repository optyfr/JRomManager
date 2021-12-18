package jrm.fx.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Loading extends Stage
{
	public Loading()
	{
		initModality(Modality.WINDOW_MODAL);
		initStyle(StageStyle.UNDECORATED);
		final var root = new HBox();
		root.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		root.setPadding(new Insets(5));
		root.getChildren().addAll(new ImageView(MainFrame.getIcon("/jrm/resicons/waiting.gif")), new Label("Loading..."));
		setScene(new Scene(root));
		show();
	}

}
