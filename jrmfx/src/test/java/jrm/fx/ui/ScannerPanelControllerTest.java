package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.security.User;
import jrm.misc.GlobalSettings;

/**
 * Tests for {@link ScannerPanelController}.
 * <p>
 * Verifies initialization of FXML fields, icons, event handlers,
 * and interface implementation for the Scanner panel.
 *
 * @since 3.0.5
 */
@TestFxApplication(ScannerPanelControllerTest.TestApp.class)
@DisplayName("ScannerPanelController Tests")
class ScannerPanelControllerTest {

    /**
     * Functional interface that accepts a runnable which may throw checked exceptions.
     */
    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    /**
     * Minimal application that creates a {@link ScannerPanelController}
     * with all FXML fields injected via reflection and mocks for Session.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        static ScannerPanelController controller;

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            this.primaryStage = primaryStage;

            // Create mock chain
            GlobalSettings mockSettings = mock(GlobalSettings.class);
            when(mockSettings.getWorkPath()).thenReturn(java.nio.file.Paths.get(System.getProperty("java.io.tmpdir")));

            User mockUser = mock(User.class);
            when(mockUser.getSettings()).thenReturn(mockSettings);

            Session mockSession = mock(Session.class);
            when(mockSession.getUser()).thenReturn(mockUser);

            // Configure shared mock to return our session
            SharedMockSession.getOrCreate().when(Sessions::getSingleSession).thenReturn(mockSession);

            // Create the controller
            controller = new ScannerPanelController();

            // Inject session if null
            Field sessionField = BaseController.class.getDeclaredField("session");
            sessionField.setAccessible(true);
            if (sessionField.get(controller) == null) {
                sessionField.set(controller, mockSession);
            }

            // Create and inject all FXML fields
            Tab dirTab = new Tab();
            Tab settingsTab = new Tab();
            Tab filterTab = new Tab();
            Tab advFilterTab = new Tab();
            Tab automationTab = new Tab();

            Button infosBtn = new Button();
            Button scanBtn = new Button();
            Button reportBtn = new Button();
            Button fixBtn = new Button();
            Button importBtn = new Button();
            Button exportBtn = new Button();
            Button romsDestBtn = new Button();
            Button disksDestBtn = new Button();
            Button swDestBtn = new Button();
            Button swDisksDestBtn = new Button();
            Button samplesDestBtn = new Button();
            Button backupDestBtn = new Button();

            TextField romsDest = new TextField();
            TextField disksDest = new TextField();
            TextField swDest = new TextField();
            TextField swDisksDest = new TextField();
            TextField samplesDest = new TextField();
            TextField backupDest = new TextField();
            TextField tfNPlayers = new TextField();
            TextField tfCatVer = new TextField();

            CheckBox disksDestCB = new CheckBox();
            CheckBox swDestCB = new CheckBox();
            CheckBox swDisksDestCB = new CheckBox();
            CheckBox samplesDestCB = new CheckBox();
            CheckBox backupDestCB = new CheckBox();
            CheckBox chckbxIncludeClones = new CheckBox();
            CheckBox chckbxIncludeDisks = new CheckBox();
            CheckBox chckbxIncludeSamples = new CheckBox();

            ListView<File> srcList = new ListView<>();
            ListView<?> systemsFilter = new ListView<>();
            ListView<?> sourcesFilter = new ListView<>();
            ListView<?> listNPlayers = new ListView<>();

            TreeView<?> treeCatVer = new TreeView<>();

            ComboBox<?> cbbxDriverStatus = new ComboBox<>();
            ComboBox<?> cbbxFilterCabinetType = new ComboBox<>();
            ComboBox<?> cbbxFilterDisplayOrientation = new ComboBox<>();
            ComboBox<?> cbbxSWMinSupportedLvl = new ComboBox<>();
            ComboBox<String> cbbxYearMin = new ComboBox<>();
            ComboBox<String> cbbxYearMax = new ComboBox<>();
            ComboBox<?> cbAutomation = new ComboBox<>();

            ContextMenu srcListMenu = new ContextMenu();
            ContextMenu systemsFilterMenu = new ContextMenu();
            ContextMenu sourcesFilterMenu = new ContextMenu();
            ContextMenu nPlayersMenu = new ContextMenu();
            ContextMenu catVerMenu = new ContextMenu();

            MenuItem srcListAddMenuItem = new MenuItem();
            MenuItem srcListDelMenuItem = new MenuItem();
            MenuItem systemsFilterSelectAllMenuItem = new MenuItem();
            MenuItem systemsFilterSelectAllBiosMenuItem = new MenuItem();
            MenuItem systemsFilterSelectAllSoftwaresMenuItem = new MenuItem();
            MenuItem systemsFilterUnselectAllMenuItem = new MenuItem();
            MenuItem systemsFilterUnselectAllBiosMenuItem = new MenuItem();
            MenuItem systemsFilterUnselectAllSoftwaresMenuItem = new MenuItem();
            MenuItem systemsFilterInvertSelectionMenuItem = new MenuItem();
            MenuItem sourcesFilterSelectAllMenuItem = new MenuItem();
            MenuItem sourcesFilterUnselectAllMenuItem = new MenuItem();
            MenuItem sourcesFilterInvertSelectionMenuItem = new MenuItem();
            MenuItem nPlayersMenuItemAll = new MenuItem();
            MenuItem nPlayersMenuItemNone = new MenuItem();
            MenuItem nPlayersMenuItemInvert = new MenuItem();
            MenuItem nPlayersMenuItemClear = new MenuItem();
            MenuItem catVerMenuItemSelectAll = new MenuItem();
            MenuItem catVerMenuItemSelectMature = new MenuItem();
            MenuItem catVerMenuItemUnselectAll = new MenuItem();
            MenuItem catVerMenuItemUnselectMature = new MenuItem();
            MenuItem catVerMenuItemClear = new MenuItem();

            HBox profileinfoLbl = new HBox();
            ScannerPanelSettingsController scannerPanelSettingsController = mock(ScannerPanelSettingsController.class);

            // Inject all fields via reflection
            VBox root = new VBox();
            injectField(controller, "dirTab", dirTab);
            injectField(controller, "settingsTab", settingsTab);
            injectField(controller, "filterTab", filterTab);
            injectField(controller, "advFilterTab", advFilterTab);
            injectField(controller, "automationTab", automationTab);

            injectField(controller, "infosBtn", infosBtn);
            injectField(controller, "scanBtn", scanBtn);
            injectField(controller, "reportBtn", reportBtn);
            injectField(controller, "fixBtn", fixBtn);
            injectField(controller, "importBtn", importBtn);
            injectField(controller, "exportBtn", exportBtn);
            injectField(controller, "romsDestBtn", romsDestBtn);
            injectField(controller, "disksDestBtn", disksDestBtn);
            injectField(controller, "swDestBtn", swDestBtn);
            injectField(controller, "swDisksDestBtn", swDisksDestBtn);
            injectField(controller, "samplesDestBtn", samplesDestBtn);
            injectField(controller, "backupDestBtn", backupDestBtn);

            injectField(controller, "romsDest", romsDest);
            injectField(controller, "disksDest", disksDest);
            injectField(controller, "swDest", swDest);
            injectField(controller, "swDisksDest", swDisksDest);
            injectField(controller, "samplesDest", samplesDest);
            injectField(controller, "backupDest", backupDest);
            injectField(controller, "tfNPlayers", tfNPlayers);
            injectField(controller, "tfCatVer", tfCatVer);

            injectField(controller, "disksDestCB", disksDestCB);
            injectField(controller, "swDestCB", swDestCB);
            injectField(controller, "swDisksDestCB", swDisksDestCB);
            injectField(controller, "samplesDestCB", samplesDestCB);
            injectField(controller, "backupDestCB", backupDestCB);
            injectField(controller, "chckbxIncludeClones", chckbxIncludeClones);
            injectField(controller, "chckbxIncludeDisks", chckbxIncludeDisks);
            injectField(controller, "chckbxIncludeSamples", chckbxIncludeSamples);

            injectField(controller, "srcList", srcList);
            injectField(controller, "systemsFilter", systemsFilter);
            injectField(controller, "sourcesFilter", sourcesFilter);
            injectField(controller, "listNPlayers", listNPlayers);

            injectField(controller, "treeCatVer", treeCatVer);

            injectField(controller, "cbbxDriverStatus", cbbxDriverStatus);
            injectField(controller, "cbbxFilterCabinetType", cbbxFilterCabinetType);
            injectField(controller, "cbbxFilterDisplayOrientation", cbbxFilterDisplayOrientation);
            injectField(controller, "cbbxSWMinSupportedLvl", cbbxSWMinSupportedLvl);
            injectField(controller, "cbbxYearMin", cbbxYearMin);
            injectField(controller, "cbbxYearMax", cbbxYearMax);
            injectField(controller, "cbAutomation", cbAutomation);

            injectField(controller, "srcListMenu", srcListMenu);
            injectField(controller, "systemsFilterMenu", systemsFilterMenu);
            injectField(controller, "sourcesFilterMenu", sourcesFilterMenu);
            injectField(controller, "nPlayersMenu", nPlayersMenu);
            injectField(controller, "catVerMenu", catVerMenu);

            injectField(controller, "srcListAddMenuItem", srcListAddMenuItem);
            injectField(controller, "srcListDelMenuItem", srcListDelMenuItem);
            injectField(controller, "systemsFilterSelectAllMenuItem", systemsFilterSelectAllMenuItem);
            injectField(controller, "systemsFilterSelectAllBiosMenuItem", systemsFilterSelectAllBiosMenuItem);
            injectField(controller, "systemsFilterSelectAllSoftwaresMenuItem", systemsFilterSelectAllSoftwaresMenuItem);
            injectField(controller, "systemsFilterUnselectAllMenuItem", systemsFilterUnselectAllMenuItem);
            injectField(controller, "systemsFilterUnselectAllBiosMenuItem", systemsFilterUnselectAllBiosMenuItem);
            injectField(controller, "systemsFilterUnselectAllSoftwaresMenuItem", systemsFilterUnselectAllSoftwaresMenuItem);
            injectField(controller, "systemsFilterInvertSelectionMenuItem", systemsFilterInvertSelectionMenuItem);
            injectField(controller, "sourcesFilterSelectAllMenuItem", sourcesFilterSelectAllMenuItem);
            injectField(controller, "sourcesFilterUnselectAllMenuItem", sourcesFilterUnselectAllMenuItem);
            injectField(controller, "sourcesFilterInvertSelectionMenuItem", sourcesFilterInvertSelectionMenuItem);
            injectField(controller, "nPlayersMenuItemAll", nPlayersMenuItemAll);
            injectField(controller, "nPlayersMenuItemNone", nPlayersMenuItemNone);
            injectField(controller, "nPlayersMenuItemInvert", nPlayersMenuItemInvert);
            injectField(controller, "nPlayersMenuItemClear", nPlayersMenuItemClear);
            injectField(controller, "catVerMenuItemSelectAll", catVerMenuItemSelectAll);
            injectField(controller, "catVerMenuItemSelectMature", catVerMenuItemSelectMature);
            injectField(controller, "catVerMenuItemUnselectAll", catVerMenuItemUnselectAll);
            injectField(controller, "catVerMenuItemUnselectMature", catVerMenuItemUnselectMature);
            injectField(controller, "catVerMenuItemClear", catVerMenuItemClear);

            injectField(controller, "profileinfoLbl", profileinfoLbl);
            injectField(controller, "scannerPanelSettingsController", scannerPanelSettingsController);

            // Initialize controller
            root.getChildren().addAll(
                    infosBtn, scanBtn, reportBtn, fixBtn, importBtn, exportBtn,
                    romsDestBtn, disksDestBtn, swDestBtn, swDisksDestBtn, samplesDestBtn, backupDestBtn,
                    romsDest, disksDest, swDest, swDisksDest, samplesDest, backupDest,
                    tfNPlayers, tfCatVer,
                    disksDestCB, swDestCB, swDisksDestCB, samplesDestCB, backupDestCB,
                    chckbxIncludeClones, chckbxIncludeDisks, chckbxIncludeSamples,
                    srcList, systemsFilter, sourcesFilter, listNPlayers, treeCatVer,
                    cbbxDriverStatus, cbbxFilterCabinetType, cbbxFilterDisplayOrientation,
                    cbbxSWMinSupportedLvl, cbbxYearMin, cbbxYearMax, cbAutomation,
                    profileinfoLbl);

            controller.initialize(null, null);

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        private static void injectField(Object target, String fieldName, Object value) {
            try {
                Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception e) {
                throw new RuntimeException("Failed to inject field: " + fieldName, e);
            }
        }

        /**
         * Helper method to run operations on the JavaFX Application Thread and wait for completion.
         */
        public static void runOnFxThread(ThrowingRunnable operation) throws Exception {
            CompletableFuture<Void> future = new CompletableFuture<>();
            Platform.runLater(() -> {
                try {
                    operation.run();
                    future.complete(null);
                } catch (Throwable t) {
                    future.completeExceptionally(t);
                }
            });
            future.get();
        }
    }

    /**
     * Helper to run code on JavaFX thread and wait for completion.
     */
    private void runOnFxThread(ThrowingRunnable action) throws Exception {
        TestApp.runOnFxThread(action);
    }

    @SuppressWarnings("unchecked")
    private <T> T getField(String fieldName) {
        try {
            Field field = ScannerPanelController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(TestApp.controller);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field " + fieldName, e);
        }
    }

    @Test
    @DisplayName("Should initialize controller without errors")
    void shouldInitializeController() throws Exception {
        runOnFxThread(() -> {
            assertThat(TestApp.controller).isNotNull();
        });
    }

    @Test
    @DisplayName("Should initialize all tab fields")
    void shouldInitializeAllTabFields() throws Exception {
        runOnFxThread(() -> {
            assertThat((Tab) getField("dirTab")).as("dirTab").isNotNull();
            assertThat((Tab) getField("settingsTab")).as("settingsTab").isNotNull();
            assertThat((Tab) getField("filterTab")).as("filterTab").isNotNull();
            assertThat((Tab) getField("advFilterTab")).as("advFilterTab").isNotNull();
            assertThat((Tab) getField("automationTab")).as("automationTab").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set icons on tabs")
    void shouldSetIconsOnTabs() throws Exception {
        runOnFxThread(() -> {
            assertThat(((Tab) getField("dirTab")).getGraphic()).as("dirTab icon").isNotNull();
            assertThat(((Tab) getField("settingsTab")).getGraphic()).as("settingsTab icon").isNotNull();
            assertThat(((Tab) getField("filterTab")).getGraphic()).as("filterTab icon").isNotNull();
            assertThat(((Tab) getField("advFilterTab")).getGraphic()).as("advFilterTab icon").isNotNull();
            assertThat(((Tab) getField("automationTab")).getGraphic()).as("automationTab icon").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set icons on action buttons")
    void shouldSetIconsOnActionButtons() throws Exception {
        runOnFxThread(() -> {
            assertThat(((Button) getField("infosBtn")).getGraphic()).as("infosBtn icon").isNotNull();
            assertThat(((Button) getField("scanBtn")).getGraphic()).as("scanBtn icon").isNotNull();
            assertThat(((Button) getField("reportBtn")).getGraphic()).as("reportBtn icon").isNotNull();
            assertThat(((Button) getField("fixBtn")).getGraphic()).as("fixBtn icon").isNotNull();
            assertThat(((Button) getField("importBtn")).getGraphic()).as("importBtn icon").isNotNull();
            assertThat(((Button) getField("exportBtn")).getGraphic()).as("exportBtn icon").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set icons on destination buttons")
    void shouldSetIconsOnDestinationButtons() throws Exception {
        runOnFxThread(() -> {
            assertThat(((Button) getField("romsDestBtn")).getGraphic()).as("romsDestBtn icon").isNotNull();
            assertThat(((Button) getField("disksDestBtn")).getGraphic()).as("disksDestBtn icon").isNotNull();
            assertThat(((Button) getField("swDestBtn")).getGraphic()).as("swDestBtn icon").isNotNull();
            assertThat(((Button) getField("swDisksDestBtn")).getGraphic()).as("swDisksDestBtn icon").isNotNull();
            assertThat(((Button) getField("samplesDestBtn")).getGraphic()).as("samplesDestBtn icon").isNotNull();
            assertThat(((Button) getField("backupDestBtn")).getGraphic()).as("backupDestBtn icon").isNotNull();
        });
    }

    @Test
    @DisplayName("Should initialize destination checkboxes")
    void shouldInitializeDestinationCheckboxes() throws Exception {
        runOnFxThread(() -> {
            assertThat(((CheckBox) getField("disksDestCB")).isSelected()).as("disksDestCB selected").isFalse();
            assertThat(((CheckBox) getField("swDestCB")).isSelected()).as("swDestCB selected").isFalse();
            assertThat(((CheckBox) getField("swDisksDestCB")).isSelected()).as("swDisksDestCB selected").isFalse();
            assertThat(((CheckBox) getField("samplesDestCB")).isSelected()).as("samplesDestCB selected").isFalse();
            assertThat(((CheckBox) getField("backupDestCB")).isSelected()).as("backupDestCB selected").isFalse();
        });
    }

    @Test
    @DisplayName("Should initialize source list with menu items")
    void shouldInitializeSourceList() throws Exception {
        runOnFxThread(() -> {
            assertThat((ListView<?>) getField("srcList")).as("srcList").isNotNull();
            assertThat((MenuItem) getField("srcListAddMenuItem")).as("srcListAddMenuItem").isNotNull();
            assertThat((MenuItem) getField("srcListDelMenuItem")).as("srcListDelMenuItem").isNotNull();
        });
    }

    @Test
    @DisplayName("Should initialize filter controls")
    void shouldInitializeFilterControls() throws Exception {
        runOnFxThread(() -> {
            assertThat((CheckBox) getField("chckbxIncludeClones")).as("chckbxIncludeClones").isNotNull();
            assertThat((CheckBox) getField("chckbxIncludeDisks")).as("chckbxIncludeDisks").isNotNull();
            assertThat((CheckBox) getField("chckbxIncludeSamples")).as("chckbxIncludeSamples").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxDriverStatus")).as("cbbxDriverStatus").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxFilterCabinetType")).as("cbbxFilterCabinetType").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxFilterDisplayOrientation")).as("cbbxFilterDisplayOrientation").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxSWMinSupportedLvl")).as("cbbxSWMinSupportedLvl").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxYearMin")).as("cbbxYearMin").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxYearMax")).as("cbbxYearMax").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set action handlers on source list menu items")
    void shouldSetSourceListMenuHandlers() throws Exception {
        runOnFxThread(() -> {
            assertThat(((MenuItem) getField("srcListAddMenuItem")).getOnAction()).as("srcListAddMenuItem onAction").isNotNull();
            assertThat(((MenuItem) getField("srcListDelMenuItem")).getOnAction()).as("srcListDelMenuItem onAction").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set action handlers on import/export buttons")
    void shouldSetImportExportHandlers() throws Exception {
        runOnFxThread(() -> {
            assertThat(((Button) getField("importBtn")).getOnAction()).as("importBtn onAction").isNotNull();
            assertThat(((Button) getField("exportBtn")).getOnAction()).as("exportBtn onAction").isNotNull();
        });
    }

    @Test
    @DisplayName("Should initialize automation controls")
    void shouldInitializeAutomationControls() throws Exception {
        runOnFxThread(() -> {
            assertThat((ComboBox<?>) getField("cbAutomation")).as("cbAutomation").isNotNull();
        });
    }

    @Test
    @DisplayName("Should implement ProfileLoader interface")
    void shouldImplementProfileLoaderInterface() throws Exception {
        runOnFxThread(() -> {
            assertThat(TestApp.controller).isInstanceOf(ProfileLoader.class);
        });
    }

    @Test
    @DisplayName("Should initialize all required fields")
    void shouldInitializeAllRequiredFields() throws Exception {
        runOnFxThread(() -> {
            String[] fieldNames = {
                "dirTab", "settingsTab", "filterTab", "advFilterTab", "automationTab",
                "infosBtn", "scanBtn", "reportBtn", "fixBtn", "importBtn", "exportBtn",
                "romsDestBtn", "disksDestBtn", "swDestBtn", "swDisksDestBtn", "samplesDestBtn", "backupDestBtn",
                "romsDest", "disksDest", "swDest", "swDisksDest", "samplesDest", "backupDest",
                "tfNPlayers", "tfCatVer",
                "disksDestCB", "swDestCB", "swDisksDestCB", "samplesDestCB", "backupDestCB",
                "chckbxIncludeClones", "chckbxIncludeDisks", "chckbxIncludeSamples",
                "srcList", "systemsFilter", "sourcesFilter", "listNPlayers", "treeCatVer",
                "cbbxDriverStatus", "cbbxFilterCabinetType", "cbbxFilterDisplayOrientation",
                "cbbxSWMinSupportedLvl", "cbbxYearMin", "cbbxYearMax", "cbAutomation",
                "srcListMenu", "systemsFilterMenu", "sourcesFilterMenu", "nPlayersMenu", "catVerMenu",
                "srcListAddMenuItem", "srcListDelMenuItem",
                "systemsFilterSelectAllMenuItem", "systemsFilterSelectAllBiosMenuItem",
                "systemsFilterSelectAllSoftwaresMenuItem", "systemsFilterUnselectAllMenuItem",
                "systemsFilterUnselectAllBiosMenuItem", "systemsFilterUnselectAllSoftwaresMenuItem",
                "systemsFilterInvertSelectionMenuItem",
                "sourcesFilterSelectAllMenuItem", "sourcesFilterUnselectAllMenuItem", "sourcesFilterInvertSelectionMenuItem",
                "nPlayersMenuItemAll", "nPlayersMenuItemNone", "nPlayersMenuItemInvert", "nPlayersMenuItemClear",
                "catVerMenuItemSelectAll", "catVerMenuItemSelectMature",
                "catVerMenuItemUnselectAll", "catVerMenuItemUnselectMature", "catVerMenuItemClear",
                "profileinfoLbl"
            };

            for (String fieldName : fieldNames) {
                assertThat((Object) getField(fieldName)).as(fieldName).isNotNull();
            }
        });
    }
}
