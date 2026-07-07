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

/**
 * FXML controller for the main tabbed window.
 * <p>
 * Wires up the five main panels (profile, scanner, Dir2Dat, batch tools, settings)
 * and assigns tab icons.
 *
 * @since 2.5
 */
public class MainFrameController implements Initializable {
    /**
     * The main tab pane.
     *
     * @return the main tab pane
     */
    @FXML
    private @Getter TabPane tabPane;
    /** The profile panel container. */
    @FXML
    private BorderPane profilePanel;
    /** The Dir2Dat panel container. */
    @FXML
    private GridPane dir2datPanel;
    /** The batch tools panel container. */
    @FXML
    private TabPane batchtoolsPanel;
    /** The settings panel container. */
    @FXML
    private ScrollPane settingsPanel;

    /** The profile panel tab. */
    @FXML
    private Tab profilePanelTab;
    /**
     * The scanner panel tab.
     *
     * @return the scanner panel tab
     */
    @FXML
    private @Getter Tab scannerPanelTab;
    /** The Dir2Dat panel tab. */
    @FXML
    private Tab dir2datPanelTab;
    /** The batch tools panel tab. */
    @FXML
    private Tab batchtoolsPanelTab;
    /** The settings panel tab. */
    @FXML
    private Tab settingsPanelTab;

    /**
     * The profile panel controller.
     *
     * @return the profile panel controller
     */
    @FXML
    private @Getter ProfilePanelController profilePanelController;
    /** The scanner panel controller. */
    @FXML
    private ScannerPanelController scannerPanelController;
    /**
     * The settings panel controller.
     *
     * @return the settings panel controller
     */
    @FXML
    private @Getter SettingsPanelController settingsPanelController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
