package jrm.profile.scan.options;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests for {@link MergeOptions} merge-mode semantics and descriptor contract.
 */
@DisplayName("MergeOptions tests")
class MergeOptionsTest {

    @Nested
    @DisplayName("isMerge()")
    class IsMerge {

        @ParameterizedTest(name = "{0} should be a merge mode")
        @EnumSource(names = { "MERGE", "FULLMERGE" })
        @DisplayName("should return true for MERGE and FULLMERGE")
        void shouldReturnTrueForMergeAndFullMerge(MergeOptions option) {
            assertThat(option.isMerge()).isTrue();
        }

        @ParameterizedTest(name = "{0} should not be a merge mode")
        @EnumSource(names = { "SUPERFULLNOMERGE", "FULLNOMERGE", "NOMERGE", "SPLIT" })
        @DisplayName("should return false for non-merge modes")
        void shouldReturnFalseForNonMergeModes(MergeOptions option) {
            assertThat(option.isMerge()).isFalse();
        }
    }

    @Nested
    @DisplayName("Descriptor contract")
    class DescriptorContract {

        @ParameterizedTest(name = "{0} should expose non-blank desc")
        @EnumSource(MergeOptions.class)
        @DisplayName("every constant should expose a non-blank description")
        void everyConstantShouldExposeNonBlankDescription(MergeOptions option) {
            assertThat(option.getDesc()).isNotBlank();
        }

        @Test
        @DisplayName("should expose exactly six merge options")
        void shouldExposeExactlySixMergeOptions() {
            assertThat(MergeOptions.values()).hasSize(6);
        }
    }

    @Test
    @DisplayName("should provide all expected constant names")
    void shouldProvideAllExpectedConstantNames() {
        assertThat(MergeOptions.values())
            .containsExactlyInAnyOrder(
                MergeOptions.FULLMERGE,
                MergeOptions.MERGE,
                MergeOptions.SUPERFULLNOMERGE,
                MergeOptions.FULLNOMERGE,
                MergeOptions.NOMERGE,
                MergeOptions.SPLIT);
    }
}
