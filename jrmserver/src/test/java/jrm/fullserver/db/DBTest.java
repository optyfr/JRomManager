package jrm.fullserver.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jrm.fullserver.ServerSettings;
import jrm.server.shared.TestWebSessions;

/**
 * Unit tests for {@link DB}.
 */
@DisplayName("DB factory")
class DBTest {

    @BeforeEach
    void setUp(@TempDir final Path tempDir) {
        TestWebSessions.setWorkPath(tempDir);
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
    }

    @Nested
    @DisplayName("getInstance")
    class GetInstanceTest {
        @Test
        @DisplayName("getInstance(settings) returns H2 via default getDBClass")
        void getInstanceViaSettings() {
            final DB db = DB.getInstance(new ServerSettings());
            assertThat(db).isNotNull();
        }

        @Test
        @DisplayName("getInstance(className, settings) returns H2")
        void getInstanceViaClassName() throws Exception {
            final DB db = DB.getInstance("jrm.fullserver.db.H2", new ServerSettings());
            assertThat(db).isNotNull();
        }

        @Test
        @DisplayName("getInstance(H2.class, settings) returns H2")
        void getInstanceViaClass() {
            final DB db = DB.getInstance(H2.class, new ServerSettings());
            assertThat(db).isNotNull().isInstanceOf(H2.class);
        }

        @Test
        @DisplayName("getInstance with invalid class name throws ClassNotFoundException")
        void getInstanceInvalidClass() {
            assertThatThrownBy(() -> DB.getInstance("jrm.fullserver.db.NonExistent", new ServerSettings()))
                    .isInstanceOf(ClassNotFoundException.class);
        }
    }
}