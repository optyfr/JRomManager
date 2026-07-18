package jrm.fullserver.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SSLReload}.
 */
@DisplayName("SSLReload")
class SSLReloadTest {

    @Nested
    @DisplayName("getDelayUntilTomorrowMidNight")
    class DelayTest {
        @Test
        @DisplayName("delay is within (0, 24h]")
        void delayWithinBounds() {
            final SslContextFactory sslContext = mock(SslContextFactory.class);
            final SSLReload reload = SSLReload.getInstance(sslContext);
            final long delay = reload.getDelayUntilTomorrowMidNight();
            assertThat(delay).isPositive().isLessThanOrEqualTo(Duration.ofHours(24).toMillis());
        }
    }

    @Nested
    @DisplayName("getInstance")
    class GetInstanceTest {
        @Test
        @DisplayName("returns a non-null instance")
        void returnsInstance() {
            final SslContextFactory sslContext = mock(SslContextFactory.class);
            final SSLReload reload = SSLReload.getInstance(sslContext);
            assertThat(reload).isNotNull();
        }
    }

    @Nested
    @DisplayName("start")
    class StartTest {
        @Test
        @DisplayName("start schedules without throwing")
        void startDoesNotThrow() {
            final SslContextFactory sslContext = mock(SslContextFactory.class);
            final SSLReload reload = SSLReload.getInstance(sslContext);
            assertThatCode(reload::start).doesNotThrowAnyException();
        }
    }
}