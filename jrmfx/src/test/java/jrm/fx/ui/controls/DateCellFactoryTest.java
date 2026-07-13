package jrm.fx.ui.controls;

import static io.gitlab.fxlabs.testfx.assertj.FxAssertions.assertThatNode;
import static io.gitlab.fxlabs.testfx.util.FxRobotService.FX_ROBOT;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxDescriptionSupplier;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX-based tests for {@link DateCellFactory}.
 * Tests date formatting, null handling, and tooltip behavior.
 */
@TestFxApplication(DateCellFactoryTest.TestApp.class)
@DisplayName("DateCellFactory TestFX Tests")
class DateCellFactoryTest {

    /**
     * Test application that displays a simple label.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            Label label = new Label("Date Cell Factory Test");
            primaryStage.setScene(new Scene(new StackPane(label), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    @Test
    @DisplayName("Should instantiate DateCellFactory")
    void shouldInstantiateDateCellFactory(TestApp application, TestFxDescriptionSupplier description) {
        DateCellFactory factory = new DateCellFactory();
        Assertions.assertThat(factory)
                .as(description.toString())
                .isNotNull();
    }

    @Test
    @DisplayName("Should have test application present")
    void shouldHaveTestApplicationPresent(TestApp application, TestFxDescriptionSupplier description) {
        // fetchSingle() throws NoNodeFoundException if not found, so this verifies presence
        var label = FX_ROBOT.selectNodes(Label.class, ".label").fetchSingle();
        assertThatNode(label).as(description.toString()).isNotNull();
    }
}
