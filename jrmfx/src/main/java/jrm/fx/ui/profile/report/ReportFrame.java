package jrm.fx.ui.profile.report;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.aui.progress.StatusHandler;
import jrm.fx.ui.misc.Settings;
import jrm.fx.ui.status.NeutralToNodeFormatter;
import jrm.locale.Messages;
import jrm.security.Session;
import jrm.security.Sessions;

public class ReportFrame extends Stage implements StatusHandler
{
	private ReportFrameController controller;
	
	private Session session;
	
	public ReportFrame(Stage parent) throws IOException, URISyntaxException
	{
		super();
		session = Sessions.getSingleSession();
		initOwner(parent);
		initModality(Modality.NONE);
		getIcons().add(parent.getIcons().get(0));
		setOnShowing(e -> Settings.fromJson(session.getUser().getSettings().getProperty("ReportFrame.Bounds", null), this));
		setOnCloseRequest(e -> {
			session.getUser().getSettings().setProperty("ReportFrame.Bounds", Settings.toJson(this));
			hide();
		});
		final var loader = new FXMLLoader(getClass().getResource("ReportFrame.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<BorderPane>load();
		controller = loader.getController();
		setScene(new Scene(root));
		session.getReport().setStatusHandler(this);
	}
	
	private String status = "";
	
	@Override
	public void setStatus(String text)
	{
		status = text;
	}
	
	private boolean needUpdate = false;
	
	public void setNeedUpdate(boolean needUpdate)
	{
		this.needUpdate = needUpdate;
		if(isShowing())
			update();
	}

	private void update()
	{
		if(needUpdate)
		{
			controller.viewController.setReport(Sessions.getSingleSession().getReport());
			controller.status.getChildren().setAll(NeutralToNodeFormatter.toNodes(status));
			needUpdate = false;
		}
	}
	
	public void setVisible()
	{
		update();
		super.show();
	}
}
