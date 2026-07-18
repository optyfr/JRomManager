package jrm.server.shared.handlers;

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
 * Unit tests for {@link DataSourceServlet}.
 */
@DisplayName("DataSourceServlet")
class DataSourceServletTest {

    private WebSession webSession;
    private DataSourceServlet servlet;

    @BeforeEach
    void setUp() {
        webSession = TestWebSessions.newAdminSession("ds-test");
        servlet = new DataSourceServlet();
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    private HttpServletRequest buildRequest(final String uri, final int contentLength, final String contentType,
            final byte[] body) {
        final HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getAttribute("session")).thenReturn(webSession);
        final HttpServletRequest req = mock(HttpServletRequest.class);
        lenient().when(req.getSession()).thenReturn(httpSession);
        lenient().when(req.getRequestURI()).thenReturn(uri);
        lenient().when(req.getContentLengthLong()).thenReturn((long) contentLength);
        lenient().when(req.getContentLength()).thenReturn(contentLength);
        lenient().when(req.getContentType()).thenReturn(contentType);
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

    @Nested
    @DisplayName("doPost validation")
    class DoPostValidationTest {
        @Test
        @DisplayName("negative contentLengthLong returns SC_LENGTH_REQUIRED")
        void lengthRequired() throws Exception {
            final HttpServletResponse resp = mock(HttpServletResponse.class);
            final HttpServletRequest req = buildRequest("/datasources/test", -1, null, null);
            servlet.doPost(req, resp);
            verify(resp).setStatus(HttpServletResponse.SC_LENGTH_REQUIRED);
        }

        @Test
        @DisplayName("negative contentLength returns SC_REQUEST_ENTITY_TOO_LARGE")
        void tooLarge() throws Exception {
            final HttpServletResponse resp = mock(HttpServletResponse.class);
            final HttpServletRequest req = buildRequest("/datasources/test", -1, null, null);
            lenient().when(req.getContentLengthLong()).thenReturn(1L);
            lenient().when(req.getContentLength()).thenReturn(-1);
            servlet.doPost(req, resp);
            verify(resp).setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
        }
    }

    @Nested
    @DisplayName("processResponse dispatch")
    class ProcessResponseTest {
        @Test
        @DisplayName("unknown URI sets SC_NOT_IMPLEMENTED and returns null")
        void unknownUri() throws Exception {
            final HttpServletResponse resp = mock(HttpServletResponse.class);
            final String xml = "<request><operationType>fetch</operationType><operationId>op1</operationId></request>";
            final byte[] body = xml.getBytes(StandardCharsets.UTF_8);
            final HttpServletRequest req = buildRequest("/datasources/unknown", body.length, "text/xml", body);
            final var result = servlet.processResponse(webSession, req, resp);
            assertThat(result).isNull();
            verify(resp).setStatus(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }
}