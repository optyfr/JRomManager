package jrm.server.shared.datasources;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jrm.server.shared.TestDataSets;
import jrm.server.shared.TestWebSessions;
import jrm.server.shared.WebSession;

/**
 * Unit tests for {@link BatchCompressorFRXMLResponse}.
 */
@DisplayName("BatchCompressorFRXMLResponse")
class BatchCompressorFRXMLResponseTest {

    @TempDir
    Path workPath;

    private WebSession session;

    @BeforeEach
    void setUp() {
        TestWebSessions.setWorkPath(workPath);
        session = TestWebSessions.newAdminSession("compressor-fr-test");
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Test
    @DisplayName("fetch returns empty list when no file results are cached")
    void fetchEmpty() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                </request>
                """;
        final String output = TestDataSets.processResponse(new BatchCompressorFRXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("<totalRows>0</totalRows>");
    }

    @Test
    @DisplayName("add caches a new file result")
    void addResult() throws Exception {
        final String xml = """
                <request>
                  <operationType>add</operationType>
                  <data>
                    <file>%work/archive.zip</file>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new BatchCompressorFRXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("archive.zip").contains("result=\"\"");

        final String fetchXml = """
                <request>
                  <operationType>fetch</operationType>
                </request>
                """;
        final String fetchOutput = TestDataSets.processResponse(new BatchCompressorFRXMLResponse(TestDataSets.xmlRequest(session, fetchXml)));
        assertThat(fetchOutput).contains("archive.zip");
    }

    @Test
    @DisplayName("clear custom operation empties the cache")
    void clearCache() throws Exception {
        final String addXml = """
                <request>
                  <operationType>add</operationType>
                  <data>
                    <file>%work/archive.zip</file>
                  </data>
                </request>
                """;
        TestDataSets.processResponse(new BatchCompressorFRXMLResponse(TestDataSets.xmlRequest(session, addXml)));

        final String clearXml = """
                <request>
                  <operationType>custom</operationType>
                  <operationId>clear</operationId>
                </request>
                """;
        final String output = TestDataSets.processResponse(new BatchCompressorFRXMLResponse(TestDataSets.xmlRequest(session, clearXml)));
        assertThat(output).contains("<status>0</status>");
        assertThat(session.getCachedCompressorList()).isEmpty();
    }
}
