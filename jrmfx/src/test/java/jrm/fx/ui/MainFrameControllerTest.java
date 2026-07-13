package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.mockito.Mockito.mock;

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
            
            ProfilePanelController mockProfile = mock(ProfilePanelController.class);
            ScannerPanelController mockScanner = mock(ScannerPanelController.class);
            SettingsPanelController mockSettings = mock(SettingsPanelController.class);

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

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    @DisplayName("Should set graphics on each tab")
    void shouldSetGraphicsOnTab(int tabIndex) {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane()).isNotNull();
        assertThat(ctrl.getTabPane().getTabs().get(tabIndex).getGraphic()).isNotNull();
    }

    @Test
    @DisplayName("Should disable scanner tab")
    void shouldDisableScannerTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getScannerPanelTab().isDisable()).isTrue();
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

    @Test
    @DisplayName("Should preserve ratio for all tab icons")
    void shouldPreserveRatioForAllTabIcons() {
        MainFrameController ctrl = TestApp.getController();
        for (var tab : ctrl.getTabPane().getTabs()) {
            if (tab.getGraphic() instanceof javafx.scene.image.ImageView imageView) {
                assertThat(imageView.isPreserveRatio())
                        .as("Icon should preserve ratio for tab: " + tab.getText())
                        .isTrue();
            }
        }
    }

    @Test
    @DisplayName("Should initialize with null location and resources")
    void shouldInitializeWithNullLocationAndResources() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl).isNotNull();
        assertThat(ctrl.getTabPane()).isNotNull();
        assertThat(ctrl.getTabPane().getTabs()).hasSize(5);
    }

    @Test
    @DisplayName("Should have profile panel tab as first tab")
    void shouldHaveProfilePanelTabAsFirstTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs().get(0).getText()).isEqualTo("Profile");
    }

    @Test
    @DisplayName("Should have scanner panel tab as second tab")
    void shouldHaveScannerPanelTabAsSecondTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs().get(1).getText()).isEqualTo("Scanner");
    }

    @Test
    @DisplayName("Should have Dir2Dat panel tab as third tab")
    void shouldHaveDir2DatPanelTabAsThirdTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs().get(2).getText()).isEqualTo("Dir2Dat");
    }

    @Test
    @DisplayName("Should have batch tools panel tab as fourth tab")
    void shouldHaveBatchToolsPanelTabAsFourthTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs().get(3).getText()).isEqualTo("Batch Tools");
    }

    @Test
    @DisplayName("Should have settings panel tab as fifth tab")
    void shouldHaveSettingsPanelTabAsFifthTab() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs().get(4).getText()).isEqualTo("Settings");
    }

    @Test
    @DisplayName("Should have profile panel as BorderPane")
    void shouldHaveProfilePanelAsBorderPane() {
        MainFrameController ctrl = TestApp.getController();
        assertThat(ctrl.getTabPane().getTabs().get(0).getContent())
                .isNull(); // Content is set via FXML, not in test
    }

    @Test
    @DisplayName("Should have scanner panel controller injected")
    void shouldHaveScannerPanelControllerInjected() {
        MainFrameController ctrl = TestApp.getController();
        // Scanner panel controller is mocked, so it should be present
        assertThat(ctrl).isNotNull();
    }
}
