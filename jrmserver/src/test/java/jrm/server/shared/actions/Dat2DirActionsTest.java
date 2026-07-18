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

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import jrm.server.shared.TestWebSessions;
import jrm.server.shared.WebSession;

/**
 * Unit tests for {@link Dat2DirActions} synchronous message methods.
 */
@DisplayName("Dat2DirActions")
class Dat2DirActionsTest {

    
    private ActionsMgr mgr;
    private final List<String> sentMessages = new ArrayList<>();

    @BeforeEach
    void setUp() {
        WebSession webSession;
        webSession = TestWebSessions.newAdminSession("dat2dir-actions-test");
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
        @DisplayName("sends Dat2Dir.updateResult with row and result")
        void updateResult() {
            final Dat2DirActions actions = new Dat2DirActions(mgr);
            actions.updateResult(5, "Updated");
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("Dat2Dir.updateResult");
            assertThat(sentMessages.get(0)).contains("\"row\":5");
            assertThat(sentMessages.get(0)).contains("\"result\":\"Updated\"");
        }

        @Test
        @DisplayName("does not send when not open")
        void updateResultNotOpen() {
            when(mgr.isOpen()).thenReturn(false);
            final Dat2DirActions actions = new Dat2DirActions(mgr);
            actions.updateResult(0, "OK");
            assertThat(sentMessages).isEmpty();
        }
    }

    @Nested
    @DisplayName("clearResults")
    class ClearResultsTest {
        @Test
        @DisplayName("sends Dat2Dir.clearResults")
        void clearResults() {
            final Dat2DirActions actions = new Dat2DirActions(mgr);
            actions.clearResults();
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("Dat2Dir.clearResults");
        }
    }

    @Nested
    @DisplayName("end")
    class EndTest {
        @Test
        @DisplayName("sends Dat2Dir.end")
        void end() {
            final Dat2DirActions actions = new Dat2DirActions(mgr);
            actions.end();
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("Dat2Dir.end");
        }
    }

    @Nested
    @DisplayName("settings")
    class SettingsTest {
        @Test
        @DisplayName("does not send when srcs is empty")
        void settingsEmptySrcs() {
            final JsonObject jso = new JsonObject();
            jso.add("params", new JsonObject().add("srcs", new JsonArray()));
            final Dat2DirActions actions = new Dat2DirActions(mgr);
            actions.settings(jso);
            assertThat(sentMessages).isEmpty();
        }
    }
}