package jrm.fx.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import lombok.Getter;

public class MainFrameController implements Initializable
{
	@FXML private @Getter TabPane tabPane;
	@FXML private BorderPane profilePanel;
	@FXML private Tab profilePanelTab;
	@FXML private @Getter Tab scannerPanelTab;
	
	@FXML private @Getter ProfilePanelController profilePanelController ;
	@FXML private ScannerPanelController scannerPanelController ;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		profilePanelTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/script.png")));
		scannerPanelTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/drive_magnify.png")));
		scannerPanelTab.setDisable(true);
		profilePanelController.setProfileLoader(scannerPanelController);
	}

}
