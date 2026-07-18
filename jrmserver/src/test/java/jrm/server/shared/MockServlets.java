package jrm.server.shared;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Helpers to build Mockito mocks of servlet request/response objects wired to a {@link WebSession} attribute, with a
 * {@link StringWriter}-backed response writer for JSON/XML body assertions.
 */
final class MockServlets {

    /** Private constructor — utility class. */
    private MockServlets() {
    }

    /**
     * Builds a mocked {@link HttpServletRequest} whose {@code getSession()} returns a session holding the given
     * {@link WebSession} under the {@code "session"} attribute, and whose request URI and content-length are preset.
     *
     * @param webSession the web session to attach
     * @param requestUri the request URI to return
     * @param contentLength the content length header value
     * @return the mocked request
     */
    static HttpServletRequest mockRequest(final WebSession webSession, final String requestUri, final long contentLength) {
        final HttpSession httpSession = mock(HttpSession.class);
        when(httpSession.getAttribute("session")).thenReturn(webSession);
        when(httpSession.getId()).thenReturn(webSession.getSessionId());

        final HttpServletRequest req = mock(HttpServletRequest.class);
        lenient().when(req.getSession()).thenReturn(httpSession);
        lenient().when(req.getRequestURI()).thenReturn(requestUri);
        lenient().when(req.getContentLength()).thenReturn((int) Math.max(contentLength, Integer.MIN_VALUE));
        lenient().when(req.getContentLengthLong()).thenReturn(contentLength);
        return req;
    }

    /**
     * Builds a mocked {@link HttpServletRequest} with a body input stream.
     *
     * @param webSession the web session to attach
     * @param requestUri the request URI to return
     * @param body the request body bytes
     * @return the mocked request
     */
    static HttpServletRequest mockRequestWithBody(final WebSession webSession, final String requestUri, final byte[] body) {
        final HttpServletRequest req = mockRequest(webSession, requestUri, body.length);
        try {
            lenient().when(req.getInputStream()).thenReturn(new jakarta.servlet.ServletInputStream() {
                private final InputStream delegate = new ByteArrayInputStream(body);

                @Override
                public boolean isFinished() {
                    try {
                        return delegate.available() == 0;
                    } catch (IOException _) {
                        return true;
                    }
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
        return req;
    }

    /**
     * Builds a mocked {@link HttpServletResponse} that captures written body content in a {@link StringWriter} and tracks status
     * code, content type, and headers set on it.
     *
     * @param writer the string writer to back {@code getWriter()}
     * @return the mocked response
     */
    static HttpServletResponse mockResponse(final StringWriter writer) throws IOException {
        final HttpServletResponse resp = mock(HttpServletResponse.class);
        lenient().when(resp.getWriter()).thenReturn(new PrintWriter(writer));
        return resp;
    }
}