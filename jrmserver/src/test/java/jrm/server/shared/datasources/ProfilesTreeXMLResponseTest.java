package jrm.server.shared.datasources;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
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
 * Unit tests for {@link ProfilesTreeXMLResponse}.
 */
@DisplayName("ProfilesTreeXMLResponse")
class ProfilesTreeXMLResponseTest {

    @TempDir
    Path workPath;

    private WebSession session;

    @BeforeEach
    void setUp() throws Exception {
        TestWebSessions.setWorkPath(workPath);
        session = TestWebSessions.newAdminSession("profiles-tree-test");
        Files.createDirectories(session.getUser().getSettings().getWorkPath().resolve("xmlfiles/subfolder"));
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Test
    @DisplayName("fetch returns the directory tree")
    void fetchTree() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                </request>
                """;
        final String output = TestDataSets.processResponse(new ProfilesTreeXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("isFolder=\"true\"");
    }

    @Test
    @DisplayName("add creates a new directory")
    void addDirectory() throws Exception {
        session.newProfileList();
        final String xml = """
                <request>
                  <operationType>add</operationType>
                  <data>
                    <title>newfolder</title>
                    <ParentID>0</ParentID>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new ProfilesTreeXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("title=\"newfolder\"").contains("isFolder=\"true\"");
        assertThat(session.getUser().getSettings().getWorkPath().resolve("xmlfiles/newfolder")).exists();
    }
}
