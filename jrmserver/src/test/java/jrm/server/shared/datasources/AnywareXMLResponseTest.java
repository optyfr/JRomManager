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
 * Unit tests for {@link AnywareXMLResponse}.
 */
@DisplayName("AnywareXMLResponse")
class AnywareXMLResponseTest {

    @TempDir
    Path workPath;

    private WebSession session;

    @BeforeEach
    void setUp() {
        TestWebSessions.setWorkPath(workPath);
        session = TestWebSessions.newAdminSession("anyware-test");
        TestDataSets.loadA5200Profile(session);
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Test
    @DisplayName("fetch returns ROM records for a machine")
    void fetchMachineRoms() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                  <data>
                    <list>*</list>
                    <ware>asteroid</ware>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new AnywareXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("ware=\"asteroid\"").contains("type=\"ROM\"");
    }

    @Test
    @DisplayName("fetch with status filter returns matching records")
    void fetchWithStatusFilter() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                  <data>
                    <list>*</list>
                    <ware>asteroid</ware>
                    <status>FOUND</status>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new AnywareXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>");
    }
}
