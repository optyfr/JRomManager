package jrm.fx.ui.controls;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jrm.fx.ui.misc.SrcDstResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DropCell} drag-and-drop table cell.
 * <p>
 * Validates the static {@code process()} method that maps dropped files to table rows
 * and invokes callbacks, as well as cell update behavior.
 *
 * @since 3.0.5
 */
@TestFxApplication(DropCellTest.TestApp.class)
@DisplayName("DropCell Tests")
class DropCellTest {

    /**
     * Test application for JavaFX component tests.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            primaryStage.setScene(new Scene(new StackPane(), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    /** The table view used by the tests. */
    private TableView<SrcDstResult> tableView;
    /** Results collected by the drop callback. */
    private List<SrcDstResult> callbackResults;
    /** Files collected by the drop callback. */
    private List<File> callbackFiles;

    /**
     * Sets up a fresh table view and callback lists before each test.
     */
    @BeforeEach
    void setUp() {
        tableView = new TableView<>();
        callbackResults = new ArrayList<>();
        callbackFiles = new ArrayList<>();
    }

    /**
     * Verifies that a {@link DropCell} can be constructed with a valid callback.
     */
    @Test
    @DisplayName("Should create DropCell with callback")
    void shouldCreateDropCellWithCallback() {
        DropCell.DropCellCallback callback = (sdrlist, files) -> {
            callbackResults.addAll(sdrlist);
            callbackFiles.addAll(files);
        };

        DropCell cell = new DropCell(tableView, callback, file -> true);

        assertThat(cell).as("cell should not be null").isNotNull();
    }

    /**
     * Verifies that a drop into an empty table creates new rows for each dropped file
     * and invokes the callback with the correct results.
     */
    @Test
    @DisplayName("Should process drop with empty table")
    void shouldProcessDropWithEmptyTable() {
        List<File> files = List.of(
            new File("/path/file1.txt"),
            new File("/path/file2.txt")
        );

        DropCell.DropCellCallback callback = (sdrlist, droppedFiles) -> {
            callbackResults.addAll(sdrlist);
            callbackFiles.addAll(droppedFiles);
        };

        DropCell.process(tableView, 0, files, callback);

        assertThat(callbackResults).as("should have 2 results").hasSize(2);
        assertThat(callbackFiles).as("should have 2 files").hasSize(2);
        assertThat(tableView.getItems()).as("table should have 2 new items").hasSize(2);
    }

    /**
     * Verifies that a drop starting at a positive row index reuses existing table rows
     * and passes the correct results to the callback.
     */
    @Test
    @DisplayName("Should process drop at start index")
    void shouldProcessDropAtStartIndex() {
        // Add existing items
        tableView.getItems().addAll(
            new SrcDstResult(),
            new SrcDstResult(),
            new SrcDstResult()
        );

        List<File> files = List.of(
            new File("/path/file1.txt"),
            new File("/path/file2.txt")
        );

        DropCell.DropCellCallback callback = (sdrlist, droppedFiles) -> {
            callbackResults.addAll(sdrlist);
            callbackFiles.addAll(droppedFiles);
        };

        DropCell.process(tableView, 1, files, callback);

        assertThat(callbackResults).as("should have 2 results starting at index 1").hasSize(2);
        assertThat(callbackResults.get(0)).as("first result should be existing item at index 1")
            .isSameAs(tableView.getItems().get(1));
        assertThat(callbackResults.get(1)).as("second result should be existing item at index 2")
            .isSameAs(tableView.getItems().get(2));
        assertThat(callbackFiles).as("should have 2 files").hasSize(2);
        assertThat(tableView.getItems()).as("table should still have 3 items").hasSize(3);
    }

    /**
     * Verifies that when the number of dropped files extends beyond the current table
     * size, new rows are appended automatically.
     */
    @Test
    @DisplayName("Should process drop extending beyond table size")
    void shouldProcessDropExtendingBeyondTableSize() {
        // Add 2 existing items
        tableView.getItems().addAll(
            new SrcDstResult(),
            new SrcDstResult()
        );

        List<File> files = List.of(
            new File("/path/file1.txt"),
            new File("/path/file2.txt"),
            new File("/path/file3.txt")
        );

        DropCell.DropCellCallback callback = (sdrlist, droppedFiles) -> {
            callbackResults.addAll(sdrlist);
            callbackFiles.addAll(droppedFiles);
        };

        // Start at index 1, so we need items at 1, 2, 3
        // Item 1 and 2 exist, item 3 needs to be created
        DropCell.process(tableView, 1, files, callback);

        assertThat(callbackResults).as("should have 3 results").hasSize(3);
        assertThat(callbackFiles).as("should have 3 files").hasSize(3);
        assertThat(tableView.getItems()).as("table should have 4 items (2 existing + 2 new)")
            .hasSize(4);
    }

    /**
     * Verifies that a negative start index is treated as the end of the table
     * and new rows are still created.
     */
    @Test
    @DisplayName("Should handle negative start index")
    void shouldHandleNegativeStartIndex() {
        List<File> files = List.of(
            new File("/path/file1.txt"),
            new File("/path/file2.txt")
        );

        DropCell.DropCellCallback callback = (sdrlist, droppedFiles) -> {
            callbackResults.addAll(sdrlist);
            callbackFiles.addAll(droppedFiles);
        };

        // Negative index should be treated as end of table
        DropCell.process(tableView, -1, files, callback);

        assertThat(callbackResults).as("should have 2 results").hasSize(2);
        assertThat(callbackFiles).as("should have 2 files").hasSize(2);
        assertThat(tableView.getItems()).as("table should have 2 items").hasSize(2);
    }

    /**
     * Verifies that a start index larger than the table size is treated as the end
     * of the table, causing new rows to be appended.
     */
    @Test
    @DisplayName("Should handle start index beyond table size")
    void shouldHandleStartIndexBeyondTableSize() {
        tableView.getItems().add(new SrcDstResult());

        List<File> files = List.of(
            new File("/path/file1.txt"),
            new File("/path/file2.txt")
        );

        DropCell.DropCellCallback callback = (sdrlist, droppedFiles) -> {
            callbackResults.addAll(sdrlist);
            callbackFiles.addAll(droppedFiles);
        };

        // Start index 10 is beyond table size (1), should be treated as end
        DropCell.process(tableView, 10, files, callback);

        assertThat(callbackResults).as("should have 2 results").hasSize(2);
        assertThat(callbackFiles).as("should have 2 files").hasSize(2);
        assertThat(tableView.getItems()).as("table should have 3 items (1 existing + 2 new)")
            .hasSize(3);
    }

    /**
     * Verifies that an empty file list results in no callback invocations and no table changes.
     */
    @Test
    @DisplayName("Should process empty file list")
    void shouldProcessEmptyFileList() {
        List<File> files = List.of();

        DropCell.DropCellCallback callback = (sdrlist, droppedFiles) -> {
            callbackResults.addAll(sdrlist);
            callbackFiles.addAll(droppedFiles);
        };

        DropCell.process(tableView, 0, files, callback);

        assertThat(callbackResults).as("should have no results").isEmpty();
        assertThat(callbackFiles).as("should have no files").isEmpty();
        assertThat(tableView.getItems()).as("table should be empty").isEmpty();
    }

    /**
     * Verifies that a single dropped file correctly creates one row and invokes the callback.
     */
    @Test
    @DisplayName("Should process single file drop")
    void shouldProcessSingleFileDrop() {
        List<File> files = List.of(new File("/path/single.txt"));

        DropCell.DropCellCallback callback = (sdrlist, droppedFiles) -> {
            callbackResults.addAll(sdrlist);
            callbackFiles.addAll(droppedFiles);
        };

        DropCell.process(tableView, 0, files, callback);

        assertThat(callbackResults).as("should have 1 result").hasSize(1);
        assertThat(callbackFiles).as("should have 1 file").hasSize(1);
        assertThat(callbackFiles.get(0)).as("file should be single.txt").hasName("single.txt");
        assertThat(tableView.getItems()).as("table should have 1 item").hasSize(1);
    }

    /**
     * Verifies that updating a cell with text sets the text and leaves the graphic {@code null}.
     */
    @Test
    @DisplayName("Should update cell with text")
    void shouldUpdateCellWithText() {
        DropCell cell = new DropCell(tableView, (sdrlist, files) -> {}, file -> true);

        cell.updateItem("test.txt", false);

        assertThat(cell.getText()).as("cell text should be set").isEqualTo("test.txt");
        assertThat(cell.getGraphic()).as("cell graphic should be null").isNull();
    }

    /**
     * Verifies that updating a cell with {@code null} and empty flag clears the text and graphic.
     */
    @Test
    @DisplayName("Should update empty cell")
    void shouldUpdateEmptyCell() {
        DropCell cell = new DropCell(tableView, (sdrlist, files) -> {}, file -> true);

        cell.updateItem(null, true);

        assertThat(cell.getText()).as("cell text should be empty").isEmpty();
        assertThat(cell.getGraphic()).as("cell graphic should be null").isNull();
    }

    /**
     * Verifies that long text is displayed with a leading ellipsis overrun style for readability.
     */
    @Test
    @DisplayName("Should update cell with long text using leading ellipsis")
    void shouldUpdateCellWithLongTextUsingLeadingEllipsis() {
        DropCell cell = new DropCell(tableView, (sdrlist, files) -> {}, file -> true);

        String longText = "/very/long/path/to/some/file/that/needs/ellipsis.txt";
        cell.updateItem(longText, false);

        assertThat(cell.getTextOverrun()).as("text overrun should be leading ellipsis")
            .isEqualTo(javafx.scene.control.OverrunStyle.LEADING_ELLIPSIS);
        assertThat(cell.getText()).as("cell text should be set").isEqualTo(longText);
    }

    /**
     * Verifies that a tooltip is created and its text matches the cell's text.
     */
    @Test
    @DisplayName("Should create tooltip for cell with text")
    void shouldCreateTooltipForCellWithText() {
        DropCell cell = new DropCell(tableView, (sdrlist, files) -> {}, file -> true);

        cell.updateItem("tooltip-text.txt", false);

        assertThat(cell.getTooltip()).as("tooltip should be set").isNotNull();
        assertThat(cell.getTooltip().getText()).as("tooltip text should match cell text")
            .isEqualTo("tooltip-text.txt");
    }

    /**
     * Verifies that a tooltip persists from a previous non-empty state even after the cell
     * is cleared (current behavior).
     */
    @Test
    @DisplayName("Should retain tooltip when cell becomes empty")
    void shouldRetainTooltipWhenCellBecomesEmpty() {
        DropCell cell = new DropCell(tableView, (sdrlist, files) -> {}, file -> true);

        // First set text with tooltip
        cell.updateItem("text.txt", false);
        assertThat(cell.getTooltip()).as("tooltip should be set").isNotNull();
        assertThat(cell.getTooltip().getText()).as("tooltip text should match").isEqualTo("text.txt");

        // Then clear - tooltip persists (current behavior)
        cell.updateItem(null, true);
        assertThat(cell.getTooltip()).as("tooltip should still exist (persists from previous state)").isNotNull();
    }
}
