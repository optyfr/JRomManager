package jrm.fx.ui.progress;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.locale.Messages;
import lombok.Getter;

public class Progress extends Stage
{
	private @Getter ProgressController controller;

	public Progress(Stage parent, ProgressTask<?> task) throws IOException, URISyntaxException
	{
		super();
		initOwner(parent);
		initModality(Modality.WINDOW_MODAL);
		getIcons().add(parent.getIcons().get(0));
		setOnShowing(e -> {
		});
		setOnCloseRequest(e -> hide());
		final var loader = new FXMLLoader(getClass().getResource("Progress.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<GridPane>load();
		controller = loader.getController();
		controller.setTask(task);
		setScene(new Scene(root));
		sizeToScene();
		show();
	}
}
