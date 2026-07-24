package jrm.fx.ui.controls;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Dialogs}.
 * <p>
 * Tests the utility methods that don't require UI interaction.
 * The dialog display methods (showError, showAlert, showConfirmation) are not tested
 * as they call showAndWait() which blocks in test context and depend on MainFrame/JRMScene.
 *
 * @since 3.0.5
 */
@DisplayName("Dialogs Tests")
class DialogsTest {

    /**
     * Tests the private getStackTrace() method via reflection.
     *
     * @param e the exception to get stack trace from
     * @return the stack trace as a string
     */
    private String invokeGetStackTrace(Throwable e) throws Exception {
        Method method = Dialogs.class.getDeclaredMethod("getStackTrace", Throwable.class);
        method.setAccessible(true);
        return (String) method.invoke(null, e);
    }

    /**
     * Verifies that the private {@code getStackTrace} method returns a string containing
     * the exception class name and message.
     */
    @Test
    @DisplayName("Should extract stack trace from exception")
    void shouldExtractStackTraceFromException() throws Exception {
        Exception exception = new RuntimeException("Test exception message");
        
        String stackTrace = invokeGetStackTrace(exception);
        
        assertThat(stackTrace)
                .as("Stack trace should not be null")
                .isNotNull()
                .as("Stack trace should contain exception class name")
                .contains("RuntimeException")
                .as("Stack trace should contain exception message")
                .contains("Test exception message");
    }

    /**
     * Verifies that the stack trace includes the outer exception, the cause exception,
     * and a {@code Caused by} line.
     */
    @Test
    @DisplayName("Should extract stack trace with cause")
    void shouldExtractStackTraceWithCause() throws Exception {
        Exception cause = new IllegalArgumentException("Root cause");
        Exception exception = new RuntimeException("Wrapper exception", cause);
        
        String stackTrace = invokeGetStackTrace(exception);
        
        assertThat(stackTrace)
                .as("Stack trace should contain wrapper exception")
                .contains("RuntimeException")
                .as("Stack trace should contain wrapper message")
                .contains("Wrapper exception")
                .as("Stack trace should contain cause exception")
                .contains("IllegalArgumentException")
                .as("Stack trace should contain cause message")
                .contains("Root cause")
                .as("Stack trace should contain 'Caused by'")
                .contains("Caused by");
    }

    /**
     * Verifies that a chain of three exception causes is fully present in the stack trace.
     */
    @Test
    @DisplayName("Should extract stack trace with multiple causes")
    void shouldExtractStackTraceWithMultipleCauses() throws Exception {
        Exception rootCause = new NullPointerException("Null value");
        Exception middleCause = new IllegalStateException("Invalid state", rootCause);
        Exception topException = new RuntimeException("Top level", middleCause);
        
        String stackTrace = invokeGetStackTrace(topException);
        
        assertThat(stackTrace)
                .as("Stack trace should contain all exception types")
                .contains("RuntimeException")
                .contains("IllegalStateException")
                .contains("NullPointerException")
                .as("Stack trace should contain all messages")
                .contains("Top level")
                .contains("Invalid state")
                .contains("Null value");
    }

    /**
     * Verifies that a stack trace is produced even when the exception has no detail message.
     */
    @Test
    @DisplayName("Should extract stack trace without message")
    void shouldExtractStackTraceWithoutMessage() throws Exception {
        Exception exception = new RuntimeException();
        
        String stackTrace = invokeGetStackTrace(exception);
        
        assertThat(stackTrace)
                .as("Stack trace should not be null")
                .isNotNull()
                .as("Stack trace should contain exception class name")
                .contains("RuntimeException");
    }

    /**
     * Verifies that a stack trace is produced when the exception message is an empty string.
     */
    @Test
    @DisplayName("Should extract stack trace with empty message")
    void shouldExtractStackTraceWithEmptyMessage() throws Exception {
        Exception exception = new RuntimeException("");
        
        String stackTrace = invokeGetStackTrace(exception);
        
        assertThat(stackTrace)
                .as("Stack trace should not be null")
                .isNotNull()
                .as("Stack trace should contain exception class name")
                .contains("RuntimeException");
    }

    /**
     * Verifies that the stack trace preserves special characters such as HTML tags,
     * quotes, and backslash characters.
     */
    @Test
    @DisplayName("Should extract stack trace with special characters in message")
    void shouldExtractStackTraceWithSpecialCharactersInMessage() throws Exception {
        Exception exception = new RuntimeException("Error: <tag> & \"quotes\" \n newlines \t tabs");
        
        String stackTrace = invokeGetStackTrace(exception);
        
        assertThat(stackTrace)
                .as("Stack trace should contain special characters")
                .contains("<tag>")
                .contains("&")
                .contains("\"quotes\"");
    }

    /**
     * Verifies that the generated stack trace includes stack frames ({@code at } lines)
     * in addition to the exception class name.
     */
    @Test
    @DisplayName("Should extract stack trace with deep stack")
    void shouldExtractStackTraceWithDeepStack() throws Exception {
        Exception exception = createDeepStackException(10);
        
        String stackTrace = invokeGetStackTrace(exception);
        
        assertThat(stackTrace)
                .as("Stack trace should not be null")
                .isNotNull()
                .as("Stack trace should contain exception class name")
                .contains("RuntimeException")
                .as("Stack trace should contain stack frames")
                .contains("at ");
    }

    /**
     * Verifies that the output of {@code Dialogs.getStackTrace} matches the standard
     * {@link Throwable#printStackTrace()} output character for character.
     */
    @Test
    @DisplayName("Should produce same output as standard printStackTrace")
    void shouldProduceSameOutputAsStandardPrintStackTrace() throws Exception {
        Exception exception = new RuntimeException("Test message");
        
        // Get stack trace using Dialogs method
        String dialogsStackTrace = invokeGetStackTrace(exception);
        
        // Get stack trace using standard method
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String standardStackTrace = sw.toString();
        
        assertThat(dialogsStackTrace)
                .as("Dialogs stack trace should match standard stack trace")
                .isEqualTo(standardStackTrace);
    }

    /**
     * Verifies that a checked exception such as {@link java.io.IOException} is properly
     * included in the stack trace.
     */
    @Test
    @DisplayName("Should handle checked exception")
    void shouldHandleCheckedException() throws Exception {
        Exception exception = new java.io.IOException("File not found");
        
        String stackTrace = invokeGetStackTrace(exception);
        
        assertThat(stackTrace)
                .as("Stack trace should contain IOException")
                .contains("IOException")
                .as("Stack trace should contain message")
                .contains("File not found");
    }

    /**
     * Verifies that a {@link java.lang.Error} such as {@link OutOfMemoryError} is properly
     * included in the stack trace.
     */
    @Test
    @DisplayName("Should handle error")
    void shouldHandleError() throws Exception {
        Error error = new OutOfMemoryError("Java heap space");
        
        String stackTrace = invokeGetStackTrace(error);
        
        assertThat(stackTrace)
                .as("Stack trace should contain OutOfMemoryError")
                .contains("OutOfMemoryError")
                .as("Stack trace should contain message")
                .contains("Java heap space");
    }

    /**
     * Helper method to create an exception with a deep stack trace.
     *
     * @param depth the recursion depth
     * @return the exception
     */
    private Exception createDeepStackException(int depth) {
        if (depth <= 0) {
            return new RuntimeException("Deep stack exception");
        }
        return createDeepStackException(depth - 1);
    }
}
