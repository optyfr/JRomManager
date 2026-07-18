package jrm.server.shared.datasources;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jrm.server.shared.TestDataSets;
import jrm.server.shared.TestWebSessions;
import jrm.server.shared.WebSession;

/**
 * Unit tests for {@link ProfilesListXMLResponse}.
 */
@DisplayName("ProfilesListXMLResponse")
class ProfilesListXMLResponseTest {

    @TempDir
    Path workPath;

    private WebSession session;
    private Path xmlfiles;

    @BeforeEach
    void setUp() throws Exception {
        TestWebSessions.setWorkPath(workPath);
        session = TestWebSessions.newAdminSession("profiles-list-test");
        xmlfiles = session.getUser().getSettings().getWorkPath().resolve("xmlfiles");
        Files.createDirectories(xmlfiles);
        Files.copy(TestDataSets.resolveResource("dats/MAME 0.288 Software List ROMs (merged)/a5200.xml").toPath(), xmlfiles.resolve("a5200.xml"), StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Test
    @DisplayName("fetch lists profiles in the xmlfiles directory")
    void fetchProfiles() throws Exception {
        final String xml = """
                <request>
                  <operationType>fetch</operationType>
                </request>
                """;
        final String output = TestDataSets.processResponse(new ProfilesListXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>").contains("a5200.xml").contains("<record");
    }

    @Test
    @DisplayName("DropCache custom operation deletes the cache file")
    void dropCache() throws Exception {
        Files.createFile(xmlfiles.resolve("a5200.xml.cache"));
        final String xml = """
                <request>
                  <operationType>custom</operationType>
                  <operationId>DropCache</operationId>
                  <data>
                    <File>a5200.xml</File>
                  </data>
                </request>
                """;
        final String output = TestDataSets.processResponse(new ProfilesListXMLResponse(TestDataSets.xmlRequest(session, xml)));
        assertThat(output).contains("<status>0</status>");
        assertThat(xmlfiles.resolve("a5200.xml.cache")).doesNotExist();
    }
}
