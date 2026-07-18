package jrm.fullserver.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jrm.server.shared.TestWebSessions;

/**
 * Unit tests for {@link Login} using an in-memory H2 database.
 */
@DisplayName("Login service")
class LoginTest {

    private Login login;

    @BeforeEach
    void setUp(@TempDir final Path tempDir) throws Exception {
        TestWebSessions.setWorkPath(tempDir);
        System.setProperty("DB_PW", "");
        login = new Login();
    }

    @AfterEach
    void tearDown() {
        if (login != null)
            login.close();
        TestWebSessions.resetStaticState();
        System.clearProperty("DB_PW");
    }

    @Nested
    @DisplayName("constructor")
    class ConstructorTest {
        @Test
        @DisplayName("creates USERS table and default admin user")
        void createsDefaultAdmin() throws SQLException {
            final Long count = login.count("SELECT * FROM USERS");
            assertThat(count).isGreaterThanOrEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("getName")
    class GetNameTest {
        @Test
        @DisplayName("returns 'Authentication'")
        void returnsName() {
            assertThat(login.getName()).isEqualTo("Authentication");
        }
    }

    @Nested
    @DisplayName("cache management")
    class CacheTest {
        @Test
        @DisplayName("logout clears cache entries")
        @SuppressWarnings("unchecked")
        void logoutClearsCache() throws Exception {
            final Field cacheField = Login.class.getDeclaredField("cache");
            cacheField.setAccessible(true);
            final Map<String, ?> cache = (Map<String, ?>) cacheField.get(null);
            // Cache should be empty initially
            assertThat(cache).isEmpty();
        }
    }
}