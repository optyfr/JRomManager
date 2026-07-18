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
 * Unit tests for {@link AnywareListXMLResponse}.
 */
@DisplayName("AnywareListXMLResponse")
class AnywareListXMLResponseTest {

    @TempDir
    Path workPath;

    private WebSession session;

    @BeforeEach
    void setUp() {
        TestWebSessions.setWorkPath(workPath);
        session = TestWebSessions.newAdminSession("anyware-list-test");
        TestDataSets.loadA5200Profile(session);
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Test
    @DisplayName("fetch returns records for the main machine list")
    void fetchMainList() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                  <data>
                    <list>*</list>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new AnywareListXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("list=\"*\"");
    }

    @Test
    @DisplayName("fetch filters machines by name")
    void fetchFiltersByName() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                  <data>
                    <list>*</list>
                    <name>asteroid</name>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new AnywareListXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("name=\"asteroid\"");
    }

    @Test
    @DisplayName("update changes selection state")
    void updateSelection() throws Exception {
        final String xml = """
                <request>
                  <operationType>update</operationType>
                  <data>
                    <list>*</list>
                    <name>asteroid</name>
                    <selected>true</selected>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new AnywareListXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("selected=\"true\"").contains("name=\"asteroid\"");
    }

    @Test
    @DisplayName("selectAll custom operation selects all filtered items")
    void selectAll() throws Exception {
        final String xml = """
                <request>
                  <operationType>custom</operationType>
                  <operationId>selectAll</operationId>
                  <data>
                    <list>*</list>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new AnywareListXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>");
    }

    @Test
    @DisplayName("find custom operation returns the index of a matching item")
    void find() throws Exception {
        final String xml = """
                <request>
                  <operationType>custom</operationType>
                  <operationId>find</operationId>
                  <data>
                    <list>*</list>
                    <find>asteroid</find>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new AnywareListXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("<found>");
    }
}
