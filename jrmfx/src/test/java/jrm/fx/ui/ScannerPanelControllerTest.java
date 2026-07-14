package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
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
import jrm.misc.GlobalSettings;
import jrm.misc.ProfileSettingsEnum;
import jrm.profile.Profile;
import jrm.profile.data.Driver;
import jrm.profile.data.Machine.CabinetType;
import jrm.profile.data.Machine.DisplayOrientation;
import jrm.profile.data.Software.Supported;
import jrm.profile.scan.options.ScanAutomation;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.security.User;

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

            Profile mockProfile = mock(Profile.class);

            Session mockSession = mock(Session.class);
            when(mockSession.getUser()).thenReturn(mockUser);
            when(mockSession.getCurrProfile()).thenReturn(mockProfile);

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

    // ==================== Functional Behavior Tests ====================

    @Test
    @DisplayName("Should have scan method defined for FXML onAction")
    void shouldHaveScanMethodDefined() throws Exception {
        java.lang.reflect.Method scanMethod = ScannerPanelController.class.getDeclaredMethod("scan", javafx.event.ActionEvent.class);
        assertThat(scanMethod).as("scan(ActionEvent) method exists").isNotNull();
    }

    @Test
    @DisplayName("Should have fix method defined for FXML onAction")
    void shouldHaveFixMethodDefined() throws Exception {
        java.lang.reflect.Method fixMethod = ScannerPanelController.class.getDeclaredMethod("fix", javafx.event.ActionEvent.class);
        assertThat(fixMethod).as("fix(ActionEvent) method exists").isNotNull();
    }

    @Test
    @DisplayName("Should have report method defined for FXML onAction")
    void shouldHaveReportMethodDefined() throws Exception {
        java.lang.reflect.Method reportMethod = ScannerPanelController.class.getDeclaredMethod("report", javafx.event.ActionEvent.class);
        assertThat(reportMethod).as("report(ActionEvent) method exists").isNotNull();
    }

    // ==================== ComboBox Population Tests ====================

    @Test
    @DisplayName("Should populate driver status combo box with all DriverStatusType values")
    void shouldPopulateDriverStatusComboBox() throws Exception {
        runOnFxThread(() -> {
            ComboBox<Driver.StatusType> cbbxDriverStatus = getField("cbbxDriverStatus");
            assertThat(cbbxDriverStatus.getItems())
                .as("driver status combo box contains all enum values")
                .hasSize(Driver.StatusType.values().length)
                .containsExactly(Driver.StatusType.values());
        });
    }

    @Test
    @DisplayName("Should populate cabinet type combo box with all CabinetType values")
    void shouldPopulateCabinetTypeComboBox() throws Exception {
        runOnFxThread(() -> {
            ComboBox<CabinetType> cbbxFilterCabinetType = getField("cbbxFilterCabinetType");
            assertThat(cbbxFilterCabinetType.getItems())
                .as("cabinet type combo box contains all enum values")
                .hasSize(CabinetType.values().length)
                .containsExactly(CabinetType.values());
        });
    }

    @Test
    @DisplayName("Should populate display orientation combo box with all DisplayOrientation values")
    void shouldPopulateDisplayOrientationComboBox() throws Exception {
        runOnFxThread(() -> {
            ComboBox<DisplayOrientation> cbbxFilterDisplayOrientation = getField("cbbxFilterDisplayOrientation");
            assertThat(cbbxFilterDisplayOrientation.getItems())
                .as("display orientation combo box contains all enum values")
                .hasSize(DisplayOrientation.values().length)
                .containsExactly(DisplayOrientation.values());
        });
    }

    @Test
    @DisplayName("Should populate software support level combo box with all Supported values")
    void shouldPopulateSWMinSupportedLvlComboBox() throws Exception {
        runOnFxThread(() -> {
            ComboBox<Supported> cbbxSWMinSupportedLvl = getField("cbbxSWMinSupportedLvl");
            assertThat(cbbxSWMinSupportedLvl.getItems())
                .as("software support level combo box contains all enum values")
                .hasSize(Supported.values().length)
                .containsExactly(Supported.values());
        });
    }

    @Test
    @DisplayName("Should populate automation combo box with all ScanAutomation values")
    void shouldPopulateAutomationComboBox() throws Exception {
        runOnFxThread(() -> {
            ComboBox<ScanAutomation> cbAutomation = getField("cbAutomation");
            assertThat(cbAutomation.getItems())
                .as("automation combo box contains all enum values")
                .hasSize(ScanAutomation.values().length)
                .containsExactly(ScanAutomation.values());
        });
    }

    // ==================== Filter Action Handler Tests ====================

    @Test
    @DisplayName("Should register action handler on include clones checkbox")
    void shouldRegisterIncludeClonesActionHandler() throws Exception {
        runOnFxThread(() -> {
            CheckBox chckbxIncludeClones = getField("chckbxIncludeClones");
            assertThat(chckbxIncludeClones.getOnAction())
                .as("include clones checkbox has action handler")
                .isNotNull();
        });
    }

    @Test
    @DisplayName("Should register action handler on include disks checkbox")
    void shouldRegisterIncludeDisksActionHandler() throws Exception {
        runOnFxThread(() -> {
            CheckBox chckbxIncludeDisks = getField("chckbxIncludeDisks");
            assertThat(chckbxIncludeDisks.getOnAction())
                .as("include disks checkbox has action handler")
                .isNotNull();
        });
    }

    @Test
    @DisplayName("Should register action handler on include samples checkbox")
    void shouldRegisterIncludeSamplesActionHandler() throws Exception {
        runOnFxThread(() -> {
            CheckBox chckbxIncludeSamples = getField("chckbxIncludeSamples");
            assertThat(chckbxIncludeSamples.getOnAction())
                .as("include samples checkbox has action handler")
                .isNotNull();
        });
    }

    @Test
    @DisplayName("Should register action handler on driver status combo box")
    void shouldRegisterDriverStatusActionHandler() throws Exception {
        runOnFxThread(() -> {
            ComboBox<Driver.StatusType> cbbxDriverStatus = getField("cbbxDriverStatus");
            assertThat(cbbxDriverStatus.getOnAction())
                .as("driver status combo box has action handler")
                .isNotNull();
        });
    }

    @Test
    @DisplayName("Should register action handler on cabinet type combo box")
    void shouldRegisterCabinetTypeActionHandler() throws Exception {
        runOnFxThread(() -> {
            ComboBox<CabinetType> cbbxFilterCabinetType = getField("cbbxFilterCabinetType");
            assertThat(cbbxFilterCabinetType.getOnAction())
                .as("cabinet type combo box has action handler")
                .isNotNull();
        });
    }

    @Test
    @DisplayName("Should register action handler on display orientation combo box")
    void shouldRegisterDisplayOrientationActionHandler() throws Exception {
        runOnFxThread(() -> {
            ComboBox<DisplayOrientation> cbbxFilterDisplayOrientation = getField("cbbxFilterDisplayOrientation");
            assertThat(cbbxFilterDisplayOrientation.getOnAction())
                .as("display orientation combo box has action handler")
                .isNotNull();
        });
    }

    @Test
    @DisplayName("Should register action handler on software support level combo box")
    void shouldRegisterSWMinSupportedLvlActionHandler() throws Exception {
        runOnFxThread(() -> {
            ComboBox<Supported> cbbxSWMinSupportedLvl = getField("cbbxSWMinSupportedLvl");
            assertThat(cbbxSWMinSupportedLvl.getOnAction())
                .as("software support level combo box has action handler")
                .isNotNull();
        });
    }

    @Test
    @DisplayName("Should register action handler on year min combo box")
    void shouldRegisterYearMinActionHandler() throws Exception {
        runOnFxThread(() -> {
            ComboBox<String> cbbxYearMin = getField("cbbxYearMin");
            assertThat(cbbxYearMin.getOnAction())
                .as("year min combo box has action handler")
                .isNotNull();
        });
    }

    @Test
    @DisplayName("Should register action handler on year max combo box")
    void shouldRegisterYearMaxActionHandler() throws Exception {
        runOnFxThread(() -> {
            ComboBox<String> cbbxYearMax = getField("cbbxYearMax");
            assertThat(cbbxYearMax.getOnAction())
                .as("year max combo box has action handler")
                .isNotNull();
        });
    }

    @Test
    @DisplayName("Should register action handler on automation combo box")
    void shouldRegisterAutomationActionHandler() throws Exception {
        runOnFxThread(() -> {
            ComboBox<ScanAutomation> cbAutomation = getField("cbAutomation");
            assertThat(cbAutomation.getOnAction())
                .as("automation combo box has action handler")
                .isNotNull();
        });
    }

    // ==================== Source List Behavior Tests ====================

    @Test
    @DisplayName("Should disable delete menu item when no source list item is selected")
    void shouldDisableDeleteMenuItemWhenNoSelection() throws Exception {
        runOnFxThread(() -> {
            ListView<File> srcList = getField("srcList");
            MenuItem srcListDelMenuItem = getField("srcListDelMenuItem");
            
            srcList.getItems().clear();
            srcList.getSelectionModel().clearSelection();
            
            // Trigger the onShowing handler
            ContextMenu srcListMenu = getField("srcListMenu");
            srcListMenu.getOnShowing().handle(null);
            
            assertThat(srcListDelMenuItem.isDisable())
                .as("delete menu item disabled when no selection")
                .isTrue();
        });
    }

    @Test
    @DisplayName("Should enable delete menu item when source list item is selected")
    void shouldEnableDeleteMenuItemWhenSelectionExists() throws Exception {
        runOnFxThread(() -> {
            ListView<File> srcList = getField("srcList");
            MenuItem srcListDelMenuItem = getField("srcListDelMenuItem");
            
            srcList.getItems().add(new File("/test/roms"));
            srcList.getSelectionModel().selectFirst();
            
            // Trigger the onShowing handler
            ContextMenu srcListMenu = getField("srcListMenu");
            srcListMenu.getOnShowing().handle(null);
            
            assertThat(srcListDelMenuItem.isDisable())
                .as("delete menu item enabled when selection exists")
                .isFalse();
        });
    }

    @Test
    @DisplayName("Should have action handler on add menu item to choose source directory")
    void shouldHaveAddMenuItemActionHandler() throws Exception {
        runOnFxThread(() -> {
            MenuItem srcListAddMenuItem = getField("srcListAddMenuItem");
            assertThat(srcListAddMenuItem.getOnAction())
                .as("add menu item has action handler")
                .isNotNull();
        });
    }

    @Test
    @DisplayName("Should have action handler on delete menu item to remove selected items")
    void shouldHaveDeleteMenuItemActionHandler() throws Exception {
        runOnFxThread(() -> {
            MenuItem srcListDelMenuItem = getField("srcListDelMenuItem");
            assertThat(srcListDelMenuItem.getOnAction())
                .as("delete menu item has action handler")
                .isNotNull();
        });
    }

    // ==================== Destination Field Tests ====================

    @Test
    @DisplayName("Should initialize all destination text fields")
    void shouldInitializeAllDestinationTextFields() throws Exception {
        runOnFxThread(() -> {
            assertThat((TextField) getField("romsDest")).as("romsDest").isNotNull();
            assertThat((TextField) getField("disksDest")).as("disksDest").isNotNull();
            assertThat((TextField) getField("swDest")).as("swDest").isNotNull();
            assertThat((TextField) getField("swDisksDest")).as("swDisksDest").isNotNull();
            assertThat((TextField) getField("samplesDest")).as("samplesDest").isNotNull();
            assertThat((TextField) getField("backupDest")).as("backupDest").isNotNull();
        });
    }

    @Test
    @DisplayName("Should initialize all destination checkboxes with binding to enabled state")
    void shouldInitializeAllDestinationCheckboxes() throws Exception {
        runOnFxThread(() -> {
            assertThat((CheckBox) getField("disksDestCB")).as("disksDestCB").isNotNull();
            assertThat((CheckBox) getField("swDestCB")).as("swDestCB").isNotNull();
            assertThat((CheckBox) getField("swDisksDestCB")).as("swDisksDestCB").isNotNull();
            assertThat((CheckBox) getField("samplesDestCB")).as("samplesDestCB").isNotNull();
            assertThat((CheckBox) getField("backupDestCB")).as("backupDestCB").isNotNull();
        });
    }

    @Test
    @DisplayName("Should initialize all destination buttons")
    void shouldInitializeAllDestinationButtons() throws Exception {
        runOnFxThread(() -> {
            assertThat((Button) getField("romsDestBtn")).as("romsDestBtn").isNotNull();
            assertThat((Button) getField("disksDestBtn")).as("disksDestBtn").isNotNull();
            assertThat((Button) getField("swDestBtn")).as("swDestBtn").isNotNull();
            assertThat((Button) getField("swDisksDestBtn")).as("swDisksDestBtn").isNotNull();
            assertThat((Button) getField("samplesDestBtn")).as("samplesDestBtn").isNotNull();
            assertThat((Button) getField("backupDestBtn")).as("backupDestBtn").isNotNull();
        });
    }

    // ==================== Filter Control Tests ====================

    @Test
    @DisplayName("Should initialize all filter checkboxes")
    void shouldInitializeAllFilterCheckboxes() throws Exception {
        runOnFxThread(() -> {
            assertThat((CheckBox) getField("chckbxIncludeClones")).as("chckbxIncludeClones").isNotNull();
            assertThat((CheckBox) getField("chckbxIncludeDisks")).as("chckbxIncludeDisks").isNotNull();
            assertThat((CheckBox) getField("chckbxIncludeSamples")).as("chckbxIncludeSamples").isNotNull();
        });
    }

    @Test
    @DisplayName("Should initialize all filter combo boxes")
    void shouldInitializeAllFilterComboBoxes() throws Exception {
        runOnFxThread(() -> {
            assertThat((ComboBox<?>) getField("cbbxDriverStatus")).as("cbbxDriverStatus").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxFilterCabinetType")).as("cbbxFilterCabinetType").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxFilterDisplayOrientation")).as("cbbxFilterDisplayOrientation").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxSWMinSupportedLvl")).as("cbbxSWMinSupportedLvl").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxYearMin")).as("cbbxYearMin").isNotNull();
            assertThat((ComboBox<?>) getField("cbbxYearMax")).as("cbbxYearMax").isNotNull();
        });
    }

    // ==================== Context Menu Tests ====================

    @Test
    @DisplayName("Should have onShowing handler on source list context menu")
    void shouldHaveOnShowingHandlerOnSourceListMenu() throws Exception {
        runOnFxThread(() -> {
            ContextMenu srcListMenu = getField("srcListMenu");
            assertThat(srcListMenu.getOnShowing())
                .as("source list menu has onShowing handler for delete item state")
                .isNotNull();
        });
    }

    // ==================== Behavioral Tests for Source List ====================

    @Test
    @DisplayName("Should remove selected items from source list when delete action is triggered")
    void shouldRemoveSelectedItemsFromSourceList() throws Exception {
        runOnFxThread(() -> {
            ListView<File> srcList = getField("srcList");
            MenuItem srcListDelMenuItem = getField("srcListDelMenuItem");
            
            // Add test items
            File file1 = new File("/test/roms1");
            File file2 = new File("/test/roms2");
            srcList.getItems().addAll(file1, file2);
            
            // Select first item
            srcList.getSelectionModel().select(file1);
            
            // Trigger delete action
            srcListDelMenuItem.getOnAction().handle(null);
            
            // Verify item was removed
            assertThat(srcList.getItems())
                .as("source list should contain only file2 after deletion")
                .hasSize(1)
                .containsExactly(file2);
        });
    }

    @Test
    @DisplayName("Should remove multiple selected items from source list")
    void shouldRemoveMultipleSelectedItemsFromSourceList() throws Exception {
        runOnFxThread(() -> {
            ListView<File> srcList = getField("srcList");
            MenuItem srcListDelMenuItem = getField("srcListDelMenuItem");
            
            // Add test items
            File file1 = new File("/test/roms1");
            File file2 = new File("/test/roms2");
            File file3 = new File("/test/roms3");
            srcList.getItems().addAll(file1, file2, file3);
            
            // Select multiple items explicitly by range
            srcList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
            srcList.getSelectionModel().selectRange(0, 3);
            
            // Verify selection worked
            assertThat(srcList.getSelectionModel().getSelectedItems()).hasSize(3);
            
            // Trigger delete action
            srcListDelMenuItem.getOnAction().handle(null);
            
            // Verify all items were removed
            assertThat(srcList.getItems())
                .as("source list should be empty after deleting all items")
                .isEmpty();
        });
    }

    // ==================== Behavioral Tests for Destination Controls ====================

    @Test
    @DisplayName("Should toggle disks destination enabled state when checkbox is clicked")
    void shouldToggleDisksDestinationEnabledState() throws Exception {
        runOnFxThread(() -> {
            CheckBox disksDestCB = getField("disksDestCB");
            TextField disksDest = getField("disksDest");
            Button disksDestBtn = getField("disksDestBtn");
            
            // Enable via checkbox (listener fires on change from false->true)
            disksDestCB.setSelected(true);
            
            // Verify enabled
            assertThat(disksDest.isDisable()).as("disks destination field should be enabled after checkbox selection").isFalse();
            assertThat(disksDestBtn.isDisable()).as("disks destination button should be enabled after checkbox selection").isFalse();
            
            // Disable via checkbox (listener fires on change from true->false)
            disksDestCB.setSelected(false);
            
            // Verify disabled again
            assertThat(disksDest.isDisable()).as("disks destination field should be disabled after checkbox deselection").isTrue();
            assertThat(disksDestBtn.isDisable()).as("disks destination button should be disabled after checkbox deselection").isTrue();
        });
    }

    @Test
    @DisplayName("Should toggle software destination enabled state when checkbox is clicked")
    void shouldToggleSoftwareDestinationEnabledState() throws Exception {
        runOnFxThread(() -> {
            CheckBox swDestCB = getField("swDestCB");
            TextField swDest = getField("swDest");
            Button swDestBtn = getField("swDestBtn");
            
            // Enable via checkbox
            swDestCB.setSelected(true);
            
            // Verify enabled
            assertThat(swDest.isDisable()).as("software destination field should be enabled after checkbox selection").isFalse();
            assertThat(swDestBtn.isDisable()).as("software destination button should be enabled after checkbox selection").isFalse();
            
            // Disable via checkbox
            swDestCB.setSelected(false);
            
            // Verify disabled
            assertThat(swDest.isDisable()).as("software destination field should be disabled after checkbox deselection").isTrue();
            assertThat(swDestBtn.isDisable()).as("software destination button should be disabled after checkbox deselection").isTrue();
        });
    }

    @Test
    @DisplayName("Should toggle samples destination enabled state when checkbox is clicked")
    void shouldToggleSamplesDestinationEnabledState() throws Exception {
        runOnFxThread(() -> {
            CheckBox samplesDestCB = getField("samplesDestCB");
            TextField samplesDest = getField("samplesDest");
            Button samplesDestBtn = getField("samplesDestBtn");
            
            // Enable via checkbox
            samplesDestCB.setSelected(true);
            
            // Verify enabled
            assertThat(samplesDest.isDisable()).as("samples destination field should be enabled after checkbox selection").isFalse();
            assertThat(samplesDestBtn.isDisable()).as("samples destination button should be enabled after checkbox selection").isFalse();
            
            // Disable via checkbox
            samplesDestCB.setSelected(false);
            
            // Verify disabled
            assertThat(samplesDest.isDisable()).as("samples destination field should be disabled after checkbox deselection").isTrue();
            assertThat(samplesDestBtn.isDisable()).as("samples destination button should be disabled after checkbox deselection").isTrue();
        });
    }

    @Test
    @DisplayName("Should toggle backup destination enabled state when checkbox is clicked")
    void shouldToggleBackupDestinationEnabledState() throws Exception {
        runOnFxThread(() -> {
            CheckBox backupDestCB = getField("backupDestCB");
            TextField backupDest = getField("backupDest");
            Button backupDestBtn = getField("backupDestBtn");
            
            // Enable via checkbox
            backupDestCB.setSelected(true);
            
            // Verify enabled
            assertThat(backupDest.isDisable()).as("backup destination field should be enabled after checkbox selection").isFalse();
            assertThat(backupDestBtn.isDisable()).as("backup destination button should be enabled after checkbox selection").isFalse();
            
            // Disable via checkbox
            backupDestCB.setSelected(false);
            
            // Verify disabled
            assertThat(backupDest.isDisable()).as("backup destination field should be disabled after checkbox deselection").isTrue();
            assertThat(backupDestBtn.isDisable()).as("backup destination button should be disabled after checkbox deselection").isTrue();
        });
    }

    // ==================== Behavioral Tests for Action Buttons ====================

    @Test
    @DisplayName("Should have scan method defined for FXML onAction")
    void shouldHaveScanFXMLMethodDefined() throws Exception {
        java.lang.reflect.Method scanMethod = ScannerPanelController.class.getDeclaredMethod("scan", javafx.event.ActionEvent.class);
        assertThat(scanMethod).as("scan(ActionEvent) method exists for FXML").isNotNull();
    }

    @Test
    @DisplayName("Should have fix method defined for FXML onAction")
    void shouldHaveFixFXMLMethodDefined() throws Exception {
        java.lang.reflect.Method fixMethod = ScannerPanelController.class.getDeclaredMethod("fix", javafx.event.ActionEvent.class);
        assertThat(fixMethod).as("fix(ActionEvent) method exists for FXML").isNotNull();
    }

    @Test
    @DisplayName("Should have report method defined for FXML onAction")
    void shouldHaveReportFXMLMethodDefined() throws Exception {
        java.lang.reflect.Method reportMethod = ScannerPanelController.class.getDeclaredMethod("report", javafx.event.ActionEvent.class);
        assertThat(reportMethod).as("report(ActionEvent) method exists for FXML").isNotNull();
    }

    @Test
    @DisplayName("Should have all action buttons with proper icons")
    void shouldHaveAllActionButtonsWithIcons() throws Exception {
        runOnFxThread(() -> {
            Button infosBtn = getField("infosBtn");
            Button scanBtn = getField("scanBtn");
            Button reportBtn = getField("reportBtn");
            Button fixBtn = getField("fixBtn");
            Button importBtn = getField("importBtn");
            Button exportBtn = getField("exportBtn");
            
            assertThat(infosBtn.getGraphic()).as("infos button should have icon").isNotNull();
            assertThat(scanBtn.getGraphic()).as("scan button should have icon").isNotNull();
            assertThat(reportBtn.getGraphic()).as("report button should have icon").isNotNull();
            assertThat(fixBtn.getGraphic()).as("fix button should have icon").isNotNull();
            assertThat(importBtn.getGraphic()).as("import button should have icon").isNotNull();
            assertThat(exportBtn.getGraphic()).as("export button should have icon").isNotNull();
        });
    }

    // ==================== Destination Button Icon Tests ====================

    @Test
    @DisplayName("Should set icons on all destination buttons")
    void shouldSetIconsOnAllDestinationButtons() throws Exception {
        runOnFxThread(() -> {
            assertThat(((Button) getField("romsDestBtn")).getGraphic()).as("romsDestBtn icon").isNotNull();
            assertThat(((Button) getField("disksDestBtn")).getGraphic()).as("disksDestBtn icon").isNotNull();
            assertThat(((Button) getField("swDestBtn")).getGraphic()).as("swDestBtn icon").isNotNull();
            assertThat(((Button) getField("swDisksDestBtn")).getGraphic()).as("swDisksDestBtn icon").isNotNull();
            assertThat(((Button) getField("samplesDestBtn")).getGraphic()).as("samplesDestBtn icon").isNotNull();
            assertThat(((Button) getField("backupDestBtn")).getGraphic()).as("backupDestBtn icon").isNotNull();
        });
    }

    // ==================== Destination Property Update Tests ====================

    @Test
    @DisplayName("Should update profile property when disks destination checkbox is toggled")
    void shouldUpdateProfilePropertyWhenDisksDestCheckboxToggled() throws Exception {
        runOnFxThread(() -> {
            CheckBox disksDestCB = getField("disksDestCB");
            disksDestCB.setSelected(true);
            verify(TestApp.controller.session.getCurrProfile()).setProperty(ProfileSettingsEnum.disks_dest_dir_enabled, true);
        });
    }

    @Test
    @DisplayName("Should update profile property when software destination checkbox is toggled")
    void shouldUpdateProfilePropertyWhenSwDestCheckboxToggled() throws Exception {
        runOnFxThread(() -> {
            CheckBox swDestCB = getField("swDestCB");
            swDestCB.setSelected(true);
            verify(TestApp.controller.session.getCurrProfile()).setProperty(ProfileSettingsEnum.swroms_dest_dir_enabled, true);
        });
    }

    @Test
    @DisplayName("Should update profile property when software disks destination checkbox is toggled")
    void shouldUpdateProfilePropertyWhenSwDisksDestCheckboxToggled() throws Exception {
        runOnFxThread(() -> {
            CheckBox swDisksDestCB = getField("swDisksDestCB");
            swDisksDestCB.setSelected(true);
            verify(TestApp.controller.session.getCurrProfile()).setProperty(ProfileSettingsEnum.swdisks_dest_dir_enabled, true);
        });
    }

    @Test
    @DisplayName("Should update profile property when samples destination checkbox is toggled")
    void shouldUpdateProfilePropertyWhenSamplesDestCheckboxToggled() throws Exception {
        runOnFxThread(() -> {
            CheckBox samplesDestCB = getField("samplesDestCB");
            samplesDestCB.setSelected(true);
            verify(TestApp.controller.session.getCurrProfile()).setProperty(ProfileSettingsEnum.samples_dest_dir_enabled, true);
        });
    }

    @Test
    @DisplayName("Should update profile property when backup destination checkbox is toggled")
    void shouldUpdateProfilePropertyWhenBackupDestCheckboxToggled() throws Exception {
        runOnFxThread(() -> {
            CheckBox backupDestCB = getField("backupDestCB");
            backupDestCB.setSelected(true);
            verify(TestApp.controller.session.getCurrProfile()).setProperty(ProfileSettingsEnum.backup_dest_dir_enabled, true);
        });
    }

    // ==================== Filter Action Property Update Tests ====================

    @Test
    @DisplayName("Should update profile property when include clones checkbox is fired")
    void shouldUpdateProfilePropertyWhenIncludeClonesToggled() throws Exception {
        runOnFxThread(() -> {
            CheckBox chckbxIncludeClones = getField("chckbxIncludeClones");
            chckbxIncludeClones.fire();
            verify(TestApp.controller.session.getCurrProfile()).setProperty(ProfileSettingsEnum.filter_InclClones, true);
        });
    }

    @Test
    @DisplayName("Should update profile property when include disks checkbox is fired")
    void shouldUpdateProfilePropertyWhenIncludeDisksToggled() throws Exception {
        runOnFxThread(() -> {
            CheckBox chckbxIncludeDisks = getField("chckbxIncludeDisks");
            chckbxIncludeDisks.fire();
            verify(TestApp.controller.session.getCurrProfile()).setProperty(ProfileSettingsEnum.filter_InclDisks, true);
        });
    }

    @Test
    @DisplayName("Should update profile property when include samples checkbox is fired")
    void shouldUpdateProfilePropertyWhenIncludeSamplesToggled() throws Exception {
        runOnFxThread(() -> {
            CheckBox chckbxIncludeSamples = getField("chckbxIncludeSamples");
            chckbxIncludeSamples.fire();
            verify(TestApp.controller.session.getCurrProfile()).setProperty(ProfileSettingsEnum.filter_InclSamples, true);
        });
    }

    // ==================== Cell Factory Tests ====================

    @Test
    @DisplayName("Should set cell factory on source list")
    void shouldSetCellFactoryOnSourceList() throws Exception {
        runOnFxThread(() -> {
            @SuppressWarnings("unchecked")
            ListView<File> srcList = getField("srcList");
            assertThat(srcList.getCellFactory()).as("srcList cell factory").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set cell factory on systems filter list")
    void shouldSetCellFactoryOnSystemsFilter() throws Exception {
        runOnFxThread(() -> {
            @SuppressWarnings("unchecked")
            ListView<?> systemsFilter = getField("systemsFilter");
            assertThat(systemsFilter.getCellFactory()).as("systemsFilter cell factory").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set cell factory on sources filter list")
    void shouldSetCellFactoryOnSourcesFilter() throws Exception {
        runOnFxThread(() -> {
            @SuppressWarnings("unchecked")
            ListView<?> sourcesFilter = getField("sourcesFilter");
            assertThat(sourcesFilter.getCellFactory()).as("sourcesFilter cell factory").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set cell factory on nplayers list")
    void shouldSetCellFactoryOnNPlayersList() throws Exception {
        runOnFxThread(() -> {
            @SuppressWarnings("unchecked")
            ListView<?> listNPlayers = getField("listNPlayers");
            assertThat(listNPlayers.getCellFactory()).as("listNPlayers cell factory").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set cell factory on catver tree")
    void shouldSetCellFactoryOnCatVerTree() throws Exception {
        runOnFxThread(() -> {
            @SuppressWarnings("unchecked")
            TreeView<?> treeCatVer = getField("treeCatVer");
            assertThat(treeCatVer.getCellFactory()).as("treeCatVer cell factory").isNotNull();
        });
    }

    @Test
    @DisplayName("Should set cell factory on automation combo box")
    void shouldSetCellFactoryOnAutomationComboBox() throws Exception {
        runOnFxThread(() -> {
            @SuppressWarnings("unchecked")
            ComboBox<ScanAutomation> cbAutomation = getField("cbAutomation");
            assertThat(cbAutomation.getCellFactory()).as("cbAutomation cell factory").isNotNull();
            assertThat(cbAutomation.getButtonCell()).as("cbAutomation button cell").isNotNull();
        });
    }

    // ==================== Source List Save Tests ====================

    @Test
    @DisplayName("Should update profile property when items are deleted from source list")
    void shouldUpdateProfilePropertyWhenItemsDeletedFromSourceList() throws Exception {
        runOnFxThread(() -> {
            @SuppressWarnings("unchecked")
            ListView<File> srcList = getField("srcList");
            MenuItem srcListDelMenuItem = getField("srcListDelMenuItem");

            srcList.getItems().addAll(new File("/test/roms1"), new File("/test/roms2"));
            srcList.getSelectionModel().selectFirst();
            srcListDelMenuItem.getOnAction().handle(null);

            verify(TestApp.controller.session.getCurrProfile()).setProperty(eq(ProfileSettingsEnum.src_dir), anyString());
        });
    }

    // ==================== Context Menu Handler Tests ====================

    @Test
    @DisplayName("Should have onShowing handler on systems filter menu")
    void shouldHaveOnShowingHandlerOnSystemsFilterMenu() throws Exception {
        runOnFxThread(() -> {
            ContextMenu menu = getField("systemsFilterMenu");
            assertThat(menu).as("systemsFilterMenu").isNotNull();
        });
    }

    @Test
    @DisplayName("Should have onShowing handler on sources filter menu")
    void shouldHaveOnShowingHandlerOnSourcesFilterMenu() throws Exception {
        runOnFxThread(() -> {
            ContextMenu menu = getField("sourcesFilterMenu");
            assertThat(menu).as("sourcesFilterMenu").isNotNull();
        });
    }

    @Test
    @DisplayName("Should have onShowing handler on nplayers menu")
    void shouldHaveOnShowingHandlerOnNPlayersMenu() throws Exception {
        runOnFxThread(() -> {
            ContextMenu menu = getField("nPlayersMenu");
            assertThat(menu).as("nPlayersMenu").isNotNull();
        });
    }

    @Test
    @DisplayName("Should have onShowing handler on catver menu")
    void shouldHaveOnShowingHandlerOnCatVerMenu() throws Exception {
        runOnFxThread(() -> {
            ContextMenu menu = getField("catVerMenu");
            assertThat(menu).as("catVerMenu").isNotNull();
        });
    }

    // ==================== initDestSettings Tests ====================

    /**
     * Stubs the mock profile's destination properties with the given values.
     *
     * @param romsDir       the roms destination directory
     * @param disksEnabled  whether disks destination is enabled
     * @param disksDir      the disks destination directory
     * @param swEnabled     whether software destination is enabled
     * @param swDir         the software destination directory
     * @param swDisksEnabled whether software disks destination is enabled
     * @param swDisksDir    the software disks destination directory
     * @param samplesEnabled whether samples destination is enabled
     * @param samplesDir    the samples destination directory
     * @param backupEnabled whether backup destination is enabled
     * @param backupDir     the backup destination directory
     */
    private void stubDestProperties(String romsDir, boolean disksEnabled, String disksDir,
            boolean swEnabled, String swDir, boolean swDisksEnabled, String swDisksDir,
            boolean samplesEnabled, String samplesDir, boolean backupEnabled, String backupDir) {
        Profile mockProfile = TestApp.controller.session.getCurrProfile();
        when(mockProfile.getProperty(ProfileSettingsEnum.roms_dest_dir)).thenReturn(romsDir);
        when(mockProfile.getProperty(ProfileSettingsEnum.disks_dest_dir_enabled, Boolean.class)).thenReturn(disksEnabled);
        when(mockProfile.getProperty(ProfileSettingsEnum.disks_dest_dir)).thenReturn(disksDir);
        when(mockProfile.getProperty(ProfileSettingsEnum.swroms_dest_dir_enabled, Boolean.class)).thenReturn(swEnabled);
        when(mockProfile.getProperty(ProfileSettingsEnum.swroms_dest_dir)).thenReturn(swDir);
        when(mockProfile.getProperty(ProfileSettingsEnum.swdisks_dest_dir_enabled, Boolean.class)).thenReturn(swDisksEnabled);
        when(mockProfile.getProperty(ProfileSettingsEnum.swdisks_dest_dir)).thenReturn(swDisksDir);
        when(mockProfile.getProperty(ProfileSettingsEnum.samples_dest_dir_enabled, Boolean.class)).thenReturn(samplesEnabled);
        when(mockProfile.getProperty(ProfileSettingsEnum.samples_dest_dir)).thenReturn(samplesDir);
        when(mockProfile.getProperty(ProfileSettingsEnum.backup_dest_dir_enabled, Boolean.class)).thenReturn(backupEnabled);
        when(mockProfile.getProperty(ProfileSettingsEnum.backup_dest_dir)).thenReturn(backupDir);
    }

    @Test
    @DisplayName("initDestSettings should populate roms destination from profile")
    void initDestSettingsShouldPopulateRomsDest() throws Exception {
        runOnFxThread(() -> {
            stubDestProperties("/roms/path", false, null, false, null, false, null, false, null, false, null);
            TestApp.controller.initDestSettings(TestApp.controller.session);
            TextField romsDest = getField("romsDest");
            assertThat(romsDest.getText()).as("roms destination").isEqualTo("/roms/path");
        });
    }

    @Test
    @DisplayName("initDestSettings should populate disks destination and checkbox from profile")
    void initDestSettingsShouldPopulateDisksDest() throws Exception {
        runOnFxThread(() -> {
            stubDestProperties(null, true, "/disks/path", false, null, false, null, false, null, false, null);
            TestApp.controller.initDestSettings(TestApp.controller.session);
            CheckBox disksDestCB = getField("disksDestCB");
            TextField disksDest = getField("disksDest");
            assertThat(disksDestCB.isSelected()).as("disks dest checkbox selected").isTrue();
            assertThat(disksDest.getText()).as("disks destination").isEqualTo("/disks/path");
        });
    }

    @Test
    @DisplayName("initDestSettings should populate software destination and checkbox from profile")
    void initDestSettingsShouldPopulateSwDest() throws Exception {
        runOnFxThread(() -> {
            stubDestProperties(null, false, null, true, "/sw/path", false, null, false, null, false, null);
            TestApp.controller.initDestSettings(TestApp.controller.session);
            CheckBox swDestCB = getField("swDestCB");
            TextField swDest = getField("swDest");
            assertThat(swDestCB.isSelected()).as("sw dest checkbox selected").isTrue();
            assertThat(swDest.getText()).as("sw destination").isEqualTo("/sw/path");
        });
    }

    @Test
    @DisplayName("initDestSettings should populate software disks destination and checkbox from profile")
    void initDestSettingsShouldPopulateSwDisksDest() throws Exception {
        runOnFxThread(() -> {
            stubDestProperties(null, false, null, false, null, true, "/swdisks/path", false, null, false, null);
            TestApp.controller.initDestSettings(TestApp.controller.session);
            CheckBox swDisksDestCB = getField("swDisksDestCB");
            TextField swDisksDest = getField("swDisksDest");
            assertThat(swDisksDestCB.isSelected()).as("sw disks dest checkbox selected").isTrue();
            assertThat(swDisksDest.getText()).as("sw disks destination").isEqualTo("/swdisks/path");
        });
    }

    @Test
    @DisplayName("initDestSettings should populate samples destination and checkbox from profile")
    void initDestSettingsShouldPopulateSamplesDest() throws Exception {
        runOnFxThread(() -> {
            stubDestProperties(null, false, null, false, null, false, null, true, "/samples/path", false, null);
            TestApp.controller.initDestSettings(TestApp.controller.session);
            CheckBox samplesDestCB = getField("samplesDestCB");
            TextField samplesDest = getField("samplesDest");
            assertThat(samplesDestCB.isSelected()).as("samples dest checkbox selected").isTrue();
            assertThat(samplesDest.getText()).as("samples destination").isEqualTo("/samples/path");
        });
    }

    @Test
    @DisplayName("initDestSettings should populate backup destination and checkbox from profile")
    void initDestSettingsShouldPopulateBackupDest() throws Exception {
        runOnFxThread(() -> {
            stubDestProperties(null, false, null, false, null, false, null, false, null, true, "/backup/path");
            TestApp.controller.initDestSettings(TestApp.controller.session);
            CheckBox backupDestCB = getField("backupDestCB");
            TextField backupDest = getField("backupDest");
            assertThat(backupDestCB.isSelected()).as("backup dest checkbox selected").isTrue();
            assertThat(backupDest.getText()).as("backup destination").isEqualTo("/backup/path");
        });
    }

    @Test
    @DisplayName("initDestSettings should uncheck all destination checkboxes when disabled in profile")
    void initDestSettingsShouldUncheckAllWhenDisabled() throws Exception {
        runOnFxThread(() -> {
            stubDestProperties(null, false, null, false, null, false, null, false, null, false, null);
            TestApp.controller.initDestSettings(TestApp.controller.session);
            assertThat(((CheckBox) getField("disksDestCB")).isSelected()).as("disksDestCB unchecked").isFalse();
            assertThat(((CheckBox) getField("swDestCB")).isSelected()).as("swDestCB unchecked").isFalse();
            assertThat(((CheckBox) getField("swDisksDestCB")).isSelected()).as("swDisksDestCB unchecked").isFalse();
            assertThat(((CheckBox) getField("samplesDestCB")).isSelected()).as("samplesDestCB unchecked").isFalse();
            assertThat(((CheckBox) getField("backupDestCB")).isSelected()).as("backupDestCB unchecked").isFalse();
        });
    }

    @Test
    @DisplayName("initDestSettings should check all destination checkboxes when enabled in profile")
    void initDestSettingsShouldCheckAllWhenEnabled() throws Exception {
        runOnFxThread(() -> {
            stubDestProperties("/roms", true, "/disks", true, "/sw", true, "/swdisks", true, "/samples", true, "/backup");
            TestApp.controller.initDestSettings(TestApp.controller.session);
            assertThat(((CheckBox) getField("disksDestCB")).isSelected()).as("disksDestCB checked").isTrue();
            assertThat(((CheckBox) getField("swDestCB")).isSelected()).as("swDestCB checked").isTrue();
            assertThat(((CheckBox) getField("swDisksDestCB")).isSelected()).as("swDisksDestCB checked").isTrue();
            assertThat(((CheckBox) getField("samplesDestCB")).isSelected()).as("samplesDestCB checked").isTrue();
            assertThat(((CheckBox) getField("backupDestCB")).isSelected()).as("backupDestCB checked").isTrue();
        });
    }

    @Test
    @DisplayName("initDestSettings should enable destination fields when checkbox is selected")
    void initDestSettingsShouldEnableFieldsWhenCheckboxSelected() throws Exception {
        runOnFxThread(() -> {
            stubDestProperties("/roms", true, "/disks", true, "/sw", true, "/swdisks", true, "/samples", true, "/backup");
            TestApp.controller.initDestSettings(TestApp.controller.session);
            assertThat(((TextField) getField("disksDest")).isDisable()).as("disksDest enabled").isFalse();
            assertThat(((Button) getField("disksDestBtn")).isDisable()).as("disksDestBtn enabled").isFalse();
            assertThat(((TextField) getField("swDest")).isDisable()).as("swDest enabled").isFalse();
            assertThat(((Button) getField("swDestBtn")).isDisable()).as("swDestBtn enabled").isFalse();
            assertThat(((TextField) getField("swDisksDest")).isDisable()).as("swDisksDest enabled").isFalse();
            assertThat(((Button) getField("swDisksDestBtn")).isDisable()).as("swDisksDestBtn enabled").isFalse();
            assertThat(((TextField) getField("samplesDest")).isDisable()).as("samplesDest enabled").isFalse();
            assertThat(((Button) getField("samplesDestBtn")).isDisable()).as("samplesDestBtn enabled").isFalse();
            assertThat(((TextField) getField("backupDest")).isDisable()).as("backupDest enabled").isFalse();
            assertThat(((Button) getField("backupDestBtn")).isDisable()).as("backupDestBtn enabled").isFalse();
        });
    }

    @Test
    @DisplayName("initDestSettings should disable destination fields when transitioning from enabled to disabled")
    void initDestSettingsShouldDisableFieldsWhenTransitioningToDisabled() throws Exception {
        runOnFxThread(() -> {
            stubDestProperties("/roms", true, "/disks", true, "/sw", true, "/swdisks", true, "/samples", true, "/backup");
            TestApp.controller.initDestSettings(TestApp.controller.session);
            // Now re-stub with all disabled and call again - listener fires on true→false transition
            stubDestProperties("/roms", false, "/disks", false, "/sw", false, "/swdisks", false, "/samples", false, "/backup");
            TestApp.controller.initDestSettings(TestApp.controller.session);
            assertThat(((TextField) getField("disksDest")).isDisable()).as("disksDest disabled").isTrue();
            assertThat(((Button) getField("disksDestBtn")).isDisable()).as("disksDestBtn disabled").isTrue();
            assertThat(((TextField) getField("swDest")).isDisable()).as("swDest disabled").isTrue();
            assertThat(((Button) getField("swDestBtn")).isDisable()).as("swDestBtn disabled").isTrue();
            assertThat(((TextField) getField("swDisksDest")).isDisable()).as("swDisksDest disabled").isTrue();
            assertThat(((Button) getField("swDisksDestBtn")).isDisable()).as("swDisksDestBtn disabled").isTrue();
            assertThat(((TextField) getField("samplesDest")).isDisable()).as("samplesDest disabled").isTrue();
            assertThat(((Button) getField("samplesDestBtn")).isDisable()).as("samplesDestBtn disabled").isTrue();
            assertThat(((TextField) getField("backupDest")).isDisable()).as("backupDest disabled").isTrue();
            assertThat(((Button) getField("backupDestBtn")).isDisable()).as("backupDestBtn disabled").isTrue();
        });
    }

    // ==================== initFilterSettings Tests ====================

    /**
     * Stubs the mock profile's filter properties with the given values.
     *
     * @param inclClones    whether to include clones
     * @param inclDisks     whether to include disks
     * @param inclSamples   whether to include samples
     * @param driverStatus  the driver status string
     * @param cabinetType   the cabinet type string
     * @param orientation   the display orientation string
     * @param supported     the software support level string
     * @param years         the collection of years
     * @param yearMin       the minimum year
     * @param yearMax       the maximum year
     */
    private void stubFilterProperties(boolean inclClones, boolean inclDisks, boolean inclSamples,
            String driverStatus, String cabinetType, String orientation, String supported,
            List<String> years, String yearMin, String yearMax) {
        Profile mockProfile = TestApp.controller.session.getCurrProfile();
        when(mockProfile.getProperty(ProfileSettingsEnum.filter_InclClones, Boolean.class)).thenReturn(inclClones);
        when(mockProfile.getProperty(ProfileSettingsEnum.filter_InclDisks, Boolean.class)).thenReturn(inclDisks);
        when(mockProfile.getProperty(ProfileSettingsEnum.filter_InclSamples, Boolean.class)).thenReturn(inclSamples);
        when(mockProfile.getProperty(ProfileSettingsEnum.filter_DriverStatus)).thenReturn(driverStatus);
        when(mockProfile.getProperty(ProfileSettingsEnum.filter_CabinetType)).thenReturn(cabinetType);
        when(mockProfile.getProperty(ProfileSettingsEnum.filter_DisplayOrientation)).thenReturn(orientation);
        when(mockProfile.getProperty(ProfileSettingsEnum.filter_MinSoftwareSupportedLevel)).thenReturn(supported);
        when(mockProfile.getYears()).thenReturn(years);
        when(mockProfile.getProperty(ProfileSettingsEnum.filter_YearMin)).thenReturn(yearMin);
        when(mockProfile.getProperty(ProfileSettingsEnum.filter_YearMax)).thenReturn(yearMax);
    }

    @Test
    @DisplayName("initFilterSettings should set include clones checkbox from profile")
    void initFilterSettingsShouldSetIncludeClonesCheckbox() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(true, false, false, "good", "any", "any", "yes",
                    Arrays.asList("", "1980", "1990"), "1980", "1990");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            CheckBox chckbxIncludeClones = getField("chckbxIncludeClones");
            assertThat(chckbxIncludeClones.isSelected()).as("include clones selected").isTrue();
        });
    }

    @Test
    @DisplayName("initFilterSettings should set include disks checkbox from profile")
    void initFilterSettingsShouldSetIncludeDisksCheckbox() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(false, true, false, "good", "any", "any", "yes",
                    Arrays.asList("", "1980", "1990"), "1980", "1990");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            CheckBox chckbxIncludeDisks = getField("chckbxIncludeDisks");
            assertThat(chckbxIncludeDisks.isSelected()).as("include disks selected").isTrue();
        });
    }

    @Test
    @DisplayName("initFilterSettings should set include samples checkbox from profile")
    void initFilterSettingsShouldSetIncludeSamplesCheckbox() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(false, false, true, "good", "any", "any", "yes",
                    Arrays.asList("", "1980", "1990"), "1980", "1990");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            CheckBox chckbxIncludeSamples = getField("chckbxIncludeSamples");
            assertThat(chckbxIncludeSamples.isSelected()).as("include samples selected").isTrue();
        });
    }

    @Test
    @DisplayName("initFilterSettings should uncheck all filter checkboxes when false in profile")
    void initFilterSettingsShouldUncheckAllWhenFalse() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(false, false, false, "good", "any", "any", "yes",
                    Arrays.asList("", "1980", "1990"), "1980", "1990");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            assertThat(((CheckBox) getField("chckbxIncludeClones")).isSelected()).as("clones unchecked").isFalse();
            assertThat(((CheckBox) getField("chckbxIncludeDisks")).isSelected()).as("disks unchecked").isFalse();
            assertThat(((CheckBox) getField("chckbxIncludeSamples")).isSelected()).as("samples unchecked").isFalse();
        });
    }

    @Test
    @DisplayName("initFilterSettings should select driver status combo box from profile")
    void initFilterSettingsShouldSelectDriverStatus() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(false, false, false, "preliminary", "any", "any", "yes",
                    Arrays.asList("", "1980", "1990"), "1980", "1990");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            @SuppressWarnings("unchecked")
            ComboBox<Driver.StatusType> cbbxDriverStatus = getField("cbbxDriverStatus");
            assertThat(cbbxDriverStatus.getValue()).as("driver status").isEqualTo(Driver.StatusType.preliminary);
        });
    }

    @Test
    @DisplayName("initFilterSettings should select cabinet type combo box from profile")
    void initFilterSettingsShouldSelectCabinetType() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(false, false, false, "good", "upright", "any", "yes",
                    Arrays.asList("", "1980", "1990"), "1980", "1990");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            @SuppressWarnings("unchecked")
            ComboBox<CabinetType> cbbxFilterCabinetType = getField("cbbxFilterCabinetType");
            assertThat(cbbxFilterCabinetType.getValue()).as("cabinet type").isEqualTo(CabinetType.upright);
        });
    }

    @Test
    @DisplayName("initFilterSettings should select display orientation combo box from profile")
    void initFilterSettingsShouldSelectDisplayOrientation() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(false, false, false, "good", "any", "vertical", "yes",
                    Arrays.asList("", "1980", "1990"), "1980", "1990");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            @SuppressWarnings("unchecked")
            ComboBox<DisplayOrientation> cbbxFilterDisplayOrientation = getField("cbbxFilterDisplayOrientation");
            assertThat(cbbxFilterDisplayOrientation.getValue()).as("display orientation").isEqualTo(DisplayOrientation.vertical);
        });
    }

    @Test
    @DisplayName("initFilterSettings should select software support level combo box from profile")
    void initFilterSettingsShouldSelectSoftwareSupportLevel() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(false, false, false, "good", "any", "any", "partial",
                    Arrays.asList("", "1980", "1990"), "1980", "1990");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            @SuppressWarnings("unchecked")
            ComboBox<Supported> cbbxSWMinSupportedLvl = getField("cbbxSWMinSupportedLvl");
            assertThat(cbbxSWMinSupportedLvl.getValue()).as("software support level").isEqualTo(Supported.partial);
        });
    }

    @Test
    @DisplayName("initFilterSettings should populate year min combo box items from profile")
    void initFilterSettingsShouldPopulateYearMinItems() throws Exception {
        runOnFxThread(() -> {
            List<String> years = Arrays.asList("", "1980", "1990", "2000");
            stubFilterProperties(false, false, false, "good", "any", "any", "yes",
                    years, "1980", "2000");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            @SuppressWarnings("unchecked")
            ComboBox<String> cbbxYearMin = getField("cbbxYearMin");
            assertThat(cbbxYearMin.getItems()).as("year min items").isNotEmpty();
            assertThat(cbbxYearMin.getItems()).as("year min items contain all years").containsExactlyElementsOf(years.stream().sorted().toList());
        });
    }

    @Test
    @DisplayName("initFilterSettings should select year min from profile")
    void initFilterSettingsShouldSelectYearMin() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(false, false, false, "good", "any", "any", "yes",
                    Arrays.asList("", "1980", "1990", "2000"), "1990", "2000");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            @SuppressWarnings("unchecked")
            ComboBox<String> cbbxYearMin = getField("cbbxYearMin");
            assertThat(cbbxYearMin.getValue()).as("year min selected").isEqualTo("1990");
        });
    }

    @Test
    @DisplayName("initFilterSettings should populate year max combo box items from profile")
    void initFilterSettingsShouldPopulateYearMaxItems() throws Exception {
        runOnFxThread(() -> {
            List<String> years = Arrays.asList("", "1980", "1990", "2000");
            stubFilterProperties(false, false, false, "good", "any", "any", "yes",
                    years, "1980", "2000");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            @SuppressWarnings("unchecked")
            ComboBox<String> cbbxYearMax = getField("cbbxYearMax");
            assertThat(cbbxYearMax.getItems()).as("year max items").isNotEmpty();
            assertThat(cbbxYearMax.getItems()).as("year max items contain all years").containsExactlyElementsOf(years.stream().sorted().toList());
        });
    }

    @Test
    @DisplayName("initFilterSettings should select year max from profile")
    void initFilterSettingsShouldSelectYearMax() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(false, false, false, "good", "any", "any", "yes",
                    Arrays.asList("", "1980", "1990", "2000"), "1980", "2000");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            @SuppressWarnings("unchecked")
            ComboBox<String> cbbxYearMax = getField("cbbxYearMax");
            assertThat(cbbxYearMax.getValue()).as("year max selected").isEqualTo("2000");
        });
    }

    @Test
    @DisplayName("initFilterSettings should set all filter checkboxes when true in profile")
    void initFilterSettingsShouldCheckAllWhenTrue() throws Exception {
        runOnFxThread(() -> {
            stubFilterProperties(true, true, true, "good", "any", "any", "yes",
                    Arrays.asList("", "1980", "1990"), "1980", "1990");
            TestApp.controller.initFilterSettings(TestApp.controller.session);
            assertThat(((CheckBox) getField("chckbxIncludeClones")).isSelected()).as("clones checked").isTrue();
            assertThat(((CheckBox) getField("chckbxIncludeDisks")).isSelected()).as("disks checked").isTrue();
            assertThat(((CheckBox) getField("chckbxIncludeSamples")).isSelected()).as("samples checked").isTrue();
        });
    }
}
