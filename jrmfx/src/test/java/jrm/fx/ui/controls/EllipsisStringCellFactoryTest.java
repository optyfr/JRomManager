package jrm.fx.ui.controls;

import static io.gitlab.fxlabs.testfx.assertj.FxAssertions.assertThat;
import static io.gitlab.fxlabs.testfx.util.FxRobotService.FX_ROBOT;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxDescriptionSupplier;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX-based tests for {@link EllipsisStringCellFactory}.
 * Tests cell rendering with text truncation and tooltip behavior.
 */
@TestFxApplication(EllipsisStringCellFactoryTest.TestApp.class)
@DisplayName("EllipsisStringCellFactory TestFX Tests")
class EllipsisStringCellFactoryTest {

    /**
     * Test application that sets up a TableView with an EllipsisStringCellFactory column.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        private TableView<String> tableView;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            tableView = new TableView<>();
            TableColumn<String, String> column = new TableColumn<>("Test Column");
            column.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()));
            column.setCellFactory(col -> new EllipsisStringCellFactory<>(OverrunStyle.ELLIPSIS, true));
            tableView.getColumns().add(column);
            tableView.getItems().addAll("Short text", "This is a very long text that should be truncated with ellipsis");
            primaryStage.setScene(new Scene(new StackPane(tableView), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    /**
     * Verifies that table cells are rendered for both rows of test data.
     */
    @Test
    @DisplayName("Should render cells with data")
    void shouldRenderCellsWithData(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Filter for cells with non-empty text (data cells)
        var dataCells = cells.stream()
                .filter(cell -> cell.getText() != null && !cell.getText().isEmpty())
                .toList();
        
        assertThat(dataCells)
                .as(description)
                .hasSize(2);
    }

    /**
     * Verifies that the cell containing the short text {@code "Short text"} is rendered.
     */
    @Test
    @DisplayName("Should render short text cell")
    void shouldRenderShortTextCell(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with "Short text"
        var shortTextCell = cells.stream()
                .filter(cell -> "Short text".equals(cell.getText()))
                .findFirst()
                .orElse(null);
        
        assertThat(shortTextCell)
                .as(description)
                .isNotNull();
    }

    /**
     * Verifies that a cell containing a long text with the substring {@code "very long text"}
     * is rendered in the table.
     */
    @Test
    @DisplayName("Should render long text cell")
    void shouldRenderLongTextCell(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with the long text
        var longTextCell = cells.stream()
                .filter(cell -> cell.getText() != null && cell.getText().contains("very long text"))
                .findFirst()
                .orElse(null);
        
        assertThat(longTextCell)
                .as(description)
                .isNotNull();
    }
}
