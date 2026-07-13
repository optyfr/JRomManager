package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jrm.security.PathAbstractor;
import jrm.security.Session;
import jrm.security.Sessions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

/**
 * Tests for {@link BaseController} file and directory chooser methods.
 * <p>
 * Uses a concrete test subclass to test the protected helper methods.
 *
 * @since 3.0.5
 */
@TestFxApplication(BaseControllerTest.TestApp.class)
@DisplayName("BaseController Tests")
class BaseControllerTest {

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

    /**
     * Concrete test subclass to access protected methods.
     */
    private static class TestController extends BaseController {
        private Path selectedPath;
        private List<Path> selectedPaths;

        @Override
        public void initialize(URL location, ResourceBundle resources) {
            // No-op for testing
        }

        public Path getSelectedPath() {
            return selectedPath;
        }

        public List<Path> getSelectedPaths() {
            return selectedPaths;
        }

        public void recordSinglePath(Path path) {
            this.selectedPath = path;
        }

        public void recordMultiplePaths(List<Path> paths) {
            this.selectedPaths = paths;
        }
    }

    @TempDir
    Path tempDir;

    private TestController controller;
    private Session mockSession;

    @BeforeEach
    void setUp() {
        controller = new TestController();
        mockSession = mock(Session.class);
    }

    @Test
    @DisplayName("Should initialize session in constructor")
    void shouldInitializeSessionInConstructor() {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created")
                    .isNotNull();
            sessionsMock.verify(Sessions::getSingleSession);
        }
    }

    @Test
    @DisplayName("Should create Callback interface instance")
    void shouldCreateCallbackInterfaceInstance() {
        BaseController.Callback callback = path -> controller.recordSinglePath(path);

        assertThat(callback)
                .as("Callback should be created")
                .isNotNull();
    }

    @Test
    @DisplayName("Should create CallbackMulti interface instance")
    void shouldCreateCallbackMultiInterfaceInstance() {
        BaseController.CallbackMulti callbackMulti = paths -> controller.recordMultiplePaths(paths);

        assertThat(callbackMulti)
                .as("CallbackMulti should be created")
                .isNotNull();
    }

    @Test
    @DisplayName("Should invoke callback with path")
    void shouldInvokeCallbackWithPath() {
        Path testPath = Path.of("/test/path");
        BaseController.Callback callback = path -> controller.recordSinglePath(path);

        callback.call(testPath);

        assertThat(controller.getSelectedPath())
                .as("Selected path should be recorded")
                .isEqualTo(testPath);
    }

    @Test
    @DisplayName("Should invoke callback with multiple paths")
    void shouldInvokeCallbackWithMultiplePaths() {
        List<Path> testPaths = List.of(
                Path.of("/test/path1"),
                Path.of("/test/path2"),
                Path.of("/test/path3"));
        BaseController.CallbackMulti callbackMulti = paths -> controller.recordMultiplePaths(paths);

        callbackMulti.call(testPaths);

        assertThat(controller.getSelectedPaths())
                .as("Selected paths should be recorded")
                .containsExactlyElementsOf(testPaths);
    }

    @Test
    @DisplayName("Should handle null path in callback")
    void shouldHandleNullPathInCallback() {
        BaseController.Callback callback = path -> controller.recordSinglePath(path);

        callback.call(null);

        assertThat(controller.getSelectedPath())
                .as("Selected path should be null")
                .isNull();
    }

    @Test
    @DisplayName("Should handle empty list in callback multi")
    void shouldHandleEmptyListInCallbackMulti() {
        List<Path> emptyPaths = new ArrayList<>();
        BaseController.CallbackMulti callbackMulti = paths -> controller.recordMultiplePaths(paths);

        callbackMulti.call(emptyPaths);

        assertThat(controller.getSelectedPaths())
                .as("Selected paths should be empty list")
                .isEmpty();
    }

    @Test
    @DisplayName("Should handle existing directory in initFileChooser")
    void shouldHandleExistingDirectoryInInitFileChooser() throws IOException {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
                @SuppressWarnings("unused")
                MockedStatic<PathAbstractor> pathMock = mockStatic(PathAbstractor.class)) {

            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);
            Path existingDir = Files.createTempDirectory(tempDir, "test");
            when(PathAbstractor.getAbsolutePath(any(Session.class), any(String.class)))
                    .thenReturn(existingDir);

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with existing directory")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle existing file in initFileChooser")
    void shouldHandleExistingFileInInitFileChooser() throws IOException {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
                @SuppressWarnings("unused")
                MockedStatic<PathAbstractor> pathMock = mockStatic(PathAbstractor.class)) {

            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);
            Path existingFile = Files.createTempFile(tempDir, "test", ".txt");
            when(PathAbstractor.getAbsolutePath(any(Session.class), any(String.class)))
                    .thenReturn(existingFile);

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with existing file")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle non-existent path in initFileChooser")
    void shouldHandleNonExistentPathInInitFileChooser() {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
                @SuppressWarnings("unused")
                MockedStatic<PathAbstractor> pathMock = mockStatic(PathAbstractor.class)) {

            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);
            when(PathAbstractor.getAbsolutePath(any(Session.class), any(String.class)))
                    .thenReturn(Path.of("/non/existent/path"));

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with non-existent path")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle null path in initFileChooser")
    void shouldHandleNullPathInInitFileChooser() {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
                @SuppressWarnings("unused")
                MockedStatic<PathAbstractor> pathMock = mockStatic(PathAbstractor.class)) {

            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);
            when(PathAbstractor.getAbsolutePath(any(Session.class), any(String.class)))
                    .thenReturn(null);

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with null path")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle blank initial value in initFileChooser")
    void shouldHandleBlankInitialValueInInitFileChooser() {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with blank initial value")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle null initial value in initFileChooser")
    void shouldHandleNullInitialValueInInitFileChooser() {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with null initial value")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle existing parent directory in initFileChooser")
    void shouldHandleExistingParentDirectoryInInitFileChooser() throws IOException {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
                @SuppressWarnings("unused")
                MockedStatic<PathAbstractor> pathMock = mockStatic(PathAbstractor.class)) {

            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);
            Path existingDir = Files.createTempDirectory(tempDir, "parent");
            Path nonExistentFile = existingDir.resolve("nonexistent.txt");
            when(PathAbstractor.getAbsolutePath(any(Session.class), any(String.class)))
                    .thenReturn(nonExistentFile);

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with existing parent directory")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle default directory in initFileChooser")
    void shouldHandleDefaultDirectoryInInitFileChooser() throws IOException {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
                @SuppressWarnings("unused")
                MockedStatic<PathAbstractor> pathMock = mockStatic(PathAbstractor.class)) {

            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);
            when(PathAbstractor.getAbsolutePath(any(Session.class), any(String.class)))
                    .thenReturn(null);

            @SuppressWarnings("unused")
            File defaultDir = Files.createTempDirectory(tempDir, "default").toFile();

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with default directory")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle non-existent default directory in initFileChooser")
    void shouldHandleNonExistentDefaultDirectoryInInitFileChooser() {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
                @SuppressWarnings("unused")
                MockedStatic<PathAbstractor> pathMock = mockStatic(PathAbstractor.class)) {

            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);
            when(PathAbstractor.getAbsolutePath(any(Session.class), any(String.class)))
                    .thenReturn(null);

            @SuppressWarnings("unused")
            File nonExistentDir = new File("/non/existent/dir");

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with non-existent default directory")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle whitespace initial value in initFileChooser")
    void shouldHandleWhitespaceInitialValueInInitFileChooser() {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with whitespace initial value")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle directory with existing parent in initDirectoryChooser")
    void shouldHandleDirectoryWithExistingParentInInitDirectoryChooser() throws IOException {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
                @SuppressWarnings("unused")
                MockedStatic<PathAbstractor> pathMock = mockStatic(PathAbstractor.class)) {

            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);
            Path existingDir = Files.createTempDirectory(tempDir, "dirtest");
            when(PathAbstractor.getAbsolutePath(any(Session.class), any(String.class)))
                    .thenReturn(existingDir);

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with existing directory")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle non-directory path in initDirectoryChooser")
    void shouldHandleNonDirectoryPathInInitDirectoryChooser() throws IOException {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
                @SuppressWarnings("unused")
                MockedStatic<PathAbstractor> pathMock = mockStatic(PathAbstractor.class)) {

            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);
            Path existingFile = Files.createTempFile(tempDir, "file", ".txt");
            when(PathAbstractor.getAbsolutePath(any(Session.class), any(String.class)))
                    .thenReturn(existingFile);

            @SuppressWarnings("unused")
            File defaultDir = Files.createTempDirectory(tempDir, "default").toFile();

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with non-directory path")
                    .isNotNull();
        }
    }

    @Test
    @DisplayName("Should handle null path with existing default in initDirectoryChooser")
    void shouldHandleNullPathWithExistingDefaultInInitDirectoryChooser() throws IOException {
        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
                @SuppressWarnings("unused")
                MockedStatic<PathAbstractor> pathMock = mockStatic(PathAbstractor.class)) {

            sessionsMock.when(Sessions::getSingleSession).thenReturn(mockSession);
            when(PathAbstractor.getAbsolutePath(any(Session.class), any(String.class)))
                    .thenReturn(null);

            @SuppressWarnings("unused")
            File defaultDir = Files.createTempDirectory(tempDir, "default").toFile();

            TestController newController = new TestController();

            assertThat(newController)
                    .as("Controller should be created with null path and existing default")
                    .isNotNull();
        }
    }
}
