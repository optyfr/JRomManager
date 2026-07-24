package jrm.fx.ui.profile;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ProfileViewer} static reset-counter management.
 * <p>
 * The {@link ProfileViewer} constructor performs heavy FXML loading and starts a timer,
 * so these tests focus on the static {@code getResetCounter()} mechanism that is shared
 * across the scanner panel, filter selection helper, and the profile viewer itself to
 * coordinate deferred profile-view resets.
 *
 * @since 3.0.5
 */
@DisplayName("ProfileViewer Tests")
class ProfileViewerTest {

    /** Resets the shared counter to zero before each test. */
    @BeforeEach
    void resetCounter() {
        ProfileViewer.getResetCounter().set(0);
    }

    @Nested
    @DisplayName("getResetCounter()")
    class GetResetCounter {

        /**
         * Verifies that {@link ProfileViewer#getResetCounter()} returns a non-null
         * {@link AtomicInteger}.
         */
        @Test
        @DisplayName("should return a non-null AtomicInteger")
        void shouldReturnNonNullAtomicInteger() {
            assertThat(ProfileViewer.getResetCounter()).isNotNull();
        }

        /**
         * Verifies that repeated calls to {@code getResetCounter()} return the same
         * {@link AtomicInteger} instance.
         */
        @Test
        @DisplayName("should return the same counter instance across calls")
        void shouldReturnSameCounterInstanceAcrossCalls() {
            final var first = ProfileViewer.getResetCounter();
            final var second = ProfileViewer.getResetCounter();

            assertThat(second).isSameAs(first);
        }

        /**
         * Verifies that the counter starts at zero after a reset via {@link BeforeEach}.
         */
        @Test
        @DisplayName("should start at zero after reset")
        void shouldStartAtZeroAfterReset() {
            assertThat(ProfileViewer.getResetCounter().get()).isZero();
        }

        /**
         * Verifies that {@code incrementAndGet()} correctly increases the counter value.
         */
        @Test
        @DisplayName("should allow incrementing and reading the counter")
        void shouldAllowIncrementingAndReadingTheCounter() {
            final AtomicInteger counter = ProfileViewer.getResetCounter();

            counter.incrementAndGet();
            counter.incrementAndGet();

            assertThat(counter.get()).isEqualTo(2);
        }

        /**
         * Verifies that increments made through one reference are visible via another
         * call to {@code getResetCounter()}.
         */
        @Test
        @DisplayName("should reflect increments performed through the returned reference")
        void shouldReflectIncrementsPerformedThroughReturnedReference() {
            ProfileViewer.getResetCounter().incrementAndGet();

            assertThat(ProfileViewer.getResetCounter().get()).isEqualTo(1);
        }

        /**
         * Verifies that the counter supports concurrent modifications from multiple threads.
         *
         * @throws Exception if a thread is interrupted while joining
         */
        @Test
        @DisplayName("should support concurrent incrementAndGet")
        void shouldSupportConcurrentIncrementAndGet() throws Exception {
            final int threads = 16;
            final int perThread = 1000;
            final var runners = new Thread[threads];
            for (int t = 0; t < threads; t++) {
                runners[t] = new Thread(() -> {
                    for (int i = 0; i < perThread; i++) {
                        ProfileViewer.getResetCounter().incrementAndGet();
                    }
                });
            }
            for (final var r : runners) {
                r.start();
            }
            for (final var r : runners) {
                r.join();
            }

            assertThat(ProfileViewer.getResetCounter().get()).isEqualTo(threads * perThread);
        }

        /**
         * Verifies that the counter can be set to an arbitrary value via
         * {@link AtomicInteger#set(int)}.
         */
        @Test
        @DisplayName("should support set to arbitrary values")
        void shouldSupportSetToArbitraryValues() {
            ProfileViewer.getResetCounter().set(42);

            assertThat(ProfileViewer.getResetCounter().get()).isEqualTo(42);
        }

        /**
         * Verifies that {@code getAndSet} atomically returns the previous value and
         * stores the new value.
         */
        @Test
        @DisplayName("should support getAndSet atomically")
        void shouldSupportGetAndSetAtomically() {
            ProfileViewer.getResetCounter().set(5);
            final var previous = ProfileViewer.getResetCounter().getAndSet(10);

            assertThat(previous).isEqualTo(5);
            assertThat(ProfileViewer.getResetCounter().get()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("reset counter contract with callers")
    class ResetCounterContractWithCallers {

        /**
         * Mimics the {@code ProfileViewer.getResetCounter().incrementAndGet()} call pattern
         * used by {@code FilterSelectionHelper} and {@code ScannerPanelController} to request
         * a deferred profile-view reset.
         */
        @Test
        @DisplayName("increment pattern used by FilterSelectionHelper should raise the counter")
        void incrementPatternUsedByFilterSelectionHelperShouldRaiseTheCounter() {
            final IntSupplier readCount = () -> ProfileViewer.getResetCounter().get();

            ProfileViewer.getResetCounter().incrementAndGet();
            ProfileViewer.getResetCounter().incrementAndGet();

            assertThat(readCount.getAsInt()).isEqualTo(2);
        }

        @Test
        @DisplayName("reset-to-zero pattern used by ProfileViewer timer should clear pending requests")
        void resetToZeroPatternUsedByProfileViewerTimerShouldClearPendingRequests() {
            ProfileViewer.getResetCounter().incrementAndGet();
            ProfileViewer.getResetCounter().incrementAndGet();

            // The ProfileViewer timer task does: if counter > 0 then counter.set(0)
            if (ProfileViewer.getResetCounter().get() > 0) {
                ProfileViewer.getResetCounter().set(0);
            }

            assertThat(ProfileViewer.getResetCounter().get()).isZero();
        }

        @Test
        @DisplayName("timer should not reset when counter is already zero")
        void timerShouldNotResetWhenCounterIsAlreadyZero() {
            // counter is 0 from @BeforeEach
            final boolean shouldReset = ProfileViewer.getResetCounter().get() > 0;
            if (shouldReset) {
                ProfileViewer.getResetCounter().set(0);
            }

            assertThat(shouldReset).isFalse();
            assertThat(ProfileViewer.getResetCounter().get()).isZero();
        }
    }
}
