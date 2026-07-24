package jrm.fx.ui.controls;

import static io.gitlab.fxlabs.testfx.assertj.FxAssertions.assertThat;
import static io.gitlab.fxlabs.testfx.util.FxRobotService.FX_ROBOT;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxDescriptionSupplier;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * TestFX-based tests for {@link ButtonCellFactory}.
 * Tests button rendering, click handling, and button text.
 */
@TestFxApplication(ButtonCellFactoryTest.TestApp.class)
@DisplayName("ButtonCellFactory TestFX Tests")
class ButtonCellFactoryTest {

    /**
     * Test application that sets up a TableView with a ButtonCellFactory column.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        private TableView<String> tableView;
        private int clickCount;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            clickCount = 0;
            tableView = new TableView<>();
            TableColumn<String, String> column = new TableColumn<>("Actions");
            column.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()));
            column.setCellFactory(col -> new ButtonCellFactory<>("Click Me", cell -> clickCount++));
            tableView.getColumns().add(column);
            tableView.getItems().addAll("Item 1", "Item 2", "Item 3");
            primaryStage.setScene(new Scene(new StackPane(tableView), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    /**
     * Verifies that a button is rendered in each table cell for every row.
     */
    @Test
    @DisplayName("Should render button in each cell")
    void shouldRenderButtonInEachCell(TestApp application, TestFxDescriptionSupplier description) {
        var buttons = FX_ROBOT.selectNodes(Button.class, ".button").fromAll().toList();
        assertThat(buttons)
                .as(description)
                .hasSize(3);
    }

    /**
     * Verifies that buttons rendered in table cells display the correct button text.
     */
    @Test
    @DisplayName("Should render button with correct text")
    void shouldRenderButtonWithCorrectText(TestApp application, TestFxDescriptionSupplier description) {
        var buttons = FX_ROBOT.selectNodes(Button.class, ".button").fromAll().toList();
        assertThat(buttons)
                .as(description)
                .isNotEmpty();
        
        // Check the first button has correct text
        Button firstButton = buttons.get(0);
        assertThat(firstButton)
                .as(description)
                .extractingText()
                .isEqualTo("Click Me");
    }

    /**
     * Verifies that clicking a button fires the registered click handler, incrementing the click count.
     */
    @Test
    @DisplayName("Should handle button click")
    void shouldHandleButtonClick(TestApp application, TestFxDescriptionSupplier description) {
        var buttons = FX_ROBOT.selectNodes(Button.class, ".button").fromAll().toList();
        assertThat(buttons)
                .as(description)
                .isNotEmpty();
        
        // Fire the first button programmatically on the FX thread
        Button firstButton = buttons.get(0);
        CompletableFuture<Void> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            firstButton.fire();
            future.complete(null);
        });
        try {
            future.get(); // Wait for completion
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fire button", e);
        }
        
        assertThat(application.clickCount)
                .as(description)
                .isGreaterThan(0);
    }
}
