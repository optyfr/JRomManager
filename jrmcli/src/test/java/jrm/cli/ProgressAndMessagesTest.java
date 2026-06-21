package jrm.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import jrm.aui.progress.ProgressHandler;

class ProgressAndMessagesTest {

    @Nested
    @DisplayName("Progress class tests")
    class ProgressTests {

        private Progress progress;
        private ByteArrayOutputStream outContent;
        private ByteArrayOutputStream errContent;
        private PrintStream originalOut;
        private PrintStream originalErr;

        @BeforeEach
        void setUp() {
            progress = new Progress();
            outContent = new ByteArrayOutputStream();
            errContent = new ByteArrayOutputStream();
            originalOut = System.out;
            originalErr = System.err;
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        @Test
        @DisplayName("should implement ProgressHandler interface")
        void shouldImplementProgressHandler() {
            assertThat(progress).isInstanceOf(ProgressHandler.class);
        }

        @Test
        @DisplayName("setProgress should print message when not quiet and msg is not empty")
        void setProgressShouldPrintMessage() {
            progress.setProgress("Test message", null, null, null);
            assertThat(outContent.toString()).contains("Test message");
        }

        @Test
        @DisplayName("setProgress should print message with count when val > 0")
        void setProgressShouldPrintMessageWithCount() {
            progress.setProgress("Processing", 5, 10, null);
            assertThat(outContent.toString()).contains("Processing (5/10)");
        }

        @Test
        @DisplayName("setProgress should not print when quiet mode is enabled")
        void setProgressShouldNotPrintWhenQuiet() {
            progress.quiet(true);
            progress.setProgress("Test message", null, null, null);
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("setProgress should not print empty message")
        void setProgressShouldNotPrintEmptyMessage() {
            progress.setProgress("", null, null, null);
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("setProgress should not print null message")
        void setProgressShouldNotPrintNullMessage() {
            progress.setProgress(null, null, null, null);
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("quiet(boolean) should set quiet mode")
        void quietShouldSetQuietMode() {
            progress.quiet(true);
            progress.setProgress("Test", null, null, null);
            assertThat(outContent.toString()).isEmpty();

            progress.quiet(false);
            progress.setProgress("Test", null, null, null);
            assertThat(outContent.toString()).contains("Test");
        }

        @Test
        @DisplayName("quiet() should toggle quiet mode")
        void quietNoArgShouldToggleQuietMode() {
            // Initially not quiet
            progress.setProgress("First", null, null, null);
            assertThat(outContent.toString()).contains("First");

            outContent.reset();
            progress.quiet(); // Enable quiet
            progress.setProgress("Second", null, null, null);
            assertThat(outContent.toString()).isEmpty();

            outContent.reset();
            progress.quiet(); // Disable quiet
            progress.setProgress("Third", null, null, null);
            assertThat(outContent.toString()).contains("Third");
        }

        @Test
        @DisplayName("addError should collect errors and print them on close")
        void addErrorShouldCollectAndPrintOnClose() {
            progress.addError("Error 1");
            progress.addError("Error 2");
            assertThat(errContent.toString()).isEmpty();

            progress.close();
            assertThat(errContent.toString()).contains("Error 1", "Error 2");
        }

        @Test
        @DisplayName("close should print multiple errors in order")
        void closeShouldPrintMultipleErrorsInOrder() {
            progress.addError("First error");
            progress.addError("Second error");
            progress.addError("Third error");

            progress.close();
            String output = errContent.toString();
            assertThat(output).contains("First error", "Second error", "Third error");
        }

        @Test
        @DisplayName("close without errors should not print anything")
        void closeWithoutErrorsShouldNotPrint() {
            progress.close();
            assertThat(errContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("isCancel should always return false")
        void isCancelShouldAlwaysReturnFalse() {
            assertThat(progress.isCancel()).isFalse();
        }

        @Test
        @DisplayName("canCancel() should always return false")
        void canCancelShouldAlwaysReturnFalse() {
            assertThat(progress.canCancel()).isFalse();
        }

        @Test
        @DisplayName("getCurrent should always return 0")
        void getCurrentShouldAlwaysReturnZero() {
            assertThat(progress.getCurrent()).isZero();
        }

        @Test
        @DisplayName("getCurrent2 should always return 0")
        void getCurrent2ShouldAlwaysReturnZero() {
            assertThat(progress.getCurrent2()).isZero();
        }

        @Test
        @DisplayName("getCurrent3 should always return 0")
        void getCurrent3ShouldAlwaysReturnZero() {
            assertThat(progress.getCurrent3()).isZero();
        }

        @Test
        @DisplayName("getInputStream should return the same stream passed in")
        void getInputStreamShouldReturnSameStream() {
            java.io.InputStream mockStream = mock(java.io.InputStream.class);
            assertThat(progress.getInputStream(mockStream, 100)).isSameAs(mockStream);
        }

        @Test
        @DisplayName("setProgress should use max parameter when provided")
        void setProgressShouldUseMaxParameter() {
            progress.setProgress("Task", 3, 15, null);
            assertThat(outContent.toString()).contains("Task (3/15)");
        }

        @Test
        @DisplayName("setProgress should not print when val is 0")
        void setProgressShouldNotPrintWhenValIsZero() {
            progress.setProgress("Task", 0, 10, null);
            assertThat(outContent.toString()).contains("Task").doesNotContain("(0/10)");
        }

        @Test
        @DisplayName("setProgress should not print when val is negative")
        void setProgressShouldNotPrintWhenValIsNegative() {
            progress.setProgress("Task", -5, 10, null);
            assertThat(outContent.toString()).contains("Task").doesNotContain("(-5/10)");
        }

        @Test
        @DisplayName("multiple setProgress calls should print multiple lines")
        void multipleSetProgressCallsShouldPrintMultipleLines() {
            progress.setProgress("First", null, null, null);
            progress.setProgress("Second", null, null, null);
            progress.setProgress("Third", null, null, null);

            String output = outContent.toString();
            assertThat(output).contains("First", "Second", "Third");
        }
    }

    @Nested
    @DisplayName("CLIMessages class tests")
    class CLIMessagesTests {

        @Test
        @DisplayName("getString should return message for valid key")
        void getStringShouldReturnMessageForValidKey() {
            String result = CLIMessages.getString("CLI_ERR_UnknownCommand");
            assertThat(result).isNotNull().isNotEmpty().doesNotStartWith("!");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "CLI_ERR_UnknownCommand",
                "CLI_ERR_WrongArgs",
                "CLI_ERR_NoProfileLoaded"
        })
        @DisplayName("getString should return non-null for common keys")
        void getStringShouldReturnNonNullForCommonKeys(String key) {
            assertThat(CLIMessages.getString(key)).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("getString should return formatted string for missing key")
        void getStringShouldReturnFormattedStringForMissingKey() {
            String result = CLIMessages.getString("NONEXISTENT_KEY_12345");
            assertThat(result).isEqualTo("!NONEXISTENT_KEY_12345!");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "MISSING_KEY_1",
                "ANOTHER_MISSING_KEY",
                "CLI_ERR_NONEXISTENT"
        })
        @DisplayName("getString should return !key! format for missing keys")
        void getStringShouldReturnExclamationFormatForMissingKeys(String key) {
            assertThat(CLIMessages.getString(key)).startsWith("!").endsWith("!");
        }

        @Test
        @DisplayName("getString should handle empty string key")
        void getStringShouldHandleEmptyStringKey() {
            String result = CLIMessages.getString("");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("getString should return consistent results for same key")
        void getStringShouldReturnConsistentResults() {
            String first = CLIMessages.getString("CLI_ERR_UnknownCommand");
            String second = CLIMessages.getString("CLI_ERR_UnknownCommand");
            assertThat(first).isEqualTo(second);
        }

        @Test
        @DisplayName("getString should handle keys with underscores and numbers")
        void getStringShouldHandleKeysWithUnderscoresAndNumbers() {
            String result = CLIMessages.getString("CLI_HELP_HELP");
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Progress advanced tests with Mockito")
    class ProgressAdvancedTests {

        private Progress progress;
        private ByteArrayOutputStream outContent;
        private ByteArrayOutputStream errContent;
        private PrintStream originalOut;
        private PrintStream originalErr;

        @BeforeEach
        void setUp() {
            progress = new Progress();
            outContent = new ByteArrayOutputStream();
            errContent = new ByteArrayOutputStream();
            originalOut = System.out;
            originalErr = System.err;
            System.setOut(new PrintStream(outContent));
            System.setErr(new PrintStream(errContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }

        @Test
        @DisplayName("setProgress should use stored max when max param is null but was previously set")
        void setProgressShouldUseStoredMaxWhenMaxParamIsNull() {
            progress.setProgress("Task", 1, 10, null);
            outContent.reset();
            progress.setProgress("Task continued", 5, null, null);
            assertThat(outContent.toString()).contains("Task continued (5/10)");
        }

        @Test
        @DisplayName("setProgress should update max when a new max is provided")
        void setProgressShouldUpdateMaxWhenNewMaxProvided() {
            progress.setProgress("Task", 1, 10, null);
            outContent.reset();
            progress.setProgress("Task", 5, 20, null);
            assertThat(outContent.toString()).contains("Task (5/20)");
            outContent.reset();
            progress.setProgress("Task", 8, null, null);
            assertThat(outContent.toString()).contains("Task (8/20)");
        }

        @Test
        @DisplayName("setProgress with submsg should not crash and should print main message")
        void setProgressWithSubmsgShouldPrintMainMessage() {
            progress.setProgress("Main", 1, 10, "Sub task");
            assertThat(outContent.toString()).contains("Main");
        }

        @Test
        @DisplayName("setProgress with null msg and null val should print nothing")
        void setProgressWithNullMsgAndNullValShouldPrintNothing() {
            progress.setProgress(null, null, null, null);
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("setProgress with empty msg and non-null val should print nothing")
        void setProgressWithEmptyMsgAndNonNullValShouldPrintNothing() {
            progress.setProgress("", 5, 10, null);
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("setProgress with val=0 should print message without count")
        void setProgressWithValZeroShouldPrintMessageWithoutCount() {
            progress.setProgress("Starting", 0, 100, null);
            String output = outContent.toString();
            assertThat(output).contains("Starting").doesNotContain("(0/100)");
        }

        @Test
        @DisplayName("setProgress with negative val should print message without count")
        void setProgressWithNegativeValShouldPrintMessageWithoutCount() {
            progress.setProgress("Reverting", -1, 100, null);
            String output = outContent.toString();
            assertThat(output).contains("Reverting").doesNotContain("(-1/100)");
        }

        @Test
        @DisplayName("quiet(true) then quiet(true) should remain quiet")
        void doubleQuietTrueShouldRemainQuiet() {
            progress.quiet(true);
            progress.quiet(true);
            progress.setProgress("Test", null, null, null);
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("quiet(false) then quiet(false) should remain verbose")
        void doubleQuietFalseShouldRemainVerbose() {
            progress.quiet(false);
            progress.quiet(false);
            progress.setProgress("Test", null, null, null);
            assertThat(outContent.toString()).contains("Test");
        }

        @Test
        @DisplayName("quiet() toggle three times should end in quiet mode")
        void quietToggleThreeTimesShouldEndQuiet() {
            progress.quiet(); // quiet=true
            progress.quiet(); // quiet=false
            progress.quiet(); // quiet=true
            progress.setProgress("Test", null, null, null);
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("addError after close should add to new error list")
        void addErrorAfterCloseShouldWork() {
            progress.addError("Before close");
            progress.close();
            assertThat(errContent.toString()).contains("Before close");
            errContent.reset();
            progress.addError("After close");
            progress.close();
            assertThat(errContent.toString()).contains("After close");
        }

        @Test
        @DisplayName("close called twice should reprint errors (no clearing in implementation)")
        void closeCalledTwiceShouldReprintErrors() {
            progress.addError("Error 1");
            progress.close();
            errContent.reset();
            progress.close();
            assertThat(errContent.toString()).contains("Error 1");
        }

        @Test
        @DisplayName("addError with empty string should still print on close")
        void addErrorWithEmptyStringShouldPrintOnClose() {
            progress.addError("");
            progress.close();
            assertThat(errContent.toString()).contains(System.lineSeparator());
        }

        @Test
        @DisplayName("addError with null should add null and print 'null' on close")
        void addErrorWithNullShouldPrintNullOnClose() {
            progress.addError(null);
            progress.close();
            assertThat(errContent.toString()).contains("null");
        }

        @Test
        @DisplayName("getInputStream should return the exact same stream instance")
        void getInputStreamShouldReturnExactSameInstance() {
            InputStream mockStream = mock(InputStream.class);
            InputStream result = progress.getInputStream(mockStream, 500);
            assertThat(result).isSameAs(mockStream);
        }

        @Test
        @DisplayName("getInputStream with null should return null")
        void getInputStreamWithNullShouldReturnNull() {
            InputStream result = progress.getInputStream(null, 100);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("getInputStream with null length should return same stream")
        void getInputStreamWithNullLengthShouldReturnSameStream() {
            InputStream mockStream = mock(InputStream.class);
            InputStream result = progress.getInputStream(mockStream, null);
            assertThat(result).isSameAs(mockStream);
        }

        @Test
        @DisplayName("doCancel should not throw and should not change isCancel")
        void doCancelShouldNotThrowOrChangeIsCancel() {
            progress.doCancel();
            assertThat(progress.isCancel()).isFalse();
        }

        @Test
        @DisplayName("canCancel(boolean) should not affect canCancel() return value")
        void canCancelSetterShouldNotAffectReturnValue() {
            progress.canCancel(true);
            assertThat(progress.canCancel()).isFalse();
            progress.canCancel(false);
            assertThat(progress.canCancel()).isFalse();
        }

        @Test
        @DisplayName("setInfos should not throw")
        void setInfosShouldNotThrow() {
            assertDoesNotThrow(() -> {
                progress.setInfos(4, true);
                progress.setInfos(1, false);
                progress.setInfos(8, null);
            });
        }

        @Test
        @DisplayName("clearInfos should not throw")
        void clearInfosShouldNotThrow() {
            assertDoesNotThrow(() -> progress.clearInfos());
        }

        @Test
        @DisplayName("setProgress2 should not throw and should not print")
        void setProgress2ShouldNotThrowOrPrint() {
            progress.setProgress2("Secondary", 1, 10);
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("setProgress3 should not throw and should not print")
        void setProgress3ShouldNotThrowOrPrint() {
            progress.setProgress3("Tertiary", 1, 10);
            assertThat(outContent.toString()).isEmpty();
        }

        @Test
        @DisplayName("setOptions should not throw")
        void setOptionsShouldNotThrow() {
            assertDoesNotThrow(() -> {
                progress.setOptions(ProgressHandler.Option.LAZY);
                progress.setOptions(ProgressHandler.Option.LAZY, ProgressHandler.Option.LAZY);
            });
        }

        @Test
        @DisplayName("setOffsetProvider should not throw")
        void setOffsetProviderShouldNotThrow() {
            jrm.misc.OffsetProvider mockProvider = mock(jrm.misc.OffsetProvider.class);
            assertDoesNotThrow(() -> progress.setOffsetProvider(mockProvider));
        }

        @Test
        @DisplayName("getCurrent, getCurrent2, getCurrent3 should all return 0 after various operations")
        void getCurrentValuesShouldAlwaysReturnZero() {
            progress.setProgress("Task", 5, 10, null);
            progress.setProgress2("Task2", 3, 8);
            progress.setProgress3("Task3", 7, 15);
            assertThat(progress.getCurrent()).isZero();
            assertThat(progress.getCurrent2()).isZero();
            assertThat(progress.getCurrent3()).isZero();
        }
    }

    @Nested
    @DisplayName("CLIMessages advanced tests")
    class CLIMessagesAdvancedTests {

        @Test
        @DisplayName("getString should return the same instance for repeated calls (ResourceBundle caching)")
        void getStringShouldReturnConsistentValues() {
            String first = CLIMessages.getString("CLI_ERR_WrongArgs");
            String second = CLIMessages.getString("CLI_ERR_WrongArgs");
            assertThat(first).isEqualTo(second);
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "CLI_ERR_CantChangeDir",
                "CLI_ERR_CantGoUpDir",
                "CLI_ERR_DIRUPD8R_SubCmdMissing",
                "CLI_ERR_NoProfileLoaded",
                "CLI_ERR_NoReport",
                "CLI_ERR_NothingToFix",
                "CLI_ERR_ProfileNotExist",
                "CLI_ERR_PropsChanged",
                "CLI_ERR_ShouldScanFirst",
                "CLI_ERR_TRNTCHK_SubCmdMissing",
                "CLI_ERR_UnknownCommand",
                "CLI_ERR_UnknownDir",
                "CLI_ERR_WrongArgs"
        })
        @DisplayName("getString should return non-empty value for all CLI_ERR_ keys")
        void getStringShouldReturnNonEmptyForAllErrorKeys(String key) {
            String result = CLIMessages.getString(key);
            assertThat(result).isNotNull().isNotEmpty().doesNotStartWith("!");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "CLI_HELP_CD",
                "CLI_HELP_COMPRESSOR",
                "CLI_HELP_DIRUPD8R",
                "CLI_HELP_EXIT",
                "CLI_HELP_FIX",
                "CLI_HELP_HELP",
                "CLI_HELP_LOAD",
                "CLI_HELP_LS",
                "CLI_HELP_MD",
                "CLI_HELP_PREFS",
                "CLI_HELP_PWD",
                "CLI_HELP_QUIET",
                "CLI_HELP_RM",
                "CLI_HELP_SCAN",
                "CLI_HELP_SCANRESULT",
                "CLI_HELP_SET",
                "CLI_HELP_SETTINGS",
                "CLI_HELP_VERBOSE"
        })
        @DisplayName("getString should return descriptive help text for all CLI_HELP_ keys")
        void getStringShouldReturnNonEmptyForAllHelpKeys(String key) {
            String result = CLIMessages.getString(key);
            assertThat(result).isNotNull().isNotEmpty().doesNotStartWith("!");
            assertThat(result.trim().length())
                    .as("Help text for %s should be longer than a single word", key)
                    .isGreaterThan(10);
            assertThat(result).contains(" ");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "CLI_HELP_DIRUPD8R_ADDSDR",
                "CLI_HELP_DIRUPD8R_ADDSRC",
                "CLI_HELP_DIRUPD8R_CLEARSDR",
                "CLI_HELP_DIRUPD8R_CLEARSRC",
                "CLI_HELP_DIRUPD8R_HELP",
                "CLI_HELP_DIRUPD8R_LSSDR",
                "CLI_HELP_DIRUPD8R_LSSRC",
                "CLI_HELP_DIRUPD8R_PRESETS",
                "CLI_HELP_DIRUPD8R_SETTINGS",
                "CLI_HELP_DIRUPD8R_START"
        })
        @DisplayName("getString should return help text containing dirupd8r-related keywords for DIRUPD8R keys")
        void getStringShouldReturnNonEmptyForDirupd8rHelpKeys(String key) {
            String result = CLIMessages.getString(key);
            assertThat(result).isNotNull().isNotEmpty().doesNotStartWith("!");
            assertThat(result.toLowerCase())
                    .as("DIRUPD8R help for %s should mention dirupd8r or related terms", key)
                    .containsAnyOf("dirupd8r", "dirupdater", "sdr", "src", "source", "destination", "dat", "preset", "setting", "help");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "CLI_HELP_TRNTCHK_ADDSDR",
                "CLI_HELP_TRNTCHK_CLEARSDR",
                "CLI_HELP_TRNTCHK_HELP",
                "CLI_HELP_TRNTCHK_LSSDR",
                "CLI_HELP_TRNTCHK_START"
        })
        @DisplayName("getString should return help text containing 'trntchk' or torrent-related keywords for TRNTCHK keys")
        void getStringShouldReturnNonEmptyForTrntchkHelpKeys(String key) {
            String result = CLIMessages.getString(key);
            assertThat(result).isNotNull().isNotEmpty().doesNotStartWith("!");
            assertThat(result.toLowerCase())
                    .as("TRNTCHK help for %s should mention torrent or trntchk", key)
                    .containsAnyOf("trntchk", "torrent", "sdr", "source", "destination", "check", "help");
        }

        @ParameterizedTest
        @CsvSource({
                "CLI_MSG_ActionRemaining, 42",
                "CLI_MSG_PropIsNotSet, myProp",
                "CLI_ERR_ProfileNotExist, myProfile",
                "CLI_ERR_UnknownDir, /some/path",
                "CLI_ERR_CantChangeDir, badDir"
        })
        @DisplayName("getString should return formatted message containing the substituted argument")
        void getStringShouldReturnFormattedMessage(String key, String arg) {
            String template = CLIMessages.getString(key);
            assertThat(template).isNotNull().isNotEmpty();
            String formatted = arg.matches("-?\\d+")
                    ? String.format(template, Integer.parseInt(arg))
                    : String.format(template, arg);
            assertThat(formatted).contains(arg);
        }

        @Test
        @DisplayName("getString should return null-like for empty key without crashing")
        void getStringShouldHandleEmptyKey() {
            String result = CLIMessages.getString("");
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("getString should handle very long key name gracefully")
        void getStringShouldHandleVeryLongKeyName() {
            String longKey = "A".repeat(1000);
            String result = CLIMessages.getString(longKey);
            assertThat(result).startsWith("!").endsWith("!");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "CLI_ERR_UnknownCommand",
                "CLI_ERR_WrongArgs",
                "CLI_ERR_NoProfileLoaded",
                "CLI_HELP_HELP",
                "CLI_MSG_ActionRemaining"
        })
        @DisplayName("getString should not return the key itself for valid keys")
        void getStringShouldNotReturnKeyItselfForValidKeys(String key) {
            String result = CLIMessages.getString(key);
            assertThat(result).isNotEqualTo(key);
        }

        @Test
        @DisplayName("all CLI_ERR_ keys should not contain '!' prefix")
        void allErrorKeysShouldNotContainExclamationPrefix() {
            String[] errorKeys = {
                    "CLI_ERR_CantChangeDir", "CLI_ERR_CantGoUpDir", "CLI_ERR_DIRUPD8R_SubCmdMissing",
                    "CLI_ERR_NoProfileLoaded", "CLI_ERR_NoReport", "CLI_ERR_NothingToFix",
                    "CLI_ERR_ProfileNotExist", "CLI_ERR_PropsChanged", "CLI_ERR_ShouldScanFirst",
                    "CLI_ERR_TRNTCHK_SubCmdMissing", "CLI_ERR_UnknownCommand", "CLI_ERR_UnknownDir",
                    "CLI_ERR_WrongArgs"
            };
            for (String key : errorKeys) {
                assertThat(CLIMessages.getString(key))
                        .as("Key %s should not start with '!'", key)
                        .doesNotStartWith("!");
            }
        }
    }
}
