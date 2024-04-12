package jrm.fx.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
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
	@FXML private ScrollPane settingsPanel;
	
	@FXML private Tab profilePanelTab;
	@FXML private @Getter Tab scannerPanelTab;
	@FXML private Tab dir2datPanelTab;
	@FXML private Tab batchtoolsPanelTab;
	@FXML private Tab settingsPanelTab;
	
	@FXML private @Getter ProfilePanelController profilePanelController ;
	@FXML private ScannerPanelController scannerPanelController ;
	@FXML private @Getter SettingsPanelController settingsPanelController ;
	
	@Override
	public void initialize(URL location, ResourceBundle resources)
	{
		ImageView script = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/script.png"));
		script.setPreserveRatio(true);
		script.getStyleClass().add("icon");
		profilePanelTab.setGraphic(script);
		ImageView magnify = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/drive_magnify.png"));
		magnify.setPreserveRatio(true);
		magnify.getStyleClass().add("icon");
		scannerPanelTab.setGraphic(magnify);
		scannerPanelTab.setDisable(true);
		ImageView go = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/drive_go.png"));
		go.setPreserveRatio(true);
		go.getStyleClass().add("icon");
		dir2datPanelTab.setGraphic(go);
		ImageView terminal = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/application_osx_terminal.png"));
		terminal.setPreserveRatio(true);
		terminal.getStyleClass().add("icon");
		batchtoolsPanelTab.setGraphic(terminal);
		ImageView cog = new ImageView(MainFrame.getIcon("/jrm/resicons/icons/cog.png"));
		cog.setPreserveRatio(true);
		cog.getStyleClass().add("icon");
		settingsPanelTab.setGraphic(cog);
		profilePanelController.setProfileLoader(scannerPanelController);
	}

}
