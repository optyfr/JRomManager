package jrm.fx.ui.progress;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ProgressTask.PData} and {@link ProgressTask.PData.PB}.
 * Tests progress data snapshot classes used for UI state management.
 */
@DisplayName("ProgressTask.PData Unit Tests")
class ProgressTaskPDataTest {

    @Nested
    @DisplayName("PB (Progress Bar) inner class tests")
    class PBTests {

        @Test
        @DisplayName("Should create PB with default values")
        void shouldCreatePBWithDefaultValues() {
            ProgressTask.PData.PB pb = new ProgressTask.PData.PB();
            
            assertThat(pb.visibility).isFalse();
            assertThat(pb.stringPainted).isFalse();
            assertThat(pb.indeterminate).isFalse();
            assertThat(pb.max).isEqualTo(100);
            assertThat(pb.val).isZero();
            assertThat(pb.perc).isEqualTo(0.0);
            assertThat(pb.msg).isNull();
            assertThat(pb.timeleft).isNull();
        }

        @Test
        @DisplayName("Should create PB as copy of another PB")
        void shouldCreatePBAsCopyOfAnotherPB() {
            ProgressTask.PData.PB original = new ProgressTask.PData.PB();
            original.visibility = true;
            original.stringPainted = true;
            original.indeterminate = true;
            original.max = 200;
            original.val = 50;
            original.perc = 25.0;
            original.msg = "Processing...";
            original.timeleft = "00:05:00";
            
            ProgressTask.PData.PB copy = new ProgressTask.PData.PB(original);
            
            assertThat(copy.visibility).isTrue();
            assertThat(copy.stringPainted).isTrue();
            assertThat(copy.indeterminate).isTrue();
            assertThat(copy.max).isEqualTo(200);
            assertThat(copy.val).isEqualTo(50);
            assertThat(copy.perc).isEqualTo(25.0);
            assertThat(copy.msg).isEqualTo("Processing...");
            assertThat(copy.timeleft).isEqualTo("00:05:00");
        }

        @Test
        @DisplayName("Should modify PB properties independently")
        void shouldModifyPBPropertiesIndependently() {
            ProgressTask.PData.PB pb1 = new ProgressTask.PData.PB();
            ProgressTask.PData.PB pb2 = new ProgressTask.PData.PB(pb1);
            
            pb1.val = 75;
            pb1.perc = 75.0;
            
            assertThat(pb1.val).isEqualTo(75);
            assertThat(pb1.perc).isEqualTo(75.0);
            assertThat(pb2.val).isZero();
            assertThat(pb2.perc).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("PData class tests")
    class PDataTests {

        @Test
        @DisplayName("Should create PData with default values")
        void shouldCreatePDataWithDefaultValues() {
            ProgressTask.PData data = new ProgressTask.PData();
            
            assertThat(data.threadCnt).isEqualTo(1);
            assertThat(data.multipleSubInfos).isFalse();
            assertThat(data.infos).hasSize(1);
            assertThat(data.infos[0]).isNull();
            assertThat(data.subinfos).hasSize(1);
            assertThat(data.subinfos[0]).isNull();
            assertThat(data.pb1).isNotNull();
            assertThat(data.pb2).isNotNull();
            assertThat(data.pb3).isNotNull();
        }

        @Test
        @DisplayName("Should create PData as copy of another PData")
        void shouldCreatePDataAsCopyOfAnotherPData() {
            ProgressTask.PData original = new ProgressTask.PData();
            original.threadCnt = 4;
            original.multipleSubInfos = true;
            original.infos = new String[]{"Info 1", "Info 2", "Info 3", "Info 4"};
            original.subinfos = new String[]{"Sub 1", "Sub 2", "Sub 3", "Sub 4"};
            original.pb1.val = 50;
            original.pb2.val = 75;
            original.pb3.val = 100;
            
            ProgressTask.PData copy = new ProgressTask.PData(original);
            
            assertThat(copy.threadCnt).isEqualTo(4);
            assertThat(copy.multipleSubInfos).isTrue();
            assertThat(copy.infos).containsExactly("Info 1", "Info 2", "Info 3", "Info 4");
            assertThat(copy.subinfos).containsExactly("Sub 1", "Sub 2", "Sub 3", "Sub 4");
            assertThat(copy.pb1.val).isEqualTo(50);
            assertThat(copy.pb2.val).isEqualTo(75);
            assertThat(copy.pb3.val).isEqualTo(100);
        }

        @Test
        @DisplayName("Should have independent progress bars")
        void shouldHaveIndependentProgressBars() {
            ProgressTask.PData data = new ProgressTask.PData();
            
            data.pb1.val = 10;
            data.pb2.val = 20;
            data.pb3.val = 30;
            
            assertThat(data.pb1.val).isEqualTo(10);
            assertThat(data.pb2.val).isEqualTo(20);
            assertThat(data.pb3.val).isEqualTo(30);
        }

        @Test
        @DisplayName("Should copy progress bars independently")
        void shouldCopyProgressBarsIndependently() {
            ProgressTask.PData original = new ProgressTask.PData();
            original.pb1.val = 10;
            original.pb2.val = 20;
            original.pb3.val = 30;
            
            ProgressTask.PData copy = new ProgressTask.PData(original);
            
            // Modify original
            original.pb1.val = 99;
            
            // Copy should remain unchanged
            assertThat(copy.pb1.val).isEqualTo(10);
            assertThat(copy.pb2.val).isEqualTo(20);
            assertThat(copy.pb3.val).isEqualTo(30);
        }

        @Test
        @DisplayName("Should copy info arrays independently")
        void shouldCopyInfoArraysIndependently() {
            ProgressTask.PData original = new ProgressTask.PData();
            original.infos = new String[]{"Original 1", "Original 2"};
            original.subinfos = new String[]{"Sub 1", "Sub 2"};
            
            ProgressTask.PData copy = new ProgressTask.PData(original);
            
            // Modify original arrays
            original.infos[0] = "Modified";
            original.subinfos[0] = "Modified Sub";
            
            // Copy should remain unchanged
            assertThat(copy.infos[0]).isEqualTo("Original 1");
            assertThat(copy.subinfos[0]).isEqualTo("Sub 1");
        }

        @Test
        @DisplayName("Should handle different thread counts")
        void shouldHandleDifferentThreadCounts() {
            ProgressTask.PData data = new ProgressTask.PData();
            
            data.threadCnt = 8;
            data.infos = new String[8];
            
            assertThat(data.threadCnt).isEqualTo(8);
            assertThat(data.infos).hasSize(8);
        }

        @Test
        @DisplayName("Should handle multiple sub-infos flag")
        void shouldHandleMultipleSubInfosFlag() {
            ProgressTask.PData data = new ProgressTask.PData();
            
            data.multipleSubInfos = true;
            data.subinfos = new String[]{"Sub 1", "Sub 2", "Sub 3"};
            
            assertThat(data.multipleSubInfos).isTrue();
            assertThat(data.subinfos).hasSize(3);
        }
    }
}
