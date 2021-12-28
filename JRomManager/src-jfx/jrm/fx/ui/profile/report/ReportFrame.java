package jrm.fx.ui.profile.report;

import java.io.IOException;
import java.net.URISyntaxException;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jrm.aui.progress.StatusHandler;
import jrm.fx.ui.web.HTMLFormatter;
import jrm.locale.Messages;
import jrm.security.Sessions;

public class ReportFrame extends Stage implements StatusHandler
{
	private ReportFrameController controller;
	
	public ReportFrame(Stage parent) throws IOException, URISyntaxException
	{
		super();
		initOwner(parent);
		initModality(Modality.NONE);
		getIcons().add(parent.getIcons().get(0));
		setOnShowing(e -> {
		});
		setOnCloseRequest(e -> {
			hide();
		});
		final var loader = new FXMLLoader(getClass().getResource("ReportFrame.fxml").toURI().toURL(), Messages.getBundle());
		final var root = loader.<BorderPane>load();
		controller = loader.getController();
		setScene(new Scene(root));
		Sessions.getSingleSession().getReport().setStatusHandler(this);
//		sizeToScene();
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
			controller.status.getEngine().loadContent(HTMLFormatter.toHTML(status));
			needUpdate = false;
		}
	}
	
	public void setVisible()
	{
		update();
		super.show();
	}
}
