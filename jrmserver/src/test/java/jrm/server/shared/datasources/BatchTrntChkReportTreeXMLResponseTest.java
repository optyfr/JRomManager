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
 * Unit tests for {@link BatchTrntChkReportTreeXMLResponse}.
 */
@DisplayName("BatchTrntChkReportTreeXMLResponse")
class BatchTrntChkReportTreeXMLResponseTest {

    @TempDir
    Path workPath;

    private WebSession session;

    @BeforeEach
    void setUp() {
        TestWebSessions.setWorkPath(workPath);
        session = TestWebSessions.newAdminSession("trntchk-report-test");
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Test
    @DisplayName("fetch without src returns success")
    void fetchWithoutSrc() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                  <data>
                    <ParentID>0</ParentID>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new BatchTrntChkReportTreeXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>");
    }
}
