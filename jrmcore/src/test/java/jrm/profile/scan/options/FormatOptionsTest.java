package jrm.profile.scan.options;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import jrm.profile.scan.options.FormatOptions.Ext;

/**
 * Tests for {@link FormatOptions} and nested {@link FormatOptions.Ext} extension mapping.
 */
@DisplayName("FormatOptions tests")
class FormatOptionsTest {

    @Nested
    @DisplayName("Ext enum")
    class ExtEnum {

        /**
         * Verifies that the toString() method returns the literal file extension.
         */
        @Test
        @DisplayName("toString should return the literal file extension")
        void toStringShouldReturnLiteralFileExtension() {
            assertThat(Ext.DIR).hasToString("");
            assertThat(Ext.ZIP).hasToString(".zip");
            assertThat(Ext.SEVENZIP).hasToString(".7z");
            assertThat(Ext.FAKE).hasToString(".$$$");
        }

        /**
         * Verifies that the isDir() method returns true only for DIR.
         */
        @Test
        @DisplayName("isDir should return true only for DIR")
        void isDirShouldReturnTrueOnlyForDir() {
            assertThat(Ext.DIR.isDir()).isTrue();
            assertThat(Ext.ZIP.isDir()).isFalse();
            assertThat(Ext.SEVENZIP.isDir()).isFalse();
            assertThat(Ext.FAKE.isDir()).isFalse();
        }

        /**
         * Verifies that the allExcept() method returns all other extensions excluding self and DIR.
         */
        @ParameterizedTest(name = "allExcept() for {0} should exclude self and DIR")
        @EnumSource(Ext.class)
        @DisplayName("allExcept should exclude self and DIR")
        void allExceptShouldExcludeSelfAndDir(Ext ext) {
            final var others = ext.allExcept();
            final var expectedSize = ext == Ext.DIR ? Ext.values().length - 1 : Ext.values().length - 2;

            assertThat(others)
                .doesNotContain(ext)
                .doesNotContain(Ext.DIR)
                .hasSize(expectedSize);
        }

        /**
         * Verifies that DIR.allExcept() returns all other extensions excluding self and DIR.
         */
        @Test
        @DisplayName("DIR.allExcept should still exclude DIR and self")
        void dirAllExceptShouldStillExcludeDirAndSelf() {
            final var others = Ext.DIR.allExcept();

            assertThat(others).doesNotContain(Ext.DIR).containsExactlyInAnyOrder(Ext.ZIP, Ext.SEVENZIP, Ext.FAKE);
        }
    }

    /**
     * Tests for {@link FormatOptions#getExt()} mapping behavior.
     */
    @Nested
    @DisplayName("getExt()")
    class GetExt {

        /**
         * Verifies that ZIP-based formats map to the ZIP extension.
         */
        @Test
        @DisplayName("ZIP-based formats should map to ZIP extension")
        void zipBasedFormatsShouldMapToZipExtension() {
            assertThat(FormatOptions.ZIP.getExt()).isEqualTo(Ext.ZIP);
            assertThat(FormatOptions.ZIPE.getExt()).isEqualTo(Ext.ZIP);
            assertThat(FormatOptions.TZIP.getExt()).isEqualTo(Ext.ZIP);
        }

        /**
         * Verifies that SEVENZIP-based formats map to the SEVENZIP extension.
         */
        @Test
        @DisplayName("SEVENZIP should map to SEVENZIP extension")
        void sevenZipShouldMapToSevenZipExtension() {
            assertThat(FormatOptions.SEVENZIP.getExt()).isEqualTo(Ext.SEVENZIP);
        }

        /**
         * Verifies that DIR-based formats map to the DIR extension.
         */
        @Test
        @DisplayName("DIR should map to DIR extension")
        void dirShouldMapToDirExtension() {
            assertThat(FormatOptions.DIR.getExt()).isEqualTo(Ext.DIR);
        }

        /**
         * Verifies that FAKE-based formats map to the FAKE extension.
         */
        @Test
        @DisplayName("FAKE should map to FAKE extension")
        void fakeShouldMapToFakeExtension() {
            assertThat(FormatOptions.FAKE.getExt()).isEqualTo(Ext.FAKE);
        }
    }

    /**
     * Tests for {@link FormatOptions#getDesc()} contract behavior.
     */
    @Nested
    @DisplayName("Descriptor contract")
    class DescriptorContract {

        /**
         * Verifies that every FormatOptions constant exposes a non-blank description.
         */
        @ParameterizedTest(name = "{0} should expose non-blank desc")
        @EnumSource(FormatOptions.class)
        @DisplayName("every constant should expose a non-blank description")
        void everyConstantShouldExposeNonBlankDescription(FormatOptions option) {
            assertThat(option.getDesc()).isNotBlank();
        }
    }

    /**
     * Tests for {@link FormatOptions#allExcept()} contract behavior.
     */
    @Nested
    @DisplayName("allExcept()")
    class AllExcept {

        /**
         * Verifies that allExcept() returns all other format options excluding self and DIR.
         */
        @ParameterizedTest(name = "allExcept() for {0} should exclude self and DIR")
        @EnumSource(FormatOptions.class)
        @DisplayName("allExcept should exclude self and DIR format")
        void allExceptShouldExcludeSelfAndDir(FormatOptions option) {
            final var others = option.allExcept();
            final var expectedSize = option == FormatOptions.DIR ? FormatOptions.values().length - 1 : FormatOptions.values().length - 2;

            assertThat(others).doesNotContain(option).doesNotContain(FormatOptions.DIR).hasSize(expectedSize);
        }
    }

    /**
     * Verifies that FormatOptions exposes exactly six constants.
     */
    @Test
    @DisplayName("should expose exactly six format options")
    void shouldExposeExactlySixFormatOptions() {
        assertThat(FormatOptions.values()).hasSize(6);
    }
}
