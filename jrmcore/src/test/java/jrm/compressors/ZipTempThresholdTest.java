package jrm.compressors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests for {@link ZipTempThreshold} temp-file threshold enum.
 */
@DisplayName("ZipTempThreshold tests")
class ZipTempThresholdTest {

    @Nested
    @DisplayName("getThreshold()")
    class GetThreshold {

        @ParameterizedTest(name = "{0} should map to threshold {1}")
        @CsvSource({
            "_NEVER, -1",
            "_1MB, 1000000",
            "_2MB, 2000000",
            "_5MB, 5000000",
            "_10MB, 10000000",
            "_25MB, 25000000",
            "_50MB, 50000000",
            "_100MB, 100000000",
            "_250MB, 250000000",
            "_500MB, 500000000"
        })
        @DisplayName("should map each constant to its expected byte threshold")
        void shouldMapEachConstantToItsExpectedByteThreshold(ZipTempThreshold threshold, long expected) {
            assertThat(threshold.getThreshold()).isEqualTo(expected);
        }

        @Test
        @DisplayName("_NEVER should be the only negative threshold")
        void neverShouldBeTheOnlyNegativeThreshold() {
            for (final var threshold : ZipTempThreshold.values()) {
                if (threshold == ZipTempThreshold._NEVER) {
                    assertThat(threshold.getThreshold()).isNegative();
                } else {
                    assertThat(threshold.getThreshold()).isNotNegative();
                }
            }
        }

        @Test
        @DisplayName("non-NEVER thresholds should be strictly increasing")
        void nonNeverThresholdsShouldBeStrictlyIncreasing() {
            final var nonNever = new ZipTempThreshold[] {
                ZipTempThreshold._1MB, ZipTempThreshold._2MB, ZipTempThreshold._5MB, ZipTempThreshold._10MB,
                ZipTempThreshold._25MB, ZipTempThreshold._50MB, ZipTempThreshold._100MB, ZipTempThreshold._250MB,
                ZipTempThreshold._500MB
            };
            for (var i = 1; i < nonNever.length; i++) {
                assertThat(nonNever[i].getThreshold()).isGreaterThan(nonNever[i - 1].getThreshold());
            }
        }
    }

    @Nested
    @DisplayName("getDesc()")
    class GetDesc {

        @ParameterizedTest(name = "{0} should expose a non-blank desc")
        @EnumSource(ZipTempThreshold.class)
        @DisplayName("every constant should expose a non-blank description")
        void everyConstantShouldExposeNonBlankDescription(ZipTempThreshold threshold) {
            assertThat(threshold.getDesc()).isNotBlank();
        }
    }

    @Test
    @DisplayName("should expose exactly ten threshold options")
    void shouldExposeExactlyTenThresholdOptions() {
        assertThat(ZipTempThreshold.values()).hasSize(10);
    }
}
