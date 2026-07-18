package jrm.server.shared.actions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jrm.server.shared.TestWebSessions;
import jrm.server.shared.WebSession;

/**
 * Unit tests for {@link CompressorActions} synchronous message methods.
 */
@DisplayName("CompressorActions")
class CompressorActionsTest {

    
    private ActionsMgr mgr;
    private final List<String> sentMessages = new ArrayList<>();

    @BeforeEach
    void setUp() {
        WebSession webSession;
        webSession = TestWebSessions.newAdminSession("compressor-actions-test");
        sentMessages.clear();
        mgr = mock(ActionsMgr.class);
        when(mgr.getSession()).thenReturn(webSession);
        when(mgr.isOpen()).thenReturn(true);
        try {
            doAnswer(inv -> {
                sentMessages.add(inv.getArgument(0));
                return null;
            }).when(mgr).send(anyString());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Nested
    @DisplayName("updateResult")
    class UpdateResultTest {
        @Test
        @DisplayName("sends Compressor.updateResult with row and result")
        void updateResult() {
            final CompressorActions actions = new CompressorActions(mgr);
            actions.updateResult(3, "Success");
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("Compressor.updateResult");
            assertThat(sentMessages.get(0)).contains("\"row\":3");
            assertThat(sentMessages.get(0)).contains("\"result\":\"Success\"");
        }

        @Test
        @DisplayName("does not send when not open")
        void updateResultNotOpen() {
            when(mgr.isOpen()).thenReturn(false);
            final CompressorActions actions = new CompressorActions(mgr);
            actions.updateResult(0, "OK");
            assertThat(sentMessages).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateFile")
    class UpdateFileTest {
        @Test
        @DisplayName("sends Compressor.updateFile with row and file path")
        void updateFile() {
            final CompressorActions actions = new CompressorActions(mgr);
            actions.updateFile(2, Path.of("/tmp/test.zip"));
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("Compressor.updateFile");
            assertThat(sentMessages.get(0)).contains("\"row\":2");
            assertThat(sentMessages.get(0)).contains("test.zip");
        }
    }

    @Nested
    @DisplayName("clearResults")
    class ClearResultsTest {
        @Test
        @DisplayName("sends Compressor.clearResults")
        void clearResults() {
            final CompressorActions actions = new CompressorActions(mgr);
            actions.clearResults();
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("Compressor.clearResults");
        }
    }

    @Nested
    @DisplayName("end")
    class EndTest {
        @Test
        @DisplayName("sends Compressor.end")
        void end() {
            final CompressorActions actions = new CompressorActions(mgr);
            actions.end();
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("Compressor.end");
        }
    }
}