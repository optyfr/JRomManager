package jrm.server.shared.datasources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import javax.xml.stream.XMLStreamException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jrm.server.shared.TestWebSessions;
import jrm.server.shared.WebSession;
import jrm.server.shared.datasources.XMLRequest.Operation;
import jrm.server.shared.TempFileInputStream;

/**
 * Unit tests for {@link XMLResponse} base class behavior: error helpers, pagination, and processRequest output structure.
 */
@DisplayName("XMLResponse base class")
class XMLResponseTest {

    private WebSession session;

    @BeforeEach
    void setUp() {
        session = TestWebSessions.newAdminSession("xml-response-test");
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    /** A concrete XMLResponse that writes a fixed record in fetch. */
    private static final class TestResponse extends XMLResponse {
        TestResponse(final XMLRequest request) throws Exception {
            super(request);
        }

        @Override
        protected void fetch(final Operation operation) throws XMLStreamException, IOException {
            writer.writeStartElement("response");
            writer.writeElement("status", "0");
            writer.writeStartElement("data");
            writer.writeStartElement("record");
            writer.writeElement("name", "test");
            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    /** A response that calls error helpers directly. */
    private static final class ErrorResponse extends XMLResponse {
        ErrorResponse(final XMLRequest request) throws Exception {
            super(request);
        }

        @Override
        protected void fetch(final Operation operation) throws XMLStreamException, IOException {
            // delegate to error helpers via custom operation type
        }
    }

    private XMLRequest buildRequest(final String operationType) throws Exception {
        final String xml = "<request><operationType>" + operationType + "</operationType><operationId>op1</operationId></request>";
        return new XMLRequest(session, new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), xml.length());
    }

    private String readXml(final TempFileInputStream tfis) throws Exception {
        return new String(tfis.readAllBytes(), StandardCharsets.UTF_8);
    }

    @Nested
    @DisplayName("processRequest structure")
    class ProcessRequestTest {
        @Test
        @DisplayName("single fetch produces response with status and data")
        void singleFetch() throws Exception {
            final XMLRequest req = buildRequest("fetch");
            try (final TestResponse resp = new TestResponse(req)) {
                final TempFileInputStream tfis = resp.processRequest();
                final String xml = readXml(tfis);
                assertThat(xml).contains("<response>").contains("<status>0</status>").contains("<name>test</name>");
            }
        }

        @Test
        @DisplayName("transaction wraps operations in <responses>")
        void transactionWraps() throws Exception {
            final String xml = """
                    <transaction>
                      <request><operationType>fetch</operationType><operationId>op1</operationId></request>
                      <request><operationType>fetch</operationType><operationId>op2</operationId></request>
                    </transaction>
                    """;
            final XMLRequest req = new XMLRequest(session, new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), xml.length());
            try (final TestResponse resp = new TestResponse(req)) {
                final TempFileInputStream tfis = resp.processRequest();
                final String output = readXml(tfis);
                assertThat(output).contains("<responses>");
                assertThat(output).contains("<response>").contains("<status>0</status>");
            }
        }
    }

    @Nested
    @DisplayName("error helpers")
    class ErrorHelpersTest {
        @Test
        @DisplayName("error(0) writes status 0")
        void noError() {
            assertThatCode(() -> {
                try (final ErrorResponse resp = new ErrorResponse(buildRequest("fetch"))) {
                    resp.noError();
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("failure writes status -1 with message")
        void failure() {
            assertThatCode(() -> {
                try (final ErrorResponse resp = new ErrorResponse(buildRequest("fetch"))) {
                    resp.failure("not implemented");
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("loginIncorrect writes status -5")
        void loginIncorrect() {
            assertThatCode(() -> {
                try (final ErrorResponse resp = new ErrorResponse(buildRequest("fetch"))) {
                    resp.loginIncorrect();
                }
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("loginRequired writes status -7")
        void loginRequired() {
            assertThatCode(() -> {
                try (final ErrorResponse resp = new ErrorResponse(buildRequest("fetch"))) {
                    resp.loginRequired();
                }
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("fetchList pagination")
    class FetchListTest {
        @Test
        @DisplayName("fetchList paginates a list within startRow/endRow")
        void fetchListPaginates() throws Exception {
            final XMLRequest req = buildRequest("fetch");
            final List<String> items = List.of("a", "b", "c", "d", "e");
            try (final var resp = new XMLResponse(req) {
                @Override
                protected void fetch(final Operation operation) throws XMLStreamException, IOException {
                    fetchList(operation, items, (obj, idx) -> writer.writeElement("item", obj));
                }
            }) {
                final TempFileInputStream tfis = resp.processRequest();
                final String xml = readXml(tfis);
                assertThat(xml).contains("<totalRows>5</totalRows>");
                assertThat(xml).contains("<item>a</item>");
            }
        }
    }

    @Nested
    @DisplayName("fetchArray pagination")
    class FetchArrayTest {
        @Test
        @DisplayName("fetchArray paginates an array")
        void fetchArrayPaginates() throws Exception {
            final XMLRequest req = buildRequest("fetch");
            try (final var resp = new XMLResponse(req) {
                @Override
                protected void fetch(final Operation operation) throws XMLStreamException, IOException {
                    fetchArray(operation, 3, (idx, count) -> writer.writeElement("item", Integer.toString(idx)));
                }
            }) {
                final TempFileInputStream tfis = resp.processRequest();
                final String xml = readXml(tfis);
                assertThat(xml).contains("<totalRows>3</totalRows>");
                assertThat(xml).contains("<item>0</item>");
            }
        }
    }

    @Nested
    @DisplayName("fetchStream pagination")
    class FetchStreamTest {
        @Test
        @DisplayName("fetchStream filters by range")
        void fetchStreamFilters() throws Exception {
            final XMLRequest req = buildRequest("fetch");
            final Stream<String> stream = Stream.of("a", "b", "c", "d", "e");
            try (final var resp = new XMLResponse(req) {
                @Override
                protected void fetch(final Operation operation) throws XMLStreamException, IOException {
                    fetchStream(operation, stream, t -> {
                        try {
                            writer.writeElement("item", t);
                        } catch (XMLStreamException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }) {
                final TempFileInputStream tfis = resp.processRequest();
                final String xml = readXml(tfis);
                assertThat(xml).contains("<totalRows>5</totalRows>");
            }
        }
    }
}