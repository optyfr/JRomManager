package jrm.fx.ui.profile.report;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.locale.Messages;
import jrm.profile.report.Report;
import lombok.Getter;

public class ReportFrame extends Stage
{
	private @Getter ReportViewController controller;

	public ReportFrame(Stage parent, Report report) throws IOException, URISyntaxException
	{
		super();
		initOwner(parent);
		initModality(Modality.APPLICATION_MODAL);
		getIcons().add(parent.getIcons().get(0));
		setOnShowing(e -> {
		});
		setOnCloseRequest(e -> {
			hide();
		});
		final var loader = new FXMLLoader(getClass().getResource("ReportView.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<ScrollPane>load();
		controller = loader.getController();
		controller.setReport(report);
		setScene(new Scene(root));
		sizeToScene();
		show();
	}
}
