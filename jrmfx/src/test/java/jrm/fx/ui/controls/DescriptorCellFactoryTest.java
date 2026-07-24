package jrm.fx.ui.controls;

import static org.assertj.core.api.Assertions.assertThat;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jrm.profile.scan.options.Descriptor;
import jrm.profile.scan.options.FormatOptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX-based tests for {@link DescriptorCellFactory}.
 * <p>
 * Tests the list cell factory that displays Descriptor enum descriptions.
 *
 * @since 3.0.5
 */
@TestFxApplication(DescriptorCellFactoryTest.TestApp.class)
@DisplayName("DescriptorCellFactory TestFX Tests")
class DescriptorCellFactoryTest {

    /**
     * Test application that sets up a ListView with a DescriptorCellFactory.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            
            ListView<Descriptor> listView = new ListView<>();
            for (FormatOptions option : FormatOptions.values()) {
                listView.getItems().add(option);
            }
            listView.setCellFactory(lv -> new DescriptorCellFactory());
            
            primaryStage.setScene(new Scene(new StackPane(listView), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    /** The cell factory under test. */
    private DescriptorCellFactory cellFactory;

    /**
     * Initializes a fresh cell factory before each test.
     */
    @BeforeEach
    void setUp() {
        cellFactory = new DescriptorCellFactory();
    }

    /**
     * Verifies that a {@link DescriptorCellFactory} can be constructed successfully.
     */
    @Test
    @DisplayName("Should create DescriptorCellFactory")
    void shouldCreateDescriptorCellFactory() {
        assertThat(cellFactory).as("Cell factory should be created").isNotNull();
    }

    /**
     * Verifies that the cell graphic is cleared to {@code null} when the item is {@code null}.
     */
    @Test
    @DisplayName("Should set graphic to null for null item")
    void shouldSetGraphicToNullForNullItem() {
        cellFactory.updateItem(null, false);

        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    /**
     * Verifies that the cell graphic is cleared to {@code null} when the cell is marked as empty.
     */
    @Test
    @DisplayName("Should set graphic to null for empty cell")
    void shouldSetGraphicToNullForEmptyCell() {
        cellFactory.updateItem(FormatOptions.ZIP, true);

        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    /**
     * Verifies that the cell text is set to a non-empty value when a {@link Descriptor} item is present.
     */
    @Test
    @DisplayName("Should set text for non-empty Descriptor item")
    void shouldSetTextForNonEmptyDescriptorItem() {
        cellFactory.updateItem(FormatOptions.ZIP, false);

        assertThat(cellFactory.getText()).as("Text should not be null").isNotNull();
        assertThat(cellFactory.getText()).as("Text should not be empty").isNotEmpty();
    }

    /**
     * Verifies that the cell displays the correct description for {@link FormatOptions#ZIP}.
     */
    @Test
    @DisplayName("Should display ZIP description")
    void shouldDisplayZipDescription() {
        cellFactory.updateItem(FormatOptions.ZIP, false);

        assertThat(cellFactory.getText()).as("Text should match ZIP description")
            .isEqualTo(FormatOptions.ZIP.getDesc());
    }

    /**
     * Verifies that the cell displays the correct description for {@link FormatOptions#SEVENZIP}.
     */
    @Test
    @DisplayName("Should display SEVENZIP description")
    void shouldDisplaySevenzipDescription() {
        cellFactory.updateItem(FormatOptions.SEVENZIP, false);

        assertThat(cellFactory.getText()).as("Text should match SEVENZIP description")
            .isEqualTo(FormatOptions.SEVENZIP.getDesc());
    }

    /**
     * Verifies that the cell displays the correct description for {@link FormatOptions#DIR}.
     */
    @Test
    @DisplayName("Should display DIR description")
    void shouldDisplayDirDescription() {
        cellFactory.updateItem(FormatOptions.DIR, false);

        assertThat(cellFactory.getText()).as("Text should match DIR description")
            .isEqualTo(FormatOptions.DIR.getDesc());
    }

    /**
     * Verifies that the cell graphic is reset when the item changes from a valid descriptor to {@code null}.
     */
    @Test
    @DisplayName("Should clear text when item becomes null")
    void shouldClearTextWhenItemBecomesNull() {
        cellFactory.updateItem(FormatOptions.ZIP, false);
        assertThat(cellFactory.getText()).as("Text should be set").isNotNull();

        cellFactory.updateItem(null, false);
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    /**
     * Verifies that the cell graphic is cleared when the cell transitions from non-empty to empty state.
     */
    @Test
    @DisplayName("Should clear graphic when cell becomes empty")
    void shouldClearGraphicWhenCellBecomesEmpty() {
        cellFactory.updateItem(FormatOptions.ZIP, false);
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();

        cellFactory.updateItem(FormatOptions.ZIP, true);
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    /**
     * Verifies that no graphic is set for any {@link FormatOptions} value when the cell is not empty.
     */
    @Test
    @DisplayName("Should not set graphic for any item")
    void shouldNotSetGraphicForAnyItem() {
        for (FormatOptions option : FormatOptions.values()) {
            cellFactory.updateItem(option, false);
            assertThat(cellFactory.getGraphic()).as("Graphic should be null for " + option).isNull();
        }
    }

    /**
     * Verifies that the cell displays the correct description for every {@link FormatOptions} enum constant.
     */
    @Test
    @DisplayName("Should display description for all FormatOptions")
    void shouldDisplayDescriptionForAllFormatOptions() {
        for (FormatOptions option : FormatOptions.values()) {
            cellFactory.updateItem(option, false);
            assertThat(cellFactory.getText()).as("Text should match description for " + option)
                .isEqualTo(option.getDesc());
        }
    }
}
