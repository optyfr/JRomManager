package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Tests for {@link MainFrameController}.
 * <p>
 * Verifies the {@code initialize} method sets up tab icons,
 * disables the scanner tab, and that the FXML-injected fields
 * are accessible via getters.
 *
 * @since 3.0.5
 */
@TestFxApplication(MainFrameControllerTest.TestApp.class)
@DisplayName("MainFrameController Tests")
class MainFrameControllerTest {

    /**
     * Minimal application that loads a real {@link MainFrameController}
     * with mocked sub-controllers injected via reflection.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        private static MainFrameController controller;

        @Override
        public void start(Stage primaryStage) throws Exception {
            this.primaryStage = primaryStage;
            
            ProfilePanelController mockProfile = Mockito.mock(ProfilePanelController.class);
            ScannerPanelController mockScanner = Mockito.mock(ScannerPanelController.class);
            SettingsPanelController mockSettings = Mockito.mock(SettingsPanelController.class);

            Tab profileTab = new Tab("Profile");
            Tab scannerTab = new Tab("Scanner");
            Tab dir2datTab = new Tab("Dir2Dat");
            Tab batchTab = new Tab("Batch Tools");
            Tab settingsTab = new Tab("Settings");

            TabPane tabPane = new TabPane(profileTab, scannerTab, dir2datTab, batchTab, settingsTab);

            controller = new MainFrameController();

            injectField(controller, "tabPane", tabPane);
            injectField(controller, "profilePanelTab", profileTab);
            injectField(controller, "scannerPanelTab", scannerTab);
            injectField(controller, "dir2datPanelTab", dir2datTab);
            injectField(controller, "batchtoolsPanelTab", batchTab);
            injectField(controller, "settingsPanelTab", settingsTab);
            injectField(controller, "profilePanel", new BorderPane());
            injectField(controller, "dir2datPanel", new GridPane());
            injectField(controller, "batchtoolsPanel", new TabPane());
            injectField(controller, "settingsPanel", new ScrollPane());
            injectField(controller, "profilePanelController", mockProfile);
            injectField(controller, "scannerPanelController", mockScanner);
            injectField(controller, "settingsPanelController", mockSettings);

            controller.initialize(null, null);

            Scene scene = new Scene(tabPane, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        private static void injectField(Object target, String fieldName, Object value) {
            try {
                var field = MainFrameController.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to inject field " + fieldName, e);
            }
        }

        /**
         * Returns the controller instance for test access.
         *
         * @return the controller
         */
        public static MainFrameController getController() {
            return controller;
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    @Test
    @DisplayName("Should set graphics on profile tab")
    void shouldSetGraphicsOnProfileTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane()).isNotNull();
        assertThat(ctrl.getTabPane().getTabs().get(0).getGraphic()).isNotNull();
    }

    @Test
    @DisplayName("Should set graphics on scanner tab")
    void shouldSetGraphicsOnScannerTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs().get(1).getGraphic()).isNotNull();
    }

    @Test
    @DisplayName("Should disable scanner tab")
    void shouldDisableScannerTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getScannerPanelTab().isDisable()).isTrue();
    }

    @Test
    @DisplayName("Should set graphics on dir2dat tab")
    void shouldSetGraphicsOnDir2DatTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs().get(2).getGraphic()).isNotNull();
    }

    @Test
    @DisplayName("Should set graphics on batchtools tab")
    void shouldSetGraphicsOnBatchToolsTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs().get(3).getGraphic()).isNotNull();
    }

    @Test
    @DisplayName("Should set graphics on settings tab")
    void shouldSetGraphicsOnSettingsTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs().get(4).getGraphic()).isNotNull();
    }

    @Test
    @DisplayName("Should have five tabs")
    void shouldHaveFiveTabs() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs()).hasSize(5);
    }

    @Test
    @DisplayName("Should return profile panel controller via getter")
    void shouldReturnProfilePanelController() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getProfilePanelController()).isNotNull();
    }

    @Test
    @DisplayName("Should return settings panel controller via getter")
    void shouldReturnSettingsPanelController() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getSettingsPanelController()).isNotNull();
    }

    @Test
    @DisplayName("Should apply icon style class to all tab graphics")
    void shouldApplyIconStyleClassToAllTabGraphics() {
        MainFrameController ctrl = TestApp.getController();
        for (var tab : ctrl.getTabPane().getTabs()) {
            assertThat(tab.getGraphic().getStyleClass()).contains("icon");
        }
    }
}
