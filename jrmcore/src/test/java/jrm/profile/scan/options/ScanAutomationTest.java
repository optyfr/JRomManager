package jrm.profile.scan.options;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests for {@link ScanAutomation} pipeline flags and descriptor contract.
 */
@DisplayName("ScanAutomation tests")
class ScanAutomationTest {

    @Nested
    @DisplayName("hasReport()")
    class HasReport {

        @ParameterizedTest(name = "{0} should report true for report")
        @EnumSource(names = { "SCAN_REPORT", "SCAN_REPORT_FIX", "SCAN_REPORT_FIX_SCAN" })
        @DisplayName("should return true for report-producing automations")
        void shouldReturnTrueForReportProducingAutomations(ScanAutomation automation) {
            assertThat(automation.hasReport()).isTrue();
        }

        @ParameterizedTest(name = "{0} should report false for no report")
        @EnumSource(names = { "SCAN", "SCAN_FIX" })
        @DisplayName("should return false for non-report automations")
        void shouldReturnFalseForNonReportAutomations(ScanAutomation automation) {
            assertThat(automation.hasReport()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasFix()")
    class HasFix {

        @ParameterizedTest(name = "{0} should fix true")
        @EnumSource(names = { "SCAN_FIX", "SCAN_REPORT_FIX", "SCAN_REPORT_FIX_SCAN" })
        @DisplayName("should return true for fix-performing automations")
        void shouldReturnTrueForFixPerformingAutomations(ScanAutomation automation) {
            assertThat(automation.hasFix()).isTrue();
        }

        @ParameterizedTest(name = "{0} should fix false")
        @EnumSource(names = { "SCAN", "SCAN_REPORT" })
        @DisplayName("should return false for non-fix automations")
        void shouldReturnFalseForNonFixAutomations(ScanAutomation automation) {
            assertThat(automation.hasFix()).isFalse();
        }
    }

    @Nested
    @DisplayName("hasScanAgain()")
    class HasScanAgain {

        @Test
        @DisplayName("SCAN_REPORT_FIX_SCAN should require a re-scan")
        void scanReportFixScanShouldRequireRescan() {
            assertThat(ScanAutomation.SCAN_REPORT_FIX_SCAN.hasScanAgain()).isTrue();
        }

        @ParameterizedTest(name = "{0} should not require re-scan")
        @EnumSource(names = { "SCAN", "SCAN_REPORT", "SCAN_REPORT_FIX", "SCAN_FIX" })
        @DisplayName("should return false for non-rescan automations")
        void shouldReturnFalseForNonRescanAutomations(ScanAutomation automation) {
            assertThat(automation.hasScanAgain()).isFalse();
        }
    }

    @Nested
    @DisplayName("Descriptor contract")
    class DescriptorContract {

        @ParameterizedTest(name = "{0} should expose non-blank desc")
        @EnumSource(ScanAutomation.class)
        @DisplayName("every constant should expose a non-blank description")
        void everyConstantShouldExposeNonBlankDescription(ScanAutomation automation) {
            assertThat(automation.getDesc()).isNotBlank();
        }

        @Test
        @DisplayName("should expose exactly five automation levels")
        void shouldExposeExactlyFiveAutomationLevels() {
            assertThat(ScanAutomation.values()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("value composition")
    class ValueComposition {

        @Test
        @DisplayName("SCAN should be scan-only (no report, no fix, no rescan)")
        void scanShouldBeScanOnly() {
            final var automation = ScanAutomation.SCAN;
            assertThat(automation.hasReport()).isFalse();
            assertThat(automation.hasFix()).isFalse();
            assertThat(automation.hasScanAgain()).isFalse();
        }

        @Test
        @DisplayName("SCAN_REPORT_FIX_SCAN should perform all phases")
        void scanReportFixScanShouldPerformAllPhases() {
            final var automation = ScanAutomation.SCAN_REPORT_FIX_SCAN;
            assertThat(automation.hasReport()).isTrue();
            assertThat(automation.hasFix()).isTrue();
            assertThat(automation.hasScanAgain()).isTrue();
        }

        @Test
        @DisplayName("SCAN_FIX should fix without producing report")
        void scanFixShouldFixWithoutReport() {
            final var automation = ScanAutomation.SCAN_FIX;
            assertThat(automation.hasReport()).isFalse();
            assertThat(automation.hasFix()).isTrue();
        }
    }
}
