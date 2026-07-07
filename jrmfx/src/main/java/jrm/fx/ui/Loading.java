package jrm.fx.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

/**
 * Undecorated modal loading indicator stage.
 * <p>
 * Displays a small animated GIF with a "Loading..." label while the main application
 * initializes in the background.
 *
 * @since 2.5
 */
public class Loading extends Stage {
    /**
     * Constructs and shows the loading indicator.
     */
    public Loading() {
        initModality(Modality.WINDOW_MODAL);
        initStyle(StageStyle.UNDECORATED);
        final var root = new HBox();
        root.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
        root.setPadding(new Insets(5));
        final var iv = new ImageView(MainFrame.getIcon("/jrm/resicons/waiting.gif"));
        root.getChildren().addAll(iv, new Label("Loading..."));
        root.setAlignment(Pos.CENTER);
        setScene(new JRMScene(root));
        show();
    }

}
