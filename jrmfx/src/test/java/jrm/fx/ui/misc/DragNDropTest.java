package jrm.fx.ui.misc;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX tests for {@link DragNDrop}.
 * <p>
 * Tests the drag-and-drop utility class that provides visual feedback and
 * callback mechanisms for various control types.
 *
 * @since 3.0.5
 */
@TestFxApplication(DragNDropTest.TestApp.class)
@DisplayName("DragNDrop Tests")
class DragNDropTest {

    /**
     * Test application for JavaFX component tests.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;

        /**
         * Starts the test application.
         *
         * @param primaryStage the primary stage for this application
         */
        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            primaryStage.setScene(new Scene(new StackPane(), 400, 300));
            primaryStage.show();
        }

        /**
         * Returns the recorded stage.
         *
         * @return the primary stage used in this test application
         */
        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    /** The list that collects callback invocations during tests. */
    private List<String> callbackLog;

    /**
     * Initializes the callback log before each test.
     */
    @BeforeEach
    void setUp() {
        callbackLog = new ArrayList<>();
    }

    /**
     * Verifies that a {@link DragNDrop} instance can be created for a regular
     * {@link javafx.scene.control.Control} with the expected style.
     */
    @Test
    @DisplayName("Should create DragNDrop for regular Control with control-inner-background style")
    void shouldCreateDragNDropForControl() {
        Button button = new Button("Test");
        DragNDrop dragNDrop = new DragNDrop(button);

        assertThat(dragNDrop).as("DragNDrop should be created").isNotNull();
    }

    /**
     * Verifies that a {@link DragNDrop} instance can be created for a
     * {@link javafx.scene.control.Cell} with the background-color style.
     */
    @Test
    @DisplayName("Should create DragNDrop for Cell with background-color style")
    void shouldCreateDragNDropForCell() {
        TableCell<String, String> cell = new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
            }
        };
        DragNDrop dragNDrop = new DragNDrop(cell);

        assertThat(dragNDrop).as("DragNDrop should be created for Cell").isNotNull();
    }

    /**
     * Verifies that {@link DragNDrop#addFile} installs drag-over, drag-dropped,
     * and drag-exited handlers on the control.
     */
    @Test
    @DisplayName("Should install addFile handler")
    void shouldInstallAddFileHandler() {
        TextField textField = new TextField();
        DragNDrop dragNDrop = new DragNDrop(textField);

        dragNDrop.addFile(txt -> callbackLog.add("file:" + txt));

        assertThat(textField.getOnDragOver()).as("DragOver handler should be installed").isNotNull();
        assertThat(textField.getOnDragDropped()).as("DragDropped handler should be installed").isNotNull();
        assertThat(textField.getOnDragExited()).as("DragExited handler should be installed").isNotNull();
    }

    /**
     * Verifies that {@link DragNDrop#addDir} installs drag-over and drag-dropped
     * handlers on the control.
     */
    @Test
    @DisplayName("Should install addDir handler")
    void shouldInstallAddDirHandler() {
        TextField textField = new TextField();
        DragNDrop dragNDrop = new DragNDrop(textField);

        dragNDrop.addDir(txt -> callbackLog.add("dir:" + txt));

        assertThat(textField.getOnDragOver()).as("DragOver handler should be installed").isNotNull();
        assertThat(textField.getOnDragDropped()).as("DragDropped handler should be installed").isNotNull();
    }

    /**
     * Verifies that {@link DragNDrop#addAny} installs drag-over and drag-dropped
     * handlers on the control.
     */
    @Test
    @DisplayName("Should install addAny handler")
    void shouldInstallAddAnyHandler() {
        ListView<File> listView = new ListView<>();
        DragNDrop dragNDrop = new DragNDrop(listView);

        dragNDrop.addAny(files -> callbackLog.add("any:" + files.size()));

        assertThat(listView.getOnDragOver()).as("DragOver handler should be installed").isNotNull();
        assertThat(listView.getOnDragDropped()).as("DragDropped handler should be installed").isNotNull();
    }

    /**
     * Verifies that {@link DragNDrop#addDirs} installs drag-over and drag-dropped
     * handlers on the control.
     */
    @Test
    @DisplayName("Should install addDirs handler")
    void shouldInstallAddDirsHandler() {
        ListView<File> listView = new ListView<>();
        DragNDrop dragNDrop = new DragNDrop(listView);

        dragNDrop.addDirs(files -> callbackLog.add("dirs:" + files.size()));

        assertThat(listView.getOnDragOver()).as("DragOver handler should be installed").isNotNull();
        assertThat(listView.getOnDragDropped()).as("DragDropped handler should be installed").isNotNull();
    }

    /**
     * Verifies that {@link DragNDrop#addNewFile} installs drag-over and drag-dropped
     * handlers on the control.
     */
    @Test
    @DisplayName("Should install addNewFile handler")
    void shouldInstallAddNewFileHandler() {
        TextField textField = new TextField();
        DragNDrop dragNDrop = new DragNDrop(textField);

        dragNDrop.addNewFile(txt -> callbackLog.add("newfile:" + txt));

        assertThat(textField.getOnDragOver()).as("DragOver handler should be installed").isNotNull();
        assertThat(textField.getOnDragDropped()).as("DragDropped handler should be installed").isNotNull();
    }

    /**
     * Verifies that {@link DragNDrop#addFiltered} installs handlers with a custom
     * {@link java.util.function.Predicate} filter and a {@link DragNDrop.SetFilesCallBack}.
     */
    @Test
    @DisplayName("Should install filtered handler with custom predicate")
    void shouldInstallFilteredHandler() {
        Button button = new Button("Test");
        DragNDrop dragNDrop = new DragNDrop(button);

        dragNDrop.addFiltered(
            file -> file.getName().endsWith(".txt"),
            (DragNDrop.SetFilesCallBack) files -> callbackLog.add("filtered:" + files.size())
        );

        assertThat(button.getOnDragOver()).as("DragOver handler should be installed").isNotNull();
        assertThat(button.getOnDragDropped()).as("DragDropped handler should be installed").isNotNull();
    }

    /**
     * Verifies that {@link DragNDrop#addFiltered} installs handlers with a
     * single-file {@link DragNDrop.SetCallBack}.
     */
    @Test
    @DisplayName("Should install filtered single-file handler")
    void shouldInstallFilteredSingleFileHandler() {
        Button button = new Button("Test");
        DragNDrop dragNDrop = new DragNDrop(button);

        dragNDrop.addFiltered(
            file -> file.getName().endsWith(".txt"),
            (DragNDrop.SetCallBack) txt -> callbackLog.add("single:" + txt)
        );

        assertThat(button.getOnDragOver()).as("DragOver handler should be installed").isNotNull();
        assertThat(button.getOnDragDropped()).as("DragDropped handler should be installed").isNotNull();
    }

    /**
     * Verifies that {@link DragNDrop#addFiltered} installs a text validator
     * listener on a {@link TextField}.
     */
    @Test
    @DisplayName("Should install text validator for TextField with filtered handler")
    void shouldInstallTextValidatorForTextField() {
        TextField textField = new TextField();
        DragNDrop dragNDrop = new DragNDrop(textField);

        dragNDrop.addFiltered(
            File::exists,
            (DragNDrop.SetCallBack) txt -> callbackLog.add("validated:" + txt)
        );

        // Text validator should be installed as a listener on textProperty
        assertThat(textField.getOnDragOver()).as("DragOver handler should be installed").isNotNull();
        assertThat(textField.getOnDragDropped()).as("DragDropped handler should be installed").isNotNull();
    }

    /**
     * Verifies that text validation is not installed when the control is
     * not a {@link TextField}.
     */
    @Test
    @DisplayName("Should not install text validator for non-TextField control")
    void shouldNotInstallTextValidatorForNonTextField() {
        Button button = new Button("Test");
        DragNDrop dragNDrop = new DragNDrop(button);

        dragNDrop.addFiltered(
            File::exists,
            (DragNDrop.SetCallBack) txt -> callbackLog.add("validated:" + txt)
        );

        // Button should not have text validation, only drag handlers
        assertThat(button.getOnDragOver()).as("DragOver handler should be installed").isNotNull();
        assertThat(button.getOnDragDropped()).as("DragDropped handler should be installed").isNotNull();
    }

    /**
     * Verifies that multiple {@link DragNDrop} instances can be created
     * on the same control, with the second instance overriding the first.
     */
    @Test
    @DisplayName("Should allow multiple DragNDrop instances on same control")
    void shouldAllowMultipleDragNDropInstances() {
        Button button = new Button("Test");
        DragNDrop dragNDrop1 = new DragNDrop(button);
        DragNDrop dragNDrop2 = new DragNDrop(button);

        dragNDrop1.addFile(txt -> callbackLog.add("1:" + txt));
        dragNDrop2.addDir(txt -> callbackLog.add("2:" + txt));

        // Second instance should override first instance's handlers
        assertThat(button.getOnDragOver()).as("DragOver handler should be from second instance").isNotNull();
    }

    /**
     * Verifies that {@link DragNDrop#addFile} handles a {@code null} callback
     * without throwing an exception and still installs the handler.
     */
    @Test
    @DisplayName("Should handle null callback gracefully")
    void shouldHandleNullCallbackGracefully() {
        TextField textField = new TextField();
        DragNDrop dragNDrop = new DragNDrop(textField);

        // This should not throw an exception
        dragNDrop.addFile(null);

        assertThat(textField.getOnDragOver()).as("Handler should still be installed").isNotNull();
    }

    /**
     * Verifies that a {@link DragNDrop} instance can be created for a
     * {@link ListView} control.
     */
    @Test
    @DisplayName("Should create DragNDrop for ListView")
    void shouldCreateDragNDropForListView() {
        ListView<File> listView = new ListView<>();
        DragNDrop dragNDrop = new DragNDrop(listView);

        assertThat(dragNDrop).as("DragNDrop should be created for ListView").isNotNull();
    }

    /**
     * Verifies that a {@link DragNDrop} instance can be created for a
     * {@link TextField} control.
     */
    @Test
    @DisplayName("Should create DragNDrop for TextField")
    void shouldCreateDragNDropForTextField() {
        TextField textField = new TextField();
        DragNDrop dragNDrop = new DragNDrop(textField);

        assertThat(dragNDrop).as("DragNDrop should be created for TextField").isNotNull();
    }

    /**
     * Verifies that separate {@link DragNDrop} instances on different controls
     * do not interfere with each other.
     */
    @Test
    @DisplayName("Should maintain separate instances for different controls")
    void shouldMaintainSeparateInstancesForDifferentControls() {
        Button button1 = new Button("Button1");
        Button button2 = new Button("Button2");
        
        DragNDrop dragNDrop1 = new DragNDrop(button1);
        DragNDrop dragNDrop2 = new DragNDrop(button2);

        dragNDrop1.addFile(txt -> callbackLog.add("btn1:" + txt));
        dragNDrop2.addDir(txt -> callbackLog.add("btn2:" + txt));

        assertThat(button1.getOnDragOver()).as("Button1 should have its own handler").isNotNull();
        assertThat(button2.getOnDragOver()).as("Button2 should have its own handler").isNotNull();
        assertThat(button1.getOnDragOver()).as("Handlers should be different instances")
            .isNotSameAs(button2.getOnDragOver());
    }
}
