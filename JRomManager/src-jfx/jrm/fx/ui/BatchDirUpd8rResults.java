package jrm.fx.ui;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.batch.DirUpdaterResults;
import jrm.fx.ui.misc.Settings;
import jrm.locale.Messages;
import jrm.security.Session;
import jrm.security.Sessions;

class BatchDirUpd8rResults extends Stage
{
	private BatchDirUpd8rResultsController controller;
	
	private Session session;
	
	public BatchDirUpd8rResults(Stage parent, DirUpdaterResults results) throws URISyntaxException, IOException
	{
		super();
		session = Sessions.getSingleSession();
		initOwner(parent);
		initModality(Modality.WINDOW_MODAL);
		getIcons().add(parent.getIcons().get(0));
		setOnShowing(e -> {
			Settings.fromJson(session.getUser().getSettings().getProperty("BatchDirUpd8rResults.Bounds", null), this);
		});
		setOnCloseRequest(e -> {
			session.getUser().getSettings().setProperty("BatchDirUpd8rResults.Bounds", Settings.toJson(this));
			hide();
		});
		final var loader = new FXMLLoader(getClass().getResource("BatchDirUpd8rResults.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<BorderPane>load();
		controller = loader.getController();
		if(results!=null && results.getResults()!=null)
			controller.getResultList().getItems().setAll(results.getResults());
		setScene(new Scene(root));
		sizeToScene();
		show();
	}
}
