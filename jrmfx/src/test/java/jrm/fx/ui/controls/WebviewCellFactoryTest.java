package jrm.fx.ui.controls;

import static org.assertj.core.api.Assertions.assertThat;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * TestFX tests for {@link WebviewCellFactory}.
 * <p>
 * Tests the table cell factory that renders HTML content in an embedded WebView.
 *
 * @since 3.0.5
 */
@TestFxApplication(WebviewCellFactoryTest.TestApp.class)
@DisplayName("WebviewCellFactory Tests")
class WebviewCellFactoryTest {

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

    private WebviewCellFactory<Object, String> cellFactory;
    private TableColumn<Object, String> tableColumn;

    @BeforeEach
    void setUp() {
        cellFactory = new WebviewCellFactory<>();
        tableColumn = new TableColumn<>();
    }

    @Test
    @DisplayName("Should create WebviewCellFactory")
    void shouldCreateWebviewCellFactory() {
        assertThat(cellFactory).as("Cell factory should be created").isNotNull();
    }

    @Test
    @DisplayName("Should create cell when calling factory")
    void shouldCreateCellWhenCallingFactory() {
        TableCell<Object, String> cell = cellFactory.call(tableColumn);

        assertThat(cell).as("Cell should be created").isNotNull();
    }

    @Test
    @DisplayName("Should return TableCell instance")
    void shouldReturnTableCellInstance() {
        TableCell<Object, String> cell = cellFactory.call(tableColumn);

        assertThat(cell).as("Cell should be a TableCell").isInstanceOf(TableCell.class);
    }

    /**
     * Helper method to invoke the protected updateItem method using reflection on the FX application thread.
     *
     * @param cell the table cell to update
     * @param item the item to set
     * @param empty whether the cell is empty
     */
    private void invokeUpdateItem(TableCell<Object, String> cell, String item, boolean empty) {
        try {
            CompletableFuture<Void> future = new CompletableFuture<>();
            Platform.runLater(() -> {
                try {
                    var method = javafx.scene.control.Cell.class.getDeclaredMethod("updateItem", Object.class, boolean.class);
                    method.setAccessible(true);
                    method.invoke(cell, item, empty);
                    future.complete(null);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            });
            future.get(); // Wait for completion
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to invoke updateItem", e);
        }
    }

    @Test
    @DisplayName("Should create cell with null item")
    void shouldCreateCellWithNullItem() {
        TableCell<Object, String> cell = cellFactory.call(tableColumn);
        
        invokeUpdateItem(cell, null, false);

        assertThat(cell.getText()).as("Text should be null").isNull();
        assertThat(cell.getGraphic()).as("Graphic should be null").isNull();
    }

    @Test
    @DisplayName("Should create cell with empty state")
    void shouldCreateCellWithEmptyState() {
        TableCell<Object, String> cell = cellFactory.call(tableColumn);
        
        invokeUpdateItem(cell, "test", true);

        assertThat(cell.getText()).as("Text should be null").isNull();
        assertThat(cell.getGraphic()).as("Graphic should be null").isNull();
    }

    @Test
    @DisplayName("Should create WebView for non-empty HTML content")
    void shouldCreateWebViewForNonEmptyHtmlContent() {
        TableCell<Object, String> cell = cellFactory.call(tableColumn);
        String htmlContent = "<p>Test HTML</p>";
        
        invokeUpdateItem(cell, htmlContent, false);

        assertThat(cell.getGraphic()).as("Graphic should be set").isNotNull();
        assertThat(cell.getGraphic()).as("Graphic should be a WebView").isInstanceOf(WebView.class);
    }

    @Test
    @DisplayName("Should configure WebView with correct properties")
    void shouldConfigureWebViewWithCorrectProperties() {
        TableCell<Object, String> cell = cellFactory.call(tableColumn);
        String htmlContent = "<p>Test</p>";
        
        invokeUpdateItem(cell, htmlContent, false);

        WebView webView = (WebView) cell.getGraphic();
        assertThat(webView.getPrefHeight()).as("WebView prefHeight should be -1").isEqualTo(-1);
        assertThat(webView.getFontScale()).as("WebView fontScale should be 0.75").isEqualTo(0.75);
    }

    @Test
    @DisplayName("Should clear WebView when item becomes null")
    void shouldClearWebViewWhenItemBecomesNull() {
        TableCell<Object, String> cell = cellFactory.call(tableColumn);
        
        // First set HTML content
        invokeUpdateItem(cell, "<p>Test</p>", false);
        assertThat(cell.getGraphic()).as("Graphic should be set initially").isNotNull();
        
        // Then set to null
        invokeUpdateItem(cell, null, false);
        
        assertThat(cell.getGraphic()).as("Graphic should be null after setting null item").isNull();
    }

    @Test
    @DisplayName("Should clear WebView when cell becomes empty")
    void shouldClearWebViewWhenCellBecomesEmpty() {
        TableCell<Object, String> cell = cellFactory.call(tableColumn);
        
        // First set HTML content
        invokeUpdateItem(cell, "<p>Test</p>", false);
        assertThat(cell.getGraphic()).as("Graphic should be set initially").isNotNull();
        
        // Then set to empty
        invokeUpdateItem(cell, "<p>Test</p>", true);
        
        assertThat(cell.getGraphic()).as("Graphic should be null after setting empty state").isNull();
    }

    @Test
    @DisplayName("Should handle different HTML content")
    void shouldHandleDifferentHtmlContent() {
        TableCell<Object, String> cell = cellFactory.call(tableColumn);
        
        // Test with simple HTML
        invokeUpdateItem(cell, "<div>Simple</div>", false);
        assertThat(cell.getGraphic()).as("Graphic should be set for simple HTML").isNotNull();
        
        // Test with complex HTML
        invokeUpdateItem(cell, "<table><tr><td>Complex</td></tr></table>", false);
        assertThat(cell.getGraphic()).as("Graphic should be set for complex HTML").isNotNull();
    }

    @Test
    @DisplayName("Should create new WebView for each cell")
    void shouldCreateNewWebViewForEachCell() {
        TableCell<Object, String> cell1 = cellFactory.call(tableColumn);
        TableCell<Object, String> cell2 = cellFactory.call(tableColumn);
        
        invokeUpdateItem(cell1, "<p>Cell 1</p>", false);
        invokeUpdateItem(cell2, "<p>Cell 2</p>", false);

        WebView webView1 = (WebView) cell1.getGraphic();
        WebView webView2 = (WebView) cell2.getGraphic();
        
        assertThat(webView1).as("Each cell should have its own WebView instance").isNotSameAs(webView2);
    }

    @Test
    @DisplayName("Should handle empty string content")
    void shouldHandleEmptyStringContent() {
        TableCell<Object, String> cell = cellFactory.call(tableColumn);
        
        invokeUpdateItem(cell, "", false);

        // Empty string is not null, so WebView should still be created
        assertThat(cell.getGraphic()).as("Graphic should be set for empty string").isNotNull();
        assertThat(cell.getGraphic()).as("Graphic should be a WebView").isInstanceOf(WebView.class);
    }
}
