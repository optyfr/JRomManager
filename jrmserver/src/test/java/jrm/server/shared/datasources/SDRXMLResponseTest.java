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
 * Unit tests for {@link SDRXMLResponse} subclasses.
 */
@DisplayName("SDRXMLResponse subclasses")
class SDRXMLResponseTest {

    @TempDir
    Path workPath;

    private WebSession session;

    @BeforeEach
    void setUp() {
        TestWebSessions.setWorkPath(workPath);
        session = TestWebSessions.newAdminSession("sdr-test");
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Test
    @DisplayName("BatchDat2DirSDRXMLResponse fetch returns empty list")
    void batchDat2DirFetch() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                </request>
                """;
        final String output = TestDataSets.processResponse(new BatchDat2DirSDRXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("<totalRows>0</totalRows>");
    }

    @Test
    @DisplayName("BatchDat2DirSDRXMLResponse add returns the new entry")
    void batchDat2DirAdd() throws Exception {
        final String addXml = """
                <request>
                  <operationType>add</operationType>
                  <data>
                    <src>%work/src</src>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new BatchDat2DirSDRXMLResponse(TestDataSets.xmlRequest(session, addXml)));
        assertThat(output).contains("<status>0</status>").contains("src=\"%work/src\"");
    }

    @Test
    @DisplayName("BatchTrntChkSDRXMLResponse add returns the new entry")
    void batchTrntChkAdd() throws Exception {
        final String addXml = """
                <request>
                  <operationType>add</operationType>
                  <data>
                    <src>%work/torrent</src>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new BatchTrntChkSDRXMLResponse(TestDataSets.xmlRequest(session, addXml)));
        assertThat(output).contains("<status>0</status>").contains("src=\"%work/torrent\"");
    }

    @Test
    @DisplayName("BatchTrntChkSDRXMLResponse fetch returns empty list")
    void batchTrntChkFetch() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                </request>
                """;
        final String output = TestDataSets.processResponse(new BatchTrntChkSDRXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("<totalRows>0</totalRows>");
    }
}
