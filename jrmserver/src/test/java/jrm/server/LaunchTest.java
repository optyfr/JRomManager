package jrm.server;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/** * Test for the Launcher class. */    
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LauncherTest {

    /** This test should be first to ensure the server is not initialized for other tests. */
    @Test
    @Order(1)
    @DisplayName("Try to terminate before initialization")
    void tryTerminateFirst() {
        assertDoesNotThrow(Server::terminate);
    }

    /** This test should be second to ensure the server is not initialized for other tests. */
    @Test
    @Order(2)
    @DisplayName("Initialize the server with valid arguments")
    void initialize() {
        assertDoesNotThrow(() -> {
            Server.parseArgs(
                    "--client=" + Paths.get(System.getProperty("JRomManager.rootPath")).resolve("WebClient").resolve("war"),
                    "--debug");
            Server.initialize();
        });
    }

    /** This test should be third to ensure the server behaves correctly when initialized multiple times. */
    @Test
    @Order(3)
    @DisplayName("Try to initialize again")
    void initializeAgain() {
        assertDoesNotThrow(Server::initialize);
    }

    /** This test should be last to ensure the server is still running for other tests. */
    @Test
    @Order(4)
    @DisplayName("Terminate the server")
    void finallyTerminate() {
        assertDoesNotThrow(Server::terminate);
    }
}