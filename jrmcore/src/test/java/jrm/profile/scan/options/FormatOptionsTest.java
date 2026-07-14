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

        @Test
        @DisplayName("toString should return the literal file extension")
        void toStringShouldReturnLiteralFileExtension() {
            assertThat(Ext.DIR).hasToString("");
            assertThat(Ext.ZIP).hasToString(".zip");
            assertThat(Ext.SEVENZIP).hasToString(".7z");
            assertThat(Ext.FAKE).hasToString(".$$$");
        }

        @Test
        @DisplayName("isDir should return true only for DIR")
        void isDirShouldReturnTrueOnlyForDir() {
            assertThat(Ext.DIR.isDir()).isTrue();
            assertThat(Ext.ZIP.isDir()).isFalse();
            assertThat(Ext.SEVENZIP.isDir()).isFalse();
            assertThat(Ext.FAKE.isDir()).isFalse();
        }

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

        @Test
        @DisplayName("DIR.allExcept should still exclude DIR and self")
        void dirAllExceptShouldStillExcludeDirAndSelf() {
            final var others = Ext.DIR.allExcept();

            assertThat(others).doesNotContain(Ext.DIR).containsExactlyInAnyOrder(Ext.ZIP, Ext.SEVENZIP, Ext.FAKE);
        }
    }

    @Nested
    @DisplayName("getExt()")
    class GetExt {

        @Test
        @DisplayName("ZIP-based formats should map to ZIP extension")
        void zipBasedFormatsShouldMapToZipExtension() {
            assertThat(FormatOptions.ZIP.getExt()).isEqualTo(Ext.ZIP);
            assertThat(FormatOptions.ZIPE.getExt()).isEqualTo(Ext.ZIP);
            assertThat(FormatOptions.TZIP.getExt()).isEqualTo(Ext.ZIP);
        }

        @Test
        @DisplayName("SEVENZIP should map to SEVENZIP extension")
        void sevenZipShouldMapToSevenZipExtension() {
            assertThat(FormatOptions.SEVENZIP.getExt()).isEqualTo(Ext.SEVENZIP);
        }

        @Test
        @DisplayName("DIR should map to DIR extension")
        void dirShouldMapToDirExtension() {
            assertThat(FormatOptions.DIR.getExt()).isEqualTo(Ext.DIR);
        }

        @Test
        @DisplayName("FAKE should map to FAKE extension")
        void fakeShouldMapToFakeExtension() {
            assertThat(FormatOptions.FAKE.getExt()).isEqualTo(Ext.FAKE);
        }
    }

    @Nested
    @DisplayName("Descriptor contract")
    class DescriptorContract {

        @ParameterizedTest(name = "{0} should expose non-blank desc")
        @EnumSource(FormatOptions.class)
        @DisplayName("every constant should expose a non-blank description")
        void everyConstantShouldExposeNonBlankDescription(FormatOptions option) {
            assertThat(option.getDesc()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("allExcept()")
    class AllExcept {

        @ParameterizedTest(name = "allExcept() for {0} should exclude self and DIR")
        @EnumSource(FormatOptions.class)
        @DisplayName("allExcept should exclude self and DIR format")
        void allExceptShouldExcludeSelfAndDir(FormatOptions option) {
            final var others = option.allExcept();
            final var expectedSize = option == FormatOptions.DIR ? FormatOptions.values().length - 1 : FormatOptions.values().length - 2;

            assertThat(others).doesNotContain(option).doesNotContain(FormatOptions.DIR).hasSize(expectedSize);
        }
    }

    @Test
    @DisplayName("should expose exactly six format options")
    void shouldExposeExactlySixFormatOptions() {
        assertThat(FormatOptions.values()).hasSize(6);
    }
}
