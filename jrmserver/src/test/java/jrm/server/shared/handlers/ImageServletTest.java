package jrm.server.shared.handlers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URISyntaxException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Unit tests for {@link ImageServlet#resolveRequestedResourceUri} path traversal protection.
 */
@DisplayName("ImageServlet path traversal protection")
class ImageServletTest {

    @Nested
    @DisplayName("resolveRequestedResourceUri rejects invalid paths")
    class InvalidPathTest {
        @ParameterizedTest
        @ValueSource(strings = { "/resicons/../etc/passwd", "/resicons/..\\secret", "/resicons/a:b",
                "/resicons/\0null" })
        @DisplayName("rejects traversal and disallowed patterns")
        void rejectsTraversal(final String uri) {
            assertThatThrownBy(() -> ImageServlet.resolveRequestedResourceUri(uri)).isInstanceOf(URISyntaxException.class);
        }

        @Test
        @DisplayName("rejects null URI")
        void rejectsNull() {
            assertThatThrownBy(() -> ImageServlet.resolveRequestedResourceUri(null)).isInstanceOf(URISyntaxException.class);
        }

        @Test
        @DisplayName("rejects too-short URI")
        void rejectsTooShort() {
            assertThatThrownBy(() -> ImageServlet.resolveRequestedResourceUri("/short")).isInstanceOf(URISyntaxException.class);
        }
    }
}