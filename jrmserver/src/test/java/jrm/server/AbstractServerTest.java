package jrm.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jrm.server.shared.TestWebSessions;

/**
 * Unit tests for {@link AbstractServer} static helpers.
 */
@DisplayName("AbstractServer helpers")
class AbstractServerTest {

    private String originalWorkDir;

    @BeforeEach
    void setUp() {
        originalWorkDir = System.getProperty("jrommanager.dir");
    }

    @AfterEach
    void tearDown() {
        TestWebSessions.resetStaticState();
        if (originalWorkDir != null)
            System.setProperty("jrommanager.dir", originalWorkDir);
        else
            System.clearProperty("jrommanager.dir");
    }

    @Nested
    @DisplayName("getWorkPath")
    class GetWorkPathTest {
        @Test
        @DisplayName("uses jrommanager.dir system property when set")
        void usesSystemProperty(@TempDir final Path tempDir) {
            System.setProperty("jrommanager.dir", tempDir.toString());
            assertThat(AbstractServer.getWorkPath()).isEqualTo(tempDir);
        }

        @Test
        @DisplayName("falls back to user.dir when jrommanager.dir not set")
        void fallsBackToUserDir() {
            System.clearProperty("jrommanager.dir");
            final Path expected = Path.of(System.getProperty("user.dir"));
            assertThat(AbstractServer.getWorkPath()).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getLogPath")
    class GetLogPathTest {
        @Test
        @DisplayName("creates logs directory under work path")
        void createsLogsDir(@TempDir final Path tempDir) throws Exception {
            System.setProperty("jrommanager.dir", tempDir.toString());
            final String logPath = AbstractServer.getLogPath();
            assertThat(logPath).isEqualTo(tempDir.resolve("logs").toString());
            assertThat(Files.exists(tempDir.resolve("logs"))).isTrue();
        }
    }

    @Nested
    @DisplayName("isStarted / isStopped / terminate")
    class LifecycleTest {
        @Test
        @DisplayName("isStopped returns true before initialization")
        void isStoppedBeforeInit() {
            assertThat(AbstractServer.isStopped()).isTrue();
        }

        @Test
        @DisplayName("isStarted returns false before initialization")
        void isStartedFalseBeforeInit() {
            assertThat(AbstractServer.isStarted()).isFalse();
        }

        @Test
        @DisplayName("terminate does not throw when server is null")
        void terminateWhenNull() throws Exception {
            AbstractServer.terminate();
            assertThat(AbstractServer.isStopped()).isTrue();
        }
    }
}