package jrm.fx.ui;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.batch.TrntChkReport;
import jrm.fx.ui.misc.Settings;
import jrm.locale.Messages;
import jrm.security.Session;
import jrm.security.Sessions;

class BatchTorrentResults extends Stage
{
	private BatchTorrentResultsController controller;
	
	private Session session;
	
	public BatchTorrentResults(Stage parent, TrntChkReport results) throws URISyntaxException, IOException
	{
		super();
		session = Sessions.getSingleSession();
		initOwner(parent);
		initModality(Modality.WINDOW_MODAL);
		getIcons().add(parent.getIcons().get(0));
		setOnShowing(e -> {
			Settings.fromJson(session.getUser().getSettings().getProperty("BatchTorrentResults.Bounds", null), this);
		});
		setOnCloseRequest(e -> {
			session.getUser().getSettings().setProperty("BatchTorrentResults.Bounds", Settings.toJson(this));
			hide();
		});
		final var loader = new FXMLLoader(getClass().getResource("BatchTorrentResults.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<BorderPane>load();
		controller = loader.getController();

		if (results != null)
			controller.setResult(results);

		setScene(new Scene(root));
		sizeToScene();
		show();
	}
}
