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
 * TestFX-based tests for {@link NameCellFactory}.
 * Tests editable text field with tooltip support.
 */
@TestFxApplication(NameCellFactoryTest.TestApp.class)
@DisplayName("NameCellFactory TestFX Tests")
class NameCellFactoryTest {

    /**
     * Test application that sets up a TableView with a NameCellFactory column.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        private TableView<String> tableView;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            tableView = new TableView<>();
            TableColumn<String, String> column = new TableColumn<>("Name");
            column.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()));
            column.setCellFactory(col -> new NameCellFactory<>());
            tableView.getColumns().add(column);
            tableView.getItems().addAll("Game 1", "Game 2", "Game 3");
            primaryStage.setScene(new Scene(new StackPane(tableView), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    @Test
    @DisplayName("Should render name cells with data")
    void shouldRenderNameCellsWithData(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Filter for cells with non-empty text (data cells)
        var dataCells = cells.stream()
                .filter(cell -> cell.getText() != null && !cell.getText().isEmpty())
                .toList();
        
        assertThat(dataCells)
                .as(description)
                .hasSize(3);
    }

    @Test
    @DisplayName("Should render first game name")
    void shouldRenderFirstGameName(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with "Game 1"
        var cellGame1 = cells.stream()
                .filter(cell -> "Game 1".equals(cell.getText()))
                .findFirst()
                .orElse(null);
        
        assertThat(cellGame1)
                .as(description)
                .isNotNull();
    }

    @Test
    @DisplayName("Should render second game name")
    void shouldRenderSecondGameName(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with "Game 2"
        var cellGame2 = cells.stream()
                .filter(cell -> "Game 2".equals(cell.getText()))
                .findFirst()
                .orElse(null);
        
        assertThat(cellGame2)
                .as(description)
                .isNotNull();
    }

    @Test
    @DisplayName("Should render third game name")
    void shouldRenderThirdGameName(TestApp application, TestFxDescriptionSupplier description) {
        var cells = FX_ROBOT.selectNodes(TableCell.class, ".table-cell").fromAll().toList();
        
        // Find the cell with "Game 3"
        var cellGame3 = cells.stream()
                .filter(cell -> "Game 3".equals(cell.getText()))
                .findFirst()
                .orElse(null);
        
        assertThat(cellGame3)
                .as(description)
                .isNotNull();
    }
}
