package jrm.fx.ui;

import static org.mockito.Mockito.mockStatic;

import org.mockito.MockedStatic;

import jrm.security.Sessions;

/**
 * Shared {@link Sessions} static mock across all controller test classes.
 * <p>
 * The mock is created once (lazily) on the JUnit thread via {@code @BeforeAll}
 * and never closed, avoiding FX-thread vs JUnit-thread deregistration conflicts.
 * Each test class reconfigures the mock in its {@code TestApp.start()} method
 * using {@code SharedMockSession.mock.when(...)}.
 *
 * @since 3.0.5
 */
final class SharedMockSession {

    /**
     * The shared static mock instance.
     */
    static MockedStatic<Sessions> mock;

    private SharedMockSession() {
        // Utility class
    }

    /**
     * Returns the shared mock, creating it if necessary.
     * <p>
     * Thread-safe via {@code synchronized}.
     *
     * @return the shared {@link MockedStatic} for {@link Sessions}
     */
    static synchronized MockedStatic<Sessions> getOrCreate() {
        if (mock == null) {
            mock = mockStatic(Sessions.class);
        }
        return mock;
    }
}
