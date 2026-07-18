package jrm.fullserver.datasources;

import static org.assertj.core.api.Assertions.assertThat;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jrm.fullserver.security.Login;
import jrm.server.shared.TestWebSessions;
import jrm.server.shared.WebSession;
import jrm.server.shared.datasources.XMLRequest;
import jrm.server.shared.TempFileInputStream;

/**
 * Unit tests for {@link AdminXMLResponse} using an in-memory H2 database.
 */
@DisplayName("AdminXMLResponse")
class AdminXMLResponseTest {

    private WebSession adminSession;
    private WebSession nonAdminSession;

    @BeforeEach
    void setUp(@TempDir final Path tempDir) throws Exception {
        TestWebSessions.setWorkPath(tempDir);
        System.setProperty("DB_PW", "");
        adminSession = TestWebSessions.newAdminSession("admin-xml-test");
        nonAdminSession = TestWebSessions.newSession("non-admin-xml-test", "user", new String[] { "user" });
        // Initialize the Login table with default admin
        try (Login login = new Login()) {
            assertThat(login.checkAdmin()).isTrue();    
        }
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
        System.clearProperty("DB_PW");
    }

    private XMLRequest buildRequest(final WebSession session, final String xml) throws Exception {
        return new XMLRequest(session, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), xml.length());
    }

    @Nested
    @DisplayName("fetch")
    class FetchTest {
        @Test
        @DisplayName("admin fetch lists users with status 0")
        void adminFetch() throws Exception {
            final String xml = "<request><operationType>fetch</operationType><operationId>op1</operationId></request>";
            final XMLRequest req = buildRequest(adminSession, xml);
            try (final AdminXMLResponse resp = new AdminXMLResponse(req)) {
                final TempFileInputStream tfis = resp.processRequest();
                final String output = new String(tfis.readAllBytes(), StandardCharsets.UTF_8);
                assertThat(output).contains("<status>0</status>");
                assertThat(output).contains("admin");
            }
        }

        @Test
        @DisplayName("non-admin fetch returns failure with Can't do that!")
        void nonAdminFetch() throws Exception {
            final String xml = "<request><operationType>fetch</operationType><operationId>op1</operationId></request>";
            final XMLRequest req = buildRequest(nonAdminSession, xml);
            try (final AdminXMLResponse resp = new AdminXMLResponse(req)) {
                final TempFileInputStream tfis = resp.processRequest();
                final String output = new String(tfis.readAllBytes(), StandardCharsets.UTF_8);
                assertThat(output).contains("Can't do that!");
            }
        }
    }
}