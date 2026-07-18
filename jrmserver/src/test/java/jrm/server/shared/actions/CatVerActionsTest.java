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

import jrm.profile.Profile;
import jrm.server.shared.TestWebSessions;
import jrm.server.shared.WebSession;

/**
 * Unit tests for {@link CatVerActions}.
 */
@DisplayName("CatVerActions")
class CatVerActionsTest {

    private WebSession webSession;
    private ActionsMgr mgr;
    private final List<String> sentMessages = new ArrayList<>();

    @BeforeEach
    void setUp() {
        webSession = TestWebSessions.newAdminSession("catver-test");
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
    @DisplayName("loaded")
    class LoadedTest {
        @Test
        @DisplayName("sends CatVer.loaded with null path when profile has no CatVer")
        void loadedNullCatVer() {
            final Profile profile = mock(Profile.class);
            when(profile.getCatver()).thenReturn(null);
            new CatVerActions(mgr).loaded(profile);
            assertThat(sentMessages).hasSize(1);
            assertThat(sentMessages.get(0)).contains("CatVer.loaded");
            assertThat(sentMessages.get(0)).contains("null");
        }

        @Test
        @DisplayName("does not send when not open")
        void loadedNotOpen() {
            when(mgr.isOpen()).thenReturn(false);
            final Profile profile = mock(Profile.class);
            when(profile.getCatver()).thenReturn(null);
            new CatVerActions(mgr).loaded(profile);
            assertThat(sentMessages).isEmpty();
        }
    }
}