package jrm.fx.ui.profile.manager;

import static org.assertj.core.api.Assertions.assertThat;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import jrm.profile.manager.ProfileNFOStats.HaveNTotal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX tests for {@link HaveNTotalCellFactory}.
 * <p>
 * Tests the table cell factory that displays have/total statistics with color coding
 * based on completion percentage and row selection state.
 *
 * @since 3.0.5
 */
@TestFxApplication(HaveNTotalCellFactoryTest.TestApp.class)
@DisplayName("HaveNTotalCellFactory Tests")
class HaveNTotalCellFactoryTest {

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

    private HaveNTotalCellFactory<Object> cellFactory;
    private TableView<Object> tableView;
    private TableRow<Object> tableRow;

    @BeforeEach
    void setUp() {
        cellFactory = new HaveNTotalCellFactory<>();
        tableView = new TableView<>();
        tableRow = new TableRow<>();
        cellFactory.updateTableView(tableView);
        cellFactory.updateIndex(-1); // Initialize the cell
        // Set up the table row
        cellFactory.updateTableRow(tableRow);
    }

    /**
     * Sets the selected state of the table row using reflection.
     *
     * @param selected the selected state to set
     */
    private void setRowSelected(boolean selected) {
        try {
            var method = javafx.scene.control.Cell.class.getDeclaredMethod("setSelected", boolean.class);
            method.setAccessible(true);
            method.invoke(tableRow, selected);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set selected state", e);
        }
    }

    @Test
    @DisplayName("Should create HaveNTotalCellFactory")
    void shouldCreateHaveNTotalCellFactory() {
        assertThat(cellFactory).as("Cell factory should be created").isNotNull();
    }

    @Test
    @DisplayName("Should display empty text for null value")
    void shouldDisplayEmptyTextForNullValue() {
        cellFactory.updateItem(null, false);

        assertThat(cellFactory.getText()).as("Text should be empty").isEmpty();
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    @Test
    @DisplayName("Should display empty text for empty cell")
    void shouldDisplayEmptyTextForEmptyCell() {
        HaveNTotal hnt = new HaveNTotal(5L, 10L);
        cellFactory.updateItem(hnt, true);

        assertThat(cellFactory.getText()).as("Text should be empty for empty cell").isEmpty();
        assertThat(cellFactory.getGraphic()).as("Graphic should be null for empty cell").isNull();
    }

    @Test
    @DisplayName("Should display ?/? when both have and total are null")
    void shouldDisplayQuestionMarksWhenBothNull() {
        HaveNTotal hnt = new HaveNTotal(null, null);
        setRowSelected(false);
        
        cellFactory.updateItem(hnt, false);

        assertThat(cellFactory.getText()).as("Text should be ?/?").isEqualTo("?/?");
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    @Test
    @DisplayName("Should display ?/total when have is null but total is not")
    void shouldDisplayQuestionMarkForHaveWhenTotalNotNull() {
        HaveNTotal hnt = new HaveNTotal(null, 10L);
        setRowSelected(false);
        
        cellFactory.updateItem(hnt, false);

        assertThat(cellFactory.getGraphic()).as("Graphic should be HBox").isInstanceOf(HBox.class);
        HBox hbox = (HBox) cellFactory.getGraphic();
        assertThat(hbox.getChildren()).as("HBox should have 2 Text nodes").hasSize(2);
        
        Text haveText = (Text) hbox.getChildren().get(0);
        Text totalText = (Text) hbox.getChildren().get(1);
        
        assertThat(haveText.getText()).as("Have text should be ?").isEqualTo("?");
        assertThat(totalText.getText()).as("Total text should be /10").isEqualTo("/10");
    }

    @Test
    @DisplayName("Should display have/total when both are not null")
    void shouldDisplayHaveAndTotalWhenBothNotNull() {
        HaveNTotal hnt = new HaveNTotal(5L, 10L);
        setRowSelected(false);
        
        cellFactory.updateItem(hnt, false);

        assertThat(cellFactory.getGraphic()).as("Graphic should be HBox").isInstanceOf(HBox.class);
        HBox hbox = (HBox) cellFactory.getGraphic();
        assertThat(hbox.getChildren()).as("HBox should have 2 Text nodes").hasSize(2);
        
        Text haveText = (Text) hbox.getChildren().get(0);
        Text totalText = (Text) hbox.getChildren().get(1);
        
        assertThat(haveText.getText()).as("Have text should be 5").isEqualTo("5");
        assertThat(totalText.getText()).as("Total text should be /10").isEqualTo("/10");
    }

    @Test
    @DisplayName("Should use gray color for ? when row is not selected")
    void shouldUseGrayColorForQuestionMarkWhenNotSelected() {
        HaveNTotal hnt = new HaveNTotal(null, 10L);
        setRowSelected(false);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        Text haveText = (Text) hbox.getChildren().get(0);
        
        assertThat(haveText.getFill()).as("Have text should be gray").isEqualTo(Color.GRAY);
    }

    @Test
    @DisplayName("Should use light gray color for ? when row is selected")
    void shouldUseLightGrayColorForQuestionMarkWhenSelected() {
        HaveNTotal hnt = new HaveNTotal(null, 10L);
        setRowSelected(true);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        Text haveText = (Text) hbox.getChildren().get(0);
        
        assertThat(haveText.getFill()).as("Have text should be light gray").isEqualTo(Color.LIGHTGRAY);
    }

    @Test
    @DisplayName("Should use black color for total when row is not selected")
    void shouldUseBlackColorForTotalWhenNotSelected() {
        HaveNTotal hnt = new HaveNTotal(5L, 10L);
        setRowSelected(false);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        Text totalText = (Text) hbox.getChildren().get(1);
        
        assertThat(totalText.getFill()).as("Total text should be black").isEqualTo(Color.BLACK);
    }

    @Test
    @DisplayName("Should use white color for total when row is selected")
    void shouldUseWhiteColorForTotalWhenSelected() {
        HaveNTotal hnt = new HaveNTotal(5L, 10L);
        setRowSelected(true);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        Text totalText = (Text) hbox.getChildren().get(1);
        
        assertThat(totalText.getFill()).as("Total text should be white").isEqualTo(Color.WHITE);
    }

    @Test
    @DisplayName("Should use red color for have when have is 0 and total > 0")
    void shouldUseRedColorWhenHaveIsZero() {
        HaveNTotal hnt = new HaveNTotal(0L, 10L);
        setRowSelected(false);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        Text haveText = (Text) hbox.getChildren().get(0);
        
        assertThat(haveText.getFill()).as("Have text should be red (#cc0000)")
            .isEqualTo(Color.web("#cc0000"));
    }

    @Test
    @DisplayName("Should use light red color for have when have is 0 and row is selected")
    void shouldUseLightRedColorWhenHaveIsZeroAndSelected() {
        HaveNTotal hnt = new HaveNTotal(0L, 10L);
        setRowSelected(true);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        Text haveText = (Text) hbox.getChildren().get(0);
        
        assertThat(haveText.getFill()).as("Have text should be light red (#ffaaaa)")
            .isEqualTo(Color.web("#ffaaaa"));
    }

    @Test
    @DisplayName("Should use green color for have when have equals total")
    void shouldUseGreenColorWhenHaveEqualsTotal() {
        HaveNTotal hnt = new HaveNTotal(10L, 10L);
        setRowSelected(false);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        Text haveText = (Text) hbox.getChildren().get(0);
        
        assertThat(haveText.getFill()).as("Have text should be green (#00aa00)")
            .isEqualTo(Color.web("#00aa00"));
    }

    @Test
    @DisplayName("Should use light green color for have when have equals total and row is selected")
    void shouldUseLightGreenColorWhenHaveEqualsTotalAndSelected() {
        HaveNTotal hnt = new HaveNTotal(10L, 10L);
        setRowSelected(true);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        Text haveText = (Text) hbox.getChildren().get(0);
        
        assertThat(haveText.getFill()).as("Have text should be light green (#aaffaa)")
            .isEqualTo(Color.web("#aaffaa"));
    }

    @Test
    @DisplayName("Should use orange color for have when have is between 0 and total")
    void shouldUseOrangeColorWhenHaveIsBetweenZeroAndTotal() {
        HaveNTotal hnt = new HaveNTotal(5L, 10L);
        setRowSelected(false);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        Text haveText = (Text) hbox.getChildren().get(0);
        
        assertThat(haveText.getFill()).as("Have text should be orange (#cc8800)")
            .isEqualTo(Color.web("#cc8800"));
    }

    @Test
    @DisplayName("Should use light orange color for have when have is between 0 and total and row is selected")
    void shouldUseLightOrangeColorWhenHaveIsBetweenZeroAndTotalAndSelected() {
        HaveNTotal hnt = new HaveNTotal(5L, 10L);
        setRowSelected(true);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        Text haveText = (Text) hbox.getChildren().get(0);
        
        assertThat(haveText.getFill()).as("Have text should be light orange (#ffaa88)")
            .isEqualTo(Color.web("#ffaa88"));
    }

    @Test
    @DisplayName("Should center align the cell content")
    void shouldCenterAlignCellContent() {
        HaveNTotal hnt = new HaveNTotal(5L, 10L);
        
        cellFactory.updateItem(hnt, false);

        assertThat(cellFactory.getAlignment()).as("Cell should be center aligned")
            .isEqualTo(javafx.geometry.Pos.CENTER);
    }

    @Test
    @DisplayName("Should center align HBox content")
    void shouldCenterAlignHBoxContent() {
        HaveNTotal hnt = new HaveNTotal(5L, 10L);
        
        cellFactory.updateItem(hnt, false);

        HBox hbox = (HBox) cellFactory.getGraphic();
        assertThat(hbox.getAlignment()).as("HBox should be center aligned")
            .isEqualTo(javafx.geometry.Pos.CENTER);
    }
}
