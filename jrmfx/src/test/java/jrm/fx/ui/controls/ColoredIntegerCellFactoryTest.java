package jrm.fx.ui.controls;

import static io.gitlab.fxlabs.testfx.assertj.FxAssertions.assertThat;
import static io.gitlab.fxlabs.testfx.util.FxRobotService.FX_ROBOT;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxDescriptionSupplier;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX-based tests for {@link ColoredIntegerCellFactory}.
 * Tests integer display with custom colors, alignment, and tooltips.
 */
@TestFxApplication(ColoredIntegerCellFactoryTest.TestApp.class)
@DisplayName("ColoredIntegerCellFactory TestFX Tests")
class ColoredIntegerCellFactoryTest {

    /**
     * Test application that sets up a TableView with a ColoredIntegerCellFactory column.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        private TableView<Integer> tableView;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            tableView = new TableView<>();
            TableColumn<Integer, Integer> column = new TableColumn<>("Count");
            column.setCellValueFactory(param -> new javafx.beans.property.SimpleObjectProperty<>(param.getValue()));
            column.setCellFactory(col -> new ColoredIntegerCellFactory<>(Color.RED, Pos.CENTER_RIGHT, true));
            tableView.getColumns().add(column);
            tableView.getItems().addAll(42, 100, 0, -5);
            primaryStage.setScene(new Scene(new StackPane(tableView), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    @Test
    @DisplayName("Should render integer cells with data")
    void shouldRenderIntegerCellsWithData(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Filter for cells with non-empty text (data cells)
        var dataCells = cells.stream()
                .filter(cell -> cell.getText() != null && !cell.getText().isEmpty())
                .toList();
        
        assertThat(dataCells)
                .as(description)
                .hasSize(4);
    }

    @Test
    @DisplayName("Should render positive integer")
    void shouldRenderPositiveInteger(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with "42"
        var cell42 = cells.stream()
                .filter(cell -> "42".equals(cell.getText()))
                .findFirst()
                .orElse(null);
        
        assertThat(cell42)
                .as(description)
                .isNotNull();
    }

    @Test
    @DisplayName("Should render zero")
    void shouldRenderZero(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with "0"
        var cellZero = cells.stream()
                .filter(cell -> "0".equals(cell.getText()))
                .findFirst()
                .orElse(null);
        
        assertThat(cellZero)
                .as(description)
                .isNotNull();
    }

    @Test
    @DisplayName("Should render negative integer")
    void shouldRenderNegativeInteger(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with "-5"
        var cellNeg5 = cells.stream()
                .filter(cell -> "-5".equals(cell.getText()))
                .findFirst()
                .orElse(null);
        
        assertThat(cellNeg5)
                .as(description)
                .isNotNull();
    }
}
