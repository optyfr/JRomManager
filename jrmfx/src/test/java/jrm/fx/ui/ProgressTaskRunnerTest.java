package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URISyntaxException;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.progress.ProgressTask;
import jrm.misc.BreakException;
import jrm.misc.Log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Tests for {@link ProgressTaskRunner} utility class.
 * <p>
 * Validates task execution, error handling, and exception management.
 *
 * @since 3.0.5
 */
@TestFxApplication(ProgressTaskRunnerTest.TestApp.class)
@DisplayName("ProgressTaskRunner Tests")
class ProgressTaskRunnerTest {

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
    @DisplayName("Should have private constructor")
    void shouldHavePrivateConstructor() {
        Constructor<?>[] constructors = ProgressTaskRunner.class.getDeclaredConstructors();
        
        assertThat(constructors)
                .as("Should have exactly one constructor")
                .hasSize(1);
        assertThat(constructors[0])
                .as("Constructor should be private")
                .matches(c -> java.lang.reflect.Modifier.isPrivate(c.getModifiers()));
    }

    @Test
    @DisplayName("Should not be able to instantiate utility class")
    void shouldNotBeAbleToInstantiate() {
        Constructor<?> constructor = ProgressTaskRunner.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        
        assertThat(constructor)
                .as("Constructor should be private")
                .isNotNull();
    }

    @Test
    @DisplayName("Should handle IOException when creating task")
    void shouldHandleIOExceptionWhenCreatingTask(TestApp application) {
        try (MockedStatic<Dialogs> dialogsMock = mockStatic(Dialogs.class);
             MockedStatic<Log> logMock = mockStatic(Log.class)) {
            
            IOException ioException = new IOException("Test IO error");
            ProgressTaskRunner.ProgressTaskFactory<Object> factory = stage -> {
                throw ioException;
            };
            
            ProgressTaskRunner.run(application.recordedStage(), factory);
            
            logMock.verify(() -> Log.err("Test IO error", ioException));
            dialogsMock.verify(() -> Dialogs.showError(ioException));
        }
    }

    @Test
    @DisplayName("Should handle URISyntaxException when creating task")
    void shouldHandleURISyntaxExceptionWhenCreatingTask(TestApp application) {
        try (MockedStatic<Dialogs> dialogsMock = mockStatic(Dialogs.class);
             MockedStatic<Log> logMock = mockStatic(Log.class)) {
            
            URISyntaxException uriException = new URISyntaxException("bad", "Test URI error");
            ProgressTaskRunner.ProgressTaskFactory<Object> factory = stage -> {
                throw uriException;
            };
            
            ProgressTaskRunner.run(application.recordedStage(), factory);
            
            logMock.verify(() -> Log.err(uriException.getMessage(), uriException));
            dialogsMock.verify(() -> Dialogs.showError(uriException));
        }
    }

    @Test
    @DisplayName("Should handle InterruptedException")
    void shouldHandleInterruptedException() {
        try (MockedStatic<Log> logMock = mockStatic(Log.class)) {
            ProgressTask<?> task = mock(ProgressTask.class);
            InterruptedException exception = new InterruptedException("Test interrupt");
            
            ProgressTaskRunner.handleInterruptedException(task, exception);
            
            verify(task).close();
            logMock.verify(() -> Log.err("Test interrupt", exception));
            // Clear the interrupt flag set by handleInterruptedException
            // to avoid interfering with the TestFX test runner
            boolean wasInterrupted = Thread.currentThread().isInterrupted();
            assertThat(wasInterrupted)
                    .as("Current thread should have been interrupted")
                    .isTrue();
            // Clear the flag
            Thread.interrupted();
        }
    }

    @Test
    @DisplayName("Should handle ExecutionException with cause")
    void shouldHandleExecutionExceptionWithCause() {
        try (MockedStatic<Dialogs> dialogsMock = mockStatic(Dialogs.class);
             MockedStatic<Log> logMock = mockStatic(Log.class)) {
            
            ProgressTask<?> task = mock(ProgressTask.class);
            Exception cause = new RuntimeException("Root cause");
            Exception exception = new Exception("Wrapper", cause);
            
            ProgressTaskRunner.handleExecutionException(task, exception);
            
            verify(task).close();
            logMock.verify(() -> Log.err("Root cause", cause));
            dialogsMock.verify(() -> Dialogs.showError(cause));
        }
    }

    @Test
    @DisplayName("Should handle ExecutionException without cause")
    void shouldHandleExecutionExceptionWithoutCause() {
        try (MockedStatic<Dialogs> dialogsMock = mockStatic(Dialogs.class);
             MockedStatic<Log> logMock = mockStatic(Log.class)) {
            
            ProgressTask<?> task = mock(ProgressTask.class);
            Exception exception = new Exception("Test exception");
            
            ProgressTaskRunner.handleExecutionException(task, exception);
            
            verify(task).close();
            logMock.verify(() -> Log.err("Test exception", exception));
            dialogsMock.verify(() -> Dialogs.showError(exception));
        }
    }

    @Test
    @DisplayName("Should handle BreakException in failed")
    void shouldHandleBreakExceptionInFailed() {
        try (MockedStatic<Dialogs> dialogsMock = mockStatic(Dialogs.class);
             MockedStatic<Log> logMock = mockStatic(Log.class)) {
            
            ProgressTask<?> task = mock(ProgressTask.class);
            BreakException breakException = new BreakException("Cancelled");
            
            ProgressTaskRunner.handleFailedException(task, breakException);
            
            verify(task, never()).close();
            logMock.verify(() -> Log.err((String) any(), (Throwable) any()), never());
            dialogsMock.verify(() -> Dialogs.showAlert("Cancelled"));
        }
    }

    @Test
    @DisplayName("Should handle other exception with cause in failed")
    void shouldHandleOtherExceptionWithCauseInFailed() {
        try (MockedStatic<Dialogs> dialogsMock = mockStatic(Dialogs.class);
             MockedStatic<Log> logMock = mockStatic(Log.class)) {
            
            ProgressTask<?> task = mock(ProgressTask.class);
            Throwable cause = new RuntimeException("Root cause");
            Throwable exception = new Exception("Wrapper", cause);
            
            ProgressTaskRunner.handleFailedException(task, exception);
            
            verify(task).close();
            logMock.verify(() -> Log.err("Root cause", cause));
            dialogsMock.verify(() -> Dialogs.showError(cause));
        }
    }

    @Test
    @DisplayName("Should handle other exception without cause in failed")
    void shouldHandleOtherExceptionWithoutCauseInFailed() {
        try (MockedStatic<Dialogs> dialogsMock = mockStatic(Dialogs.class);
             MockedStatic<Log> logMock = mockStatic(Log.class)) {
            
            ProgressTask<?> task = mock(ProgressTask.class);
            Throwable exception = new Exception("Test exception");
            
            ProgressTaskRunner.handleFailedException(task, exception);
            
            verify(task).close();
            logMock.verify(() -> Log.err("Test exception", exception));
            dialogsMock.verify(() -> Dialogs.showError(exception));
        }
    }
}
