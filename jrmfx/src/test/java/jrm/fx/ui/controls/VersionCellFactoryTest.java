package jrm.fx.ui.controls;

import static io.gitlab.fxlabs.testfx.assertj.FxAssertions.assertThat;
import static io.gitlab.fxlabs.testfx.util.FxRobotService.FX_ROBOT;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxDescriptionSupplier;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX-based tests for {@link VersionCellFactory}.
 * Tests version string display with null handling and tooltips.
 */
@TestFxApplication(VersionCellFactoryTest.TestApp.class)
@DisplayName("VersionCellFactory TestFX Tests")
class VersionCellFactoryTest {

    /**
     * Test application that sets up a TableView with a VersionCellFactory column.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        private TableView<String> tableView;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            tableView = new TableView<>();
            TableColumn<String, String> column = new TableColumn<>("Version");
            column.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()));
            column.setCellFactory(col -> new VersionCellFactory<>());
            tableView.getColumns().add(column);
            tableView.getItems().addAll("1.0.0", "2.5.3", null, "3.0.0-beta");
            primaryStage.setScene(new Scene(new StackPane(tableView), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    @Test
    @DisplayName("Should render version cells with data")
    void shouldRenderVersionCellsWithData(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Filter for cells with non-empty text (data cells)
        var dataCells = cells.stream()
                .filter(cell -> cell.getText() != null && !cell.getText().isEmpty())
                .toList();
        
        // TableView renders at least 4 data cells (3 versions + 1 "???" for null)
        // May render more empty placeholder cells
        assertThat(dataCells)
                .as(description)
                .hasSizeGreaterThanOrEqualTo(4);
    }

    @Test
    @DisplayName("Should render version string")
    void shouldRenderVersionString(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with "1.0.0"
        var cell100 = cells.stream()
                .filter(cell -> "1.0.0".equals(cell.getText()))
                .findFirst()
                .orElse(null);
        
        assertThat(cell100)
                .as(description)
                .isNotNull();
    }

    @Test
    @DisplayName("Should render null version as ???")
    void shouldRenderNullVersionAsQuestionMarks(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with "???"
        var cellNull = cells.stream()
                .filter(cell -> "???".equals(cell.getText()))
                .findFirst()
                .orElse(null);
        
        assertThat(cellNull)
                .as(description)
                .isNotNull();
    }

    @Test
    @DisplayName("Should render beta version")
    void shouldRenderBetaVersion(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with "3.0.0-beta"
        var cellBeta = cells.stream()
                .filter(cell -> "3.0.0-beta".equals(cell.getText()))
                .findFirst()
                .orElse(null);
        
        assertThat(cellBeta)
                .as(description)
                .isNotNull();
    }
}
