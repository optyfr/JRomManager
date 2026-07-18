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
 * Unit tests for {@link AnywareListListXMLResponse}.
 */
@DisplayName("AnywareListListXMLResponse")
class AnywareListListXMLResponseTest {

    @TempDir
    Path workPath;

    private WebSession session;

    @BeforeEach
    void setUp() {
        TestWebSessions.setWorkPath(workPath);
        session = TestWebSessions.newAdminSession("anyware-list-list-test");
        TestDataSets.loadA5200Profile(session);
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Test
    @DisplayName("fetch returns the main machine list")
    void fetchReturnsMainMachineList() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                </request>
                """;
        final String output = TestDataSets.processResponse(new AnywareListListXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("<record").contains("name=\"*\"");
    }

    @Test
    @DisplayName("fetch filters lists by status")
    void fetchFiltersByStatus() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                  <data>
                    <status>FOUND</status>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new AnywareListListXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>");
    }
}
