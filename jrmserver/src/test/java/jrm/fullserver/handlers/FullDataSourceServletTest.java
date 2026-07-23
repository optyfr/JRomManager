package jrm.fullserver.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletInputStream;
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
 * Unit tests for {@link FullDataSourceServlet}.
 */
@DisplayName("FullDataSourceServlet")
class FullDataSourceServletTest {

    /** Web session for testing. */
    private WebSession webSession;
    /** Servlet under test. */
    private FullDataSourceServlet servlet;

    /** Set up before each test. */
    @BeforeEach
    void setUp() {
        webSession = TestWebSessions.newAdminSession("full-ds-test");
        servlet = new FullDataSourceServlet();
    }

    /** Clean up after each test. */
    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    /**
     * Builds a mock HttpServletRequest with the given URI and body.
     *
     * @param uri the request URI
     * @param body the request body as bytes, or null if no body
     * @return a mock HttpServletRequest configured with the given parameters
     */
    private HttpServletRequest buildRequest(final String uri, final byte[] body) {
        final HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getAttribute("session")).thenReturn(webSession);
        final HttpServletRequest req = mock(HttpServletRequest.class);
        lenient().when(req.getSession()).thenReturn(httpSession);
        lenient().when(req.getRequestURI()).thenReturn(uri);
        lenient().when(req.getContentLength()).thenReturn(body != null ? body.length : 0);
        if (body != null) {
            try {
                lenient().when(req.getInputStream()).thenReturn(new ServletInputStream() {
                    private final ByteArrayInputStream delegate = new ByteArrayInputStream(body);

                    @Override
                    public boolean isFinished() {
                        return delegate.available() == 0;
                    }

                    @Override
                    public boolean isReady() {
                        return true;
                    }

                    @Override
                    public void setReadListener(final jakarta.servlet.ReadListener readListener) {
                        // no-op
                    }

                    @Override
                    public int read() throws IOException {
                        return delegate.read();
                    }
                });
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return req;
    }

    /**
     * Tests for the processResponse dispatch method.
     */
    @Nested
    @DisplayName("processResponse dispatch")
    class ProcessResponseTest {

        /** Tests that an unknown URI delegates to the super implementation and sets SC_NOT_IMPLEMENTED. */
        @Test
        @DisplayName("unknown URI delegates to super and sets SC_NOT_IMPLEMENTED")
        void unknownUri() throws Exception {
            final HttpServletResponse resp = mock(HttpServletResponse.class);
            final String xml = "<request><operationType>fetch</operationType><operationId>op1</operationId></request>";
            final byte[] body = xml.getBytes(StandardCharsets.UTF_8);
            final HttpServletRequest req = buildRequest("/datasources/unknown", body);
            final var result = servlet.processResponse(webSession, req, resp);
            assertThat(result).isNull();
            verify(resp).setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }
}