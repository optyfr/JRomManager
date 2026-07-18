package jrm.server.shared.actions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
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
 * Unit tests for {@link TrntChkActions} synchronous message methods.
 */
@DisplayName("TrntChkActions")
class TrntChkActionsTest {

    
    private ActionsMgr mgr;
    private final List<String> sentMessages = new ArrayList<>();

    @BeforeEach
    void setUp() {
        WebSession webSession;
        webSession = TestWebSessions.newAdminSession("trntchk-actions-test");
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
        @DisplayName("sends TrntChk.updateResult with row and result")
        void updateResult() {
            final TrntChkActions actions = new TrntChkActions(mgr);
            actions.updateResult(1, "Complete");
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("TrntChk.updateResult");
            assertThat(sentMessages.get(0)).contains("\"row\":1");
            assertThat(sentMessages.get(0)).contains("\"result\":\"Complete\"");
        }

        @Test
        @DisplayName("does not send when not open")
        void updateResultNotOpen() {
            when(mgr.isOpen()).thenReturn(false);
            final TrntChkActions actions = new TrntChkActions(mgr);
            actions.updateResult(0, "OK");
            assertThat(sentMessages).isEmpty();
        }
    }

    @Nested
    @DisplayName("clearResults")
    class ClearResultsTest {
        @Test
        @DisplayName("sends TrntChk.clearResults")
        void clearResults() {
            final TrntChkActions actions = new TrntChkActions(mgr);
            actions.clearResults();
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("TrntChk.clearResults");
        }
    }

    @Nested
    @DisplayName("end")
    class EndTest {
        @Test
        @DisplayName("sends TrntChk.end")
        void end() {
            final TrntChkActions actions = new TrntChkActions(mgr);
            actions.end();
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("TrntChk.end");
        }
    }
}