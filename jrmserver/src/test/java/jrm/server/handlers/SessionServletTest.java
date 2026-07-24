package jrm.server.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jrm.server.shared.TestWebSessions;
import jrm.server.shared.WebSession;

/**
 * Unit tests for {@link SessionServlet} (server variant).
 */
@DisplayName("SessionServlet (server)")
class SessionServletTest {

    /** The web session to use for the tests. */
    private WebSession webSession;

    @BeforeEach
    void setUp() {
        webSession = TestWebSessions.newAdminSession("server-servlet-test");
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    /** Builds a mock HttpServletRequest with a preconfigured WebSession. */
    private HttpServletRequest buildRequest() {
        final HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getAttribute("session")).thenReturn(webSession);
        when(httpSession.getId()).thenReturn("server-servlet-test");
        final HttpServletRequest req = mock(HttpServletRequest.class);
        lenient().when(req.getSession()).thenReturn(httpSession);
        lenient().when(req.getHeader("accept-language")).thenReturn("en");
        return req;
    }

    @Nested
    @DisplayName("doPost success")
    class DoPostSuccessTest {
        @Test
        @DisplayName("writes JSON with session, msgs, settings")
        void doPostSuccess() throws Exception {
            final StringWriter writer = new StringWriter();
            final HttpServletResponse resp = mock(HttpServletResponse.class);
            when(resp.getWriter()).thenReturn(new PrintWriter(writer));
            final HttpServletRequest req = buildRequest();

            final SessionServlet servlet = new SessionServlet();
            servlet.doPost(req, resp);

            final String json = writer.toString();
            assertThat(json).contains("\"session\"").contains("\"msgs\"").contains("\"settings\"");
        }
    }

    @Nested
    @DisplayName("doPost exception")
    class DoPostExceptionTest {
        @Test
        @DisplayName("sets SC_INTERNAL_SERVER_ERROR on exception")
        void doPostException() throws Exception {
            final HttpServletResponse resp = mock(HttpServletResponse.class);
            when(resp.getWriter()).thenThrow(new RuntimeException("boom"));
            final HttpServletRequest req = buildRequest();

            final SessionServlet servlet = new SessionServlet();
            servlet.doPost(req, resp);

            verify(resp).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}