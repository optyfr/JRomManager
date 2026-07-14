package jrm.compressors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests for {@link ZipLevel} compression-level enum mapping.
 */
@DisplayName("ZipLevel tests")
class ZipLevelTest {

    @Nested
    @DisplayName("getLevel()")
    class GetLevel {

        @ParameterizedTest(name = "{0} should map to level {1}")
        @CsvSource({ "DEFAULT, -1", "STORE, 0", "FASTEST, 1", "FAST, 3", "NORMAL, 5", "MAXIMUM, 7", "ULTRA, 9" })
        @DisplayName("should map each constant to its expected integer level")
        void shouldMapEachConstantToItsExpectedIntegerLevel(ZipLevel level, int expected) {
            assertThat(level.getLevel()).isEqualTo(expected);
        }

        @Test
        @DisplayName("levels should be monotonically non-decreasing from STORE onward")
        void levelsShouldBeMonotonicallyNonDecreasingFromStoreOnward() {
            final var ordered = new ZipLevel[] {
                ZipLevel.STORE, ZipLevel.FASTEST, ZipLevel.FAST, ZipLevel.NORMAL, ZipLevel.MAXIMUM, ZipLevel.ULTRA
            };
            for (var i = 1; i < ordered.length; i++) {
                assertThat(ordered[i].getLevel()).isGreaterThanOrEqualTo(ordered[i - 1].getLevel());
            }
        }
    }

    @Nested
    @DisplayName("getName()")
    class GetName {

        @ParameterizedTest(name = "{0} should expose a non-blank name")
        @EnumSource(ZipLevel.class)
        @DisplayName("every constant should expose a non-blank name")
        void everyConstantShouldExposeNonBlankName(ZipLevel level) {
            assertThat(level.getName()).isNotBlank();
        }
    }

    @Test
    @DisplayName("should expose exactly seven compression levels")
    void shouldExposeExactlySevenCompressionLevels() {
        assertThat(ZipLevel.values()).hasSize(7);
    }

    @Test
    @DisplayName("should provide all expected constant names")
    void shouldProvideAllExpectedConstantNames() {
        assertThat(ZipLevel.values())
            .containsExactlyInAnyOrder(
                ZipLevel.DEFAULT,
                ZipLevel.STORE,
                ZipLevel.FASTEST,
                ZipLevel.FAST,
                ZipLevel.NORMAL,
                ZipLevel.MAXIMUM,
                ZipLevel.ULTRA);
    }

    @Test
    @DisplayName("DEFAULT should have a distinct sentinel level below STORE")
    void defaultShouldHaveDistinctSentinelLevelBelowStore() {
        assertThat(ZipLevel.DEFAULT.getLevel()).isLessThan(ZipLevel.STORE.getLevel());
    }

    @Test
    @DisplayName("ULTRA should have the highest level")
    void ultraShouldHaveHighestLevel() {
        for (final var level : ZipLevel.values()) {
            assertThat(ZipLevel.ULTRA.getLevel()).isGreaterThanOrEqualTo(level.getLevel());
        }
    }
}
