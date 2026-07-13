package jrm.fx.ui.status;

import static io.gitlab.fxlabs.testfx.assertj.FxAssertions.assertThat;

import java.util.List;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX-based tests for {@link NeutralToNodeFormatter}.
 * Tests XML parsing and conversion of neutral markup to JavaFX nodes.
 */
@TestFxApplication(NeutralToNodeFormatterTest.TestApp.class)
@DisplayName("NeutralToNodeFormatter TestFX Tests")
class NeutralToNodeFormatterTest {

    /**
     * Test application that initializes JavaFX toolkit.
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

    @Test
    @DisplayName("Should return empty list for null input")
    void shouldReturnEmptyListForNullInput() {
        List<Node> nodes = NeutralToNodeFormatter.toNodes(null);
        
        assertThat(nodes)
                .as("Null input should return empty list")
                .isEmpty();
    }

    @Test
    @DisplayName("Should return single label for plain text")
    void shouldReturnSingleLabelForPlainText() {
        List<Node> nodes = NeutralToNodeFormatter.toNodes("Plain text");
        
        assertThat(nodes)
                .as("Plain text should return single node")
                .hasSize(1);
        assertThat(nodes.get(0))
                .as("Node should be a Label")
                .isInstanceOf(Label.class);
        Label label = (Label) nodes.get(0);
        assertThat(label.getText())
                .as("Label text should match input")
                .isEqualTo("Plain text");
    }

    @Test
    @DisplayName("Should return single label for empty string")
    void shouldReturnSingleLabelForEmptyString() {
        List<Node> nodes = NeutralToNodeFormatter.toNodes("");
        
        assertThat(nodes)
                .as("Empty string should return single node")
                .hasSize(1);
        assertThat(nodes.get(0))
                .as("Node should be a Label")
                .isInstanceOf(Label.class);
        Label label = (Label) nodes.get(0);
        assertThat(label.getText())
                .as("Label text should be empty")
                .isEmpty();
    }

    @Test
    @DisplayName("Should parse simple label")
    void shouldParseSimpleLabel() {
        String xml = "<document><label>Test Label</label></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return single node")
                .hasSize(1);
        assertThat(nodes.get(0))
                .as("Node should be a Label")
                .isInstanceOf(Label.class);
        Label label = (Label) nodes.get(0);
        assertThat(label.getText())
                .as("Label text should match")
                .isEqualTo("Test Label");
    }

    @Test
    @DisplayName("Should parse label with color attribute")
    void shouldParseLabelWithColorAttribute() {
        String xml = "<document><label color=\"#FF0000\">Red Text</label></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return single node")
                .hasSize(1);
        Label label = (Label) nodes.get(0);
        assertThat(label.getText())
                .as("Label text should match")
                .isEqualTo("Red Text");
        assertThat(label.getTextFill())
                .as("Label color should be red")
                .isEqualTo(Color.RED);
    }

    @Test
    @DisplayName("Should parse label with bold attribute")
    void shouldParseLabelWithBoldAttribute() {
        String xml = "<document><label bold=\"true\">Bold Text</label></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return single node")
                .hasSize(1);
        Label label = (Label) nodes.get(0);
        assertThat(label.getText())
                .as("Label text should match")
                .isEqualTo("Bold Text");
        assertThat(label.getStyle())
                .as("Label style should contain bold")
                .contains("-fx-font-weight: bold;");
    }

    @Test
    @DisplayName("Should parse label with italic attribute")
    void shouldParseLabelWithItalicAttribute() {
        String xml = "<document><label italic=\"true\">Italic Text</label></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return single node")
                .hasSize(1);
        Label label = (Label) nodes.get(0);
        assertThat(label.getText())
                .as("Label text should match")
                .isEqualTo("Italic Text");
        assertThat(label.getStyle())
                .as("Label style should contain italic")
                .contains("-fx-font-style: italic;");
    }

    @Test
    @DisplayName("Should parse label with both bold and italic attributes")
    void shouldParseLabelWithBothBoldAndItalicAttributes() {
        String xml = "<document><label bold=\"true\" italic=\"true\">Bold Italic</label></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return single node")
                .hasSize(1);
        Label label = (Label) nodes.get(0);
        assertThat(label.getText())
                .as("Label text should match")
                .isEqualTo("Bold Italic");
        assertThat(label.getStyle())
                .as("Label style should contain bold")
                .contains("-fx-font-weight: bold;");
        assertThat(label.getStyle())
                .as("Label style should contain italic")
                .contains("-fx-font-style: italic;");
    }

    @Test
    @DisplayName("Should parse progress bar with default values")
    void shouldParseProgressBarWithDefaultValues() {
        String xml = "<document><progress/></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return single node")
                .hasSize(1);
        assertThat(nodes.get(0))
                .as("Node should be a ProgressBar")
                .isInstanceOf(ProgressBar.class);
        ProgressBar progressBar = (ProgressBar) nodes.get(0);
        assertThat(progressBar.getPrefWidth())
                .as("Default width should be 100.0")
                .isEqualTo(100.0);
        assertThat(progressBar.getPrefHeight())
                .as("Default height should be 10.0")
                .isEqualTo(10.0);
        assertThat(progressBar.getProgress())
                .as("Default progress should be 0.0")
                .isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should parse progress bar with custom values")
    void shouldParseProgressBarWithCustomValues() {
        String xml = "<document><progress width=\"200\" value=\"50\" max=\"100\"/></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return single node")
                .hasSize(1);
        ProgressBar progressBar = (ProgressBar) nodes.get(0);
        assertThat(progressBar.getPrefWidth())
                .as("Width should be 200.0")
                .isEqualTo(200.0);
        assertThat(progressBar.getProgress())
                .as("Progress should be 0.5")
                .isEqualTo(0.5);
    }

    @Test
    @DisplayName("Should parse progress bar with partial progress")
    void shouldParseProgressBarWithPartialProgress() {
        String xml = "<document><progress width=\"150\" value=\"25\" max=\"200\"/></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return single node")
                .hasSize(1);
        ProgressBar progressBar = (ProgressBar) nodes.get(0);
        assertThat(progressBar.getPrefWidth())
                .as("Width should be 150.0")
                .isEqualTo(150.0);
        assertThat(progressBar.getProgress())
                .as("Progress should be 0.125")
                .isEqualTo(0.125);
    }

    @Test
    @DisplayName("Should parse progress bar with full progress")
    void shouldParseProgressBarWithFullProgress() {
        String xml = "<document><progress width=\"100\" value=\"100\" max=\"100\"/></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return single node")
                .hasSize(1);
        ProgressBar progressBar = (ProgressBar) nodes.get(0);
        assertThat(progressBar.getProgress())
                .as("Progress should be 1.0")
                .isEqualTo(1.0);
    }

    @Test
    @DisplayName("Should parse document with label and progress")
    void shouldParseDocumentWithLabelAndProgress() {
        String xml = "<document><label>Status:</label><progress value=\"75\" max=\"100\"/></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return two nodes")
                .hasSize(2);
        assertThat(nodes.get(0))
                .as("First node should be a Label")
                .isInstanceOf(Label.class);
        assertThat(nodes.get(1))
                .as("Second node should be a ProgressBar")
                .isInstanceOf(ProgressBar.class);
        
        Label label = (Label) nodes.get(0);
        assertThat(label.getText())
                .as("Label text should match")
                .isEqualTo("Status:");
        
        ProgressBar progressBar = (ProgressBar) nodes.get(1);
        assertThat(progressBar.getProgress())
                .as("Progress should be 0.75")
                .isEqualTo(0.75);
    }

    @Test
    @DisplayName("Should parse document with multiple labels")
    void shouldParseDocumentWithMultipleLabels() {
        String xml = "<document><label>First</label><label>Second</label><label>Third</label></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return three nodes")
                .hasSize(3);
        assertThat(((Label) nodes.get(0)).getText())
                .as("First label text should match")
                .isEqualTo("First");
        assertThat(((Label) nodes.get(1)).getText())
                .as("Second label text should match")
                .isEqualTo("Second");
        assertThat(((Label) nodes.get(2)).getText())
                .as("Third label text should match")
                .isEqualTo("Third");
    }

    @Test
    @DisplayName("Should parse document with multiple progress bars")
    void shouldParseDocumentWithMultipleProgressBars() {
        String xml = "<document><progress value=\"25\" max=\"100\"/><progress value=\"50\" max=\"100\"/><progress value=\"75\" max=\"100\"/></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return three nodes")
                .hasSize(3);
        assertThat(((ProgressBar) nodes.get(0)).getProgress())
                .as("First progress should be 0.25")
                .isEqualTo(0.25);
        assertThat(((ProgressBar) nodes.get(1)).getProgress())
                .as("Second progress should be 0.5")
                .isEqualTo(0.5);
        assertThat(((ProgressBar) nodes.get(2)).getProgress())
                .as("Third progress should be 0.75")
                .isEqualTo(0.75);
    }

    @Test
    @DisplayName("Should parse complex document with mixed elements")
    void shouldParseComplexDocumentWithMixedElements() {
        String xml = "<document><label color=\"#00FF00\">Success</label><progress value=\"100\" max=\"100\"/><label>Complete</label></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Should return three nodes")
                .hasSize(3);
        
        Label label1 = (Label) nodes.get(0);
        assertThat(label1.getText())
                .as("First label text should match")
                .isEqualTo("Success");
        assertThat(label1.getTextFill())
                .as("First label color should be lime")
                .isEqualTo(Color.LIME);
        
        ProgressBar progressBar = (ProgressBar) nodes.get(1);
        assertThat(progressBar.getProgress())
                .as("Progress should be 1.0")
                .isEqualTo(1.0);
        
        Label label2 = (Label) nodes.get(2);
        assertThat(label2.getText())
                .as("Second label text should match")
                .isEqualTo("Complete");
    }

    @Test
    @DisplayName("Should handle malformed XML gracefully")
    void shouldHandleMalformedXmlGracefully() {
        String xml = "<document><label>Unclosed";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Malformed XML should return single node")
                .hasSize(1);
        assertThat(nodes.get(0))
                .as("Node should be a Label")
                .isInstanceOf(Label.class);
    }

    @Test
    @DisplayName("Should handle empty document")
    void shouldHandleEmptyDocument() {
        String xml = "<document></document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Empty document should return empty list")
                .isEmpty();
    }

    @Test
    @DisplayName("Should handle document with only whitespace")
    void shouldHandleDocumentWithOnlyWhitespace() {
        String xml = "<document>   </document>";
        List<Node> nodes = NeutralToNodeFormatter.toNodes(xml);
        
        assertThat(nodes)
                .as("Whitespace document should return single node")
                .hasSize(1);
        assertThat(nodes.get(0))
                .as("Node should be a Label")
                .isInstanceOf(Label.class);
    }
}
