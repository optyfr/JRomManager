package jrm.fx.ui.profile.report;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.fx.ui.misc.Settings;
import jrm.locale.Messages;
import jrm.profile.report.Report;
import jrm.security.Session;
import jrm.security.Sessions;

public class ReportLite extends Stage
{
	private ReportLiteController controller;
	
	private Session session;
	
	public ReportLite(Stage parent, Report report) throws IOException, URISyntaxException
	{
		super();
		session = Sessions.getSingleSession();
		initOwner(parent);
		initModality(Modality.NONE);
		getIcons().add(parent.getIcons().get(0));
		setOnShowing(e -> {
			Settings.fromJson(session.getUser().getSettings().getProperty("ReportLite.Bounds", null), this);
		});
		setOnCloseRequest(e -> {
			session.getUser().getSettings().setProperty("ReportLite.Bounds", Settings.toJson(this));
			hide();
		});
		final var loader = new FXMLLoader(getClass().getResource("ReportLite.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<BorderPane>load();
		controller = loader.getController();
		setScene(new Scene(root));
		controller.viewController.setReport(report);
		show();
	}
}
