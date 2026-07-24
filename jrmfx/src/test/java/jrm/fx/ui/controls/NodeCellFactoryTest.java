package jrm.fx.ui.controls;

import static org.assertj.core.api.Assertions.assertThat;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX-based tests for {@link NodeCellFactory}.
 * <p>
 * Tests the table cell factory that renders neutral markup as JavaFX nodes.
 *
 * @since 3.0.5
 */
@TestFxApplication(NodeCellFactoryTest.TestApp.class)
@DisplayName("NodeCellFactory TestFX Tests")
class NodeCellFactoryTest {

    /**
     * Test application that sets up a TableView with a NodeCellFactory column.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            
            TableView<String> tableView = new TableView<>();
            TableColumn<String, String> column = new TableColumn<>("Content");
            column.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()));
            column.setCellFactory(col -> new NodeCellFactory<>());
            tableView.getColumns().add(column);
            tableView.getItems().addAll("Test Item 1", "Test Item 2", null);
            
            primaryStage.setScene(new Scene(new StackPane(tableView), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    /** The cell factory under test. */
    private NodeCellFactory<String> cellFactory;
    /** The table view used by the tests. */
    private TableView<String> tableView;
    /** The table column bound to the cell factory. */
    private TableColumn<String, String> column;

    /**
     * Initializes a fresh cell factory, table view, and column before each test.
     */
    @BeforeEach
    void setUp() {
        tableView = new TableView<>();
        column = new TableColumn<>("Test Column");
        column.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()));
        column.setCellFactory(col -> new NodeCellFactory<>());
        tableView.getColumns().add(column);
        tableView.getItems().addAll("Test Item 1", "Test Item 2", null);
        
        cellFactory = new NodeCellFactory<>();
    }

    /**
     * Verifies that a {@link NodeCellFactory} can be constructed.
     */
    @Test
    @DisplayName("Should create NodeCellFactory")
    void shouldCreateNodeCellFactory() {
        assertThat(cellFactory).as("Cell factory should be created").isNotNull();
    }

    /**
     * Verifies that the text and graphic are cleared when the item is {@code null}.
     */
    @Test
    @DisplayName("Should set text empty for null item")
    void shouldSetTextEmptyForNullItem() {
        cellFactory.updateItem(null, false);

        assertThat(cellFactory.getText()).as("Text should be empty").isEmpty();
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    /**
     * Verifies that the text and graphic are cleared when the cell is marked as empty.
     */
    @Test
    @DisplayName("Should set text empty for empty cell")
    void shouldSetTextEmptyForEmptyCell() {
        cellFactory.updateItem("Test Content", true);

        assertThat(cellFactory.getText()).as("Text should be empty").isEmpty();
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    /**
     * Verifies that a non-empty item produces a {@link TextFlow} graphic with text left null or empty.
     */
    @Test
    @DisplayName("Should set graphic for non-empty item")
    void shouldSetGraphicForNonEmptyItem() {
        cellFactory.updateItem("Test Content", false);

        // NodeCellFactory doesn't set text when item is not null/empty, so it's null
        assertThat(cellFactory.getText()).as("Text should be null or empty").isNullOrEmpty();
        // Graphic should be TextFlow when item is not null and not empty
        assertThat(cellFactory.getGraphic()).as("Graphic should not be null").isNotNull();
        assertThat(cellFactory.getGraphic()).as("Graphic should be TextFlow").isInstanceOf(TextFlow.class);
    }

    /**
     * Verifies that the cell content is center-aligned.
     */
    @Test
    @DisplayName("Should center align cell content")
    void shouldCenterAlignCellContent() {
        cellFactory.updateItem("Test Content", false);

        assertThat(cellFactory.getAlignment()).as("Cell should be center aligned").isEqualTo(Pos.CENTER);
    }

    /**
     * Verifies that a plain text item produces a {@link TextFlow} with at least one child node.
     */
    @Test
    @DisplayName("Should create TextFlow with children for plain text")
    void shouldCreateTextFlowWithChildrenForPlainText() {
        cellFactory.updateItem("Plain text content", false);

        Node graphic = cellFactory.getGraphic();
        assertThat(graphic).as("Graphic should be TextFlow").isInstanceOf(TextFlow.class);
        
        TextFlow textFlow = (TextFlow) graphic;
        assertThat(textFlow.getChildren()).as("TextFlow should have children").isNotEmpty();
    }

    /**
     * Verifies that XML content is parsed into a {@link TextFlow} with child nodes.
     */
    @Test
    @DisplayName("Should create TextFlow with children for XML content")
    void shouldCreateTextFlowWithChildrenForXmlContent() {
        String xmlContent = "<document><label>Test Label</label></document>";
        cellFactory.updateItem(xmlContent, false);

        Node graphic = cellFactory.getGraphic();
        assertThat(graphic).as("Graphic should be TextFlow").isInstanceOf(TextFlow.class);
        
        TextFlow textFlow = (TextFlow) graphic;
        assertThat(textFlow.getChildren()).as("TextFlow should have children for XML").isNotEmpty();
    }

    /**
     * Verifies that the {@link TextFlow} text alignment is set to {@code LEFT}.
     */
    @Test
    @DisplayName("Should set TextFlow alignment to LEFT")
    void shouldSetTextFlowAlignmentToLeft() {
        cellFactory.updateItem("Test Content", false);

        Node graphic = cellFactory.getGraphic();
        assertThat(graphic).as("Graphic should be TextFlow").isInstanceOf(TextFlow.class);
        
        TextFlow textFlow = (TextFlow) graphic;
        assertThat(textFlow.getTextAlignment()).as("TextFlow alignment should be LEFT")
            .isEqualTo(javafx.scene.text.TextAlignment.LEFT);
    }

    /**
     * Verifies that the {@link TextFlow} minimum width is set to {@link javafx.scene.layout.Region#USE_PREF_SIZE}.
     */
    @Test
    @DisplayName("Should set TextFlow min width to USE_PREF_SIZE")
    void shouldSetTextFlowMinWidthToUsePrefSize() {
        cellFactory.updateItem("Test Content", false);

        Node graphic = cellFactory.getGraphic();
        TextFlow textFlow = (TextFlow) graphic;
        
        assertThat(textFlow.getMinWidth()).as("TextFlow min width should be USE_PREF_SIZE")
            .isEqualTo(StackPane.USE_PREF_SIZE);
    }

    /**
     * Verifies that the {@link TextFlow} preferred width is set to {@link javafx.scene.layout.Region#USE_COMPUTED_SIZE}.
     */
    @Test
    @DisplayName("Should set TextFlow pref width to USE_COMPUTED_SIZE")
    void shouldSetTextFlowPrefWidthToUseComputedSize() {
        cellFactory.updateItem("Test Content", false);

        Node graphic = cellFactory.getGraphic();
        TextFlow textFlow = (TextFlow) graphic;
        
        assertThat(textFlow.getPrefWidth()).as("TextFlow pref width should be USE_COMPUTED_SIZE")
            .isEqualTo(StackPane.USE_COMPUTED_SIZE);
    }

    /**
     * Verifies that the {@link TextFlow} preferred height is set to {@link javafx.scene.layout.Region#USE_PREF_SIZE}.
     */
    @Test
    @DisplayName("Should set TextFlow pref height to USE_PREF_SIZE")
    void shouldSetTextFlowPrefHeightToUsePrefSize() {
        cellFactory.updateItem("Test Content", false);

        Node graphic = cellFactory.getGraphic();
        TextFlow textFlow = (TextFlow) graphic;
        
        assertThat(textFlow.getPrefHeight()).as("TextFlow pref height should be USE_PREF_SIZE")
            .isEqualTo(StackPane.USE_PREF_SIZE);
    }

    /**
     * Verifies that the graphic is cleared when the item transitions from non-null to {@code null}.
     */
    @Test
    @DisplayName("Should clear graphic when item becomes null")
    void shouldClearGraphicWhenItemBecomesNull() {
        cellFactory.updateItem("Test Content", false);
        assertThat(cellFactory.getGraphic()).as("Graphic should be set").isNotNull();

        cellFactory.updateItem(null, false);
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    /**
     * Verifies that the graphic is cleared when the cell transitions from non-empty to empty.
     */
    @Test
    @DisplayName("Should clear graphic when cell becomes empty")
    void shouldClearGraphicWhenCellBecomesEmpty() {
        cellFactory.updateItem("Test Content", false);
        assertThat(cellFactory.getGraphic()).as("Graphic should be set").isNotNull();

        cellFactory.updateItem("Test Content", true);
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    /**
     * Verifies that the cell style is empty after updating with a {@code null} item.
     */
    @Test
    @DisplayName("Should clear style when item is null")
    void shouldClearStyleWhenItemIsNull() {
        cellFactory.updateItem(null, false);

        assertThat(cellFactory.getStyle()).as("Style should be empty").isEmpty();
    }

    /**
     * Verifies that an empty string item still produces a {@link TextFlow} graphic.
     */
    @Test
    @DisplayName("Should handle empty string")
    void shouldHandleEmptyString() {
        cellFactory.updateItem("", false);

        // Empty string is not null, so it creates a TextFlow (possibly with no children)
        assertThat(cellFactory.getGraphic()).as("Graphic should be TextFlow for empty string")
            .isInstanceOf(TextFlow.class);
    }
}
