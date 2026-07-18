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
 * Unit tests for {@link BatchDat2DirSrcXMLResponse}.
 */
@DisplayName("BatchDat2DirSrcXMLResponse")
class BatchDat2DirSrcXMLResponseTest {

    @TempDir
    Path workPath;

    private WebSession session;

    @BeforeEach
    void setUp() {
        TestWebSessions.setWorkPath(workPath);
        session = TestWebSessions.newAdminSession("batch-src-test");
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Test
    @DisplayName("fetch returns empty list when no source directories are configured")
    void fetchEmpty() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                </request>
                """;
        final String output = TestDataSets.processResponse(new BatchDat2DirSrcXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("<totalRows>0</totalRows>");
    }

    @Test
    @DisplayName("add and remove source directories")
    void addAndRemove() throws Exception {
        final String addXml = """
                <request>
                  <operationType>add</operationType>
                  <data>
                    <name>%work/roms</name>
                  </data>
                </request>
                """;
        final String addOutput = TestDataSets.processResponse(new BatchDat2DirSrcXMLResponse(TestDataSets.xmlRequest(session, addXml)));
        assertThat(addOutput).contains("<status>0</status>").contains("name=\"%work/roms\"");

        final String removeXml = """
                <request>
                  <operationType>remove</operationType>
                  <data>
                    <name>%work/roms</name>
                  </data>
                </request>
                """;
        final String removeOutput = TestDataSets.processResponse(new BatchDat2DirSrcXMLResponse(TestDataSets.xmlRequest(session, removeXml)));
        assertThat(removeOutput).contains("<status>0</status>").contains("name=\"%work/roms\"");
    }
}
