package jrm.fx.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import lombok.Getter;

public class MainFrameController implements Initializable
{
	@FXML private @Getter TabPane tabPane;
	@FXML private BorderPane profilePanel;
	@FXML private GridPane dir2datPanel;
	@FXML private TabPane batchtoolsPanel;
	@FXML private TabPane settingsPanel;
	
	@FXML private Tab profilePanelTab;
	@FXML private @Getter Tab scannerPanelTab;
	@FXML private Tab dir2datPanelTab;
	@FXML private Tab batchtoolsPanelTab;
	@FXML private Tab settingsPanelTab;
	
	@FXML private @Getter ProfilePanelController profilePanelController ;
	@FXML private ScannerPanelController scannerPanelController ;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		profilePanelTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/script.png")));
		scannerPanelTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/drive_magnify.png")));
		scannerPanelTab.setDisable(true);
		dir2datPanelTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/drive_go.png")));
		batchtoolsPanelTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/application_osx_terminal.png")));
		settingsPanelTab.setGraphic(new ImageView(MainFrame.getIcon("/jrm/resicons/icons/cog.png")));
		profilePanelController.setProfileLoader(scannerPanelController);
	}

}
