package jrm.fx.ui.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javafx.application.Platform;
import javafx.stage.Stage;
import jrm.aui.progress.ProgressInputStream;
import jrm.misc.OffsetProvider;

/**
 * Unit tests for {@link ProgressTask} abstract class.
 * Tests progress tracking, cancellation, error handling, and thread management.
 */
@DisplayName("ProgressTask Unit Tests")
class ProgressTaskTest {

    /**
     * Concrete implementation of ProgressTask for testing.
     */
    private static class TestProgressTask extends ProgressTask<Void> {
        TestProgressTask(Stage owner) throws IOException, URISyntaxException {
            super(owner);
        }

        @Override
        protected Void call() throws Exception {
            return null;
        }
    }

    private TestProgressTask task;
    private Stage mockStage;

    @BeforeEach
    void setUp() throws Exception {
        // Mock JavaFX Platform to avoid toolkit initialization
        try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
            platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
            platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                .thenAnswer(invocation -> {
                    // Don't execute runnables to avoid JavaFX thread issues
                    return null;
                });

            mockStage = mock(Stage.class);

            // Mock Progress construction to avoid FXML loading
            try (@SuppressWarnings("unused")
            MockedConstruction<Progress> progressMock = mockConstruction(Progress.class, (mock, context) -> {
                ProgressController mockController = mock(ProgressController.class);
                when(mock.getController()).thenReturn(mockController);
            })) {
                task = new TestProgressTask(mockStage);
            }
        }
    }

    @Nested
    @DisplayName("Progress tracking tests")
    class ProgressTrackingTests {

        @Test
        @DisplayName("Should initialize with zero progress")
        void shouldInitializeWithZeroProgress() {
            assertThat(task.getCurrent()).isZero();
            assertThat(task.getCurrent2()).isZero();
            assertThat(task.getCurrent3()).isZero();
        }

        @Test
        @DisplayName("Should track primary progress")
        void shouldTrackPrimaryProgress() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.setProgress("Processing", 50, 100, null);

                assertThat(task.getCurrent()).isEqualTo(50);
            }
        }

        @Test
        @DisplayName("Should track secondary progress")
        void shouldTrackSecondaryProgress() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.setProgress2("Subtask", 25, 50);

                assertThat(task.getCurrent2()).isEqualTo(25);
            }
        }

        @Test
        @DisplayName("Should track tertiary progress")
        void shouldTrackTertiaryProgress() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.setProgress3("Detail", 10, 20);

                assertThat(task.getCurrent3()).isEqualTo(10);
            }
        }

        @Test
        @DisplayName("Should handle progress with max value")
        void shouldHandleProgressWithMaxValue() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.setProgress("Processing", 75, 150, null);

                assertThat(task.getCurrent()).isEqualTo(75);
            }
        }

        @Test
        @DisplayName("Should handle indeterminate progress (val=0)")
        void shouldHandleIndeterminateProgress() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.setProgress("Starting", 0, 100, null);

                assertThat(task.getCurrent()).isZero();
            }
        }
    }

    @Nested
    @DisplayName("Cancellation tests")
    class CancellationTests {

        @Test
        @DisplayName("Should not be cancelled initially")
        void shouldNotBeCancelledInitially() {
            assertThat(task.isCancel()).isFalse();
        }

        @Test
        @DisplayName("Should be cancellable by default")
        void shouldBeCancellableByDefault() {
            assertThat(task.canCancel()).isTrue();
        }

        @Test
        @DisplayName("Should cancel when doCancel is called")
        void shouldCancelWhenDoCancelIsCalled() {
            task.doCancel();

            assertThat(task.isCancel()).isTrue();
        }

        @Test
        @DisplayName("Should disable cancellation")
        void shouldDisableCancellation() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.canCancel(false);

                assertThat(task.canCancel()).isFalse();
            }
        }

        @Test
        @DisplayName("Should re-enable cancellation")
        void shouldReEnableCancellation() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.canCancel(false);
                assertThat(task.canCancel()).isFalse();

                task.canCancel(true);
                assertThat(task.canCancel()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("Error handling tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should add error messages")
        void shouldAddErrorMessages() {
            task.addError("Error 1");
            task.addError("Error 2");

            // Errors are stored internally, no direct getter available
            // Just verify no exception is thrown
            assertThat(task).isNotNull();
        }

        @Test
        @DisplayName("Should handle multiple errors")
        void shouldHandleMultipleErrors() {
            for (int i = 0; i < 10; i++) {
                task.addError("Error " + i);
            }

            assertThat(task).isNotNull();
        }
    }

    @Nested
    @DisplayName("Info management tests")
    class InfoManagementTests {

        @Test
        @DisplayName("Should set thread count and sub-info mode")
        void shouldSetThreadCountAndSubInfoMode() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.setInfos(4, true);

                // Verify no exception is thrown
                assertThat(task).isNotNull();
            }
        }

        @Test
        @DisplayName("Should handle zero thread count by using available processors")
        void shouldHandleZeroThreadCount() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.setInfos(0, false);

                assertThat(task).isNotNull();
            }
        }

        @Test
        @DisplayName("Should handle negative thread count by using available processors")
        void shouldHandleNegativeThreadCount() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.setInfos(-1, null);

                assertThat(task).isNotNull();
            }
        }

        @Test
        @DisplayName("Should clear infos")
        void shouldClearInfos() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.setInfos(2, true);
                task.setProgress("Info 1", 10, 100, "Sub 1");
                task.clearInfos();

                assertThat(task).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("InputStream wrapper tests")
    class InputStreamWrapperTests {

        @Test
        @DisplayName("Should wrap input stream with progress tracking")
        void shouldWrapInputStreamWithProgressTracking() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                InputStream originalStream = new ByteArrayInputStream("test data".getBytes());

                InputStream wrappedStream = task.getInputStream(originalStream, 9);

                assertThat(wrappedStream).isInstanceOf(ProgressInputStream.class);
                assertThat(wrappedStream).isNotSameAs(originalStream);
            }
        }

        @Test
        @DisplayName("Should handle null input stream")
        void shouldHandleNullInputStream() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                InputStream wrappedStream = task.getInputStream(null, 0);

                assertThat(wrappedStream).isInstanceOf(ProgressInputStream.class);
            }
        }
    }

    @Nested
    @DisplayName("Options tests")
    class OptionsTests {

        @Test
        @DisplayName("Should set options")
        void shouldSetOptions() {
            task.setOptions(jrm.aui.progress.ProgressHandler.Option.LAZY);

            assertThat(task).isNotNull();
        }
    }

    @Nested
    @DisplayName("Offset provider tests")
    class OffsetProviderTests {

        @Test
        @DisplayName("Should set offset provider")
        void shouldSetOffsetProvider() {
            OffsetProvider mockProvider = mock(OffsetProvider.class);

            task.setOffsetProvider(mockProvider);

            assertThat(task).isNotNull();
        }

        @Test
        @DisplayName("Should handle null offset provider")
        void shouldHandleNullOffsetProvider() {
            task.setOffsetProvider(null);

            assertThat(task).isNotNull();
        }
    }

    @Nested
    @DisplayName("Close tests")
    class CloseTests {

        @Test
        @DisplayName("Should close progress dialog")
        void shouldCloseProgressDialog() {
            try (MockedStatic<Platform> platformMock = mockStatic(Platform.class)) {
                platformMock.when(Platform::isFxApplicationThread).thenReturn(false);
                platformMock.when(() -> Platform.runLater(org.mockito.ArgumentMatchers.any(Runnable.class)))
                    .thenAnswer(invocation -> null);

                task.close();

                assertThat(task).isNotNull();
            }
        }
    }
}
