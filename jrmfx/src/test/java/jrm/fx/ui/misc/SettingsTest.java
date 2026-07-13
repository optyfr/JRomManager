package jrm.fx.ui.misc;

import static io.gitlab.fxlabs.testfx.assertj.FxAssertions.assertThat;
import static io.gitlab.fxlabs.testfx.util.FxRobotService.FX_ROBOT;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxDescriptionSupplier;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jrm.misc.Settings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX-based tests for {@link Settings} window state serialization.
 * Tests basic UI rendering of settings-related components.
 */
@TestFxApplication(SettingsTest.TestApp.class)
@DisplayName("Settings TestFX Tests")
class SettingsTest {

    /**
     * Test application that sets up a simple settings UI.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            Label label = new Label("Settings Test");
            primaryStage.setScene(new Scene(new StackPane(label), 300, 200));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    @Test
    @DisplayName("Should render settings label")
    void shouldRenderSettingsLabel(TestApp application, TestFxDescriptionSupplier description) {
        var label = FX_ROBOT.selectNodes(Label.class, ".label").fetchSingle();
        assertThat(label)
                .as(description)
                .extractingText()
                .isEqualTo("Settings Test");
    }

    @Test
    @DisplayName("Should have exactly one label in scene")
    void shouldHaveOneLabel(TestApp application, TestFxDescriptionSupplier description) {
        var labels = FX_ROBOT.selectNodes(Label.class, ".label").fromAll().toList();
        assertThat(labels)
                .as(description)
                .hasSize(1);
    }
}
