package jrm.fx.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import jrm.security.Session;
import jrm.security.Sessions;

public class MainFrameController implements Initializable
{
	@FXML private TabPane tabPane;
	@FXML private BorderPane profilePanel;
	@FXML private Tab profilePanelTab;
	@FXML private Tab scannerPanelTab;
	
	@FXML private ProfilePanelController profilePanelController ;
	
	private Session session;
	

	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		session = Sessions.getSingleSession();
		profilePanelTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/script.png")));
		scannerPanelTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/drive_magnify.png")));
	}

}
