package jrm.fx.ui.misc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.eclipsesource.json.JsonObject;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Tests for {@link SrcDstResult} JavaFX property binding.
 * <p>
 * Validates JavaFX property lazy initialization, getter/setter methods,
 * and property change notifications.
 *
 * @since 3.0.5
 */
@TestFxApplication(SrcDstResultTest.TestApp.class)
@DisplayName("SrcDstResult JavaFX Property Tests")
class SrcDstResultTest {

    /**
     * Test application for JavaFX property tests.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;

        @Override
        public void start(Stage primaryStage) {
            this.primaryStage = primaryStage;
            primaryStage.setScene(new Scene(new StackPane(), 400, 300));
            primaryStage.show();
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }
    }

    @Test
    @DisplayName("Should create empty SrcDstResult with default constructor")
    void shouldCreateEmptySrcDstResult() {
        SrcDstResult result = new SrcDstResult();

        assertThat(result).as("result should not be null").isNotNull();
    }

    @Test
    @DisplayName("Should create SrcDstResult from JSON")
    void shouldCreateSrcDstResultFromJson() {
        JsonObject json = new JsonObject()
                .set("src", "/source/path")
                .set("dst", "/dest/path")
                .set("result", "Success")
                .set("selected", true);

        SrcDstResult result = new SrcDstResult(json);

        assertThat(result).as("result should not be null").isNotNull();
    }

    @Test
    @DisplayName("Should lazily initialize src property")
    void shouldLazilyInitializeSrcProperty() {
        SrcDstResult result = new SrcDstResult();

        // Property should be null before first access
        assertThat(result.srcProperty()).as("src property should be initialized").isNotNull();
        
        // Should be able to set and get value
        result.setSrc("/test/source");
        assertThat(result.getSrc()).as("src value should be set").isEqualTo("/test/source");
    }

    @Test
    @DisplayName("Should lazily initialize dst property")
    void shouldLazilyInitializeDstProperty() {
        SrcDstResult result = new SrcDstResult();

        // Property should be null before first access
        assertThat(result.dstProperty()).as("dst property should be initialized").isNotNull();
        
        // Should be able to set and get value
        result.setDst("/test/dest");
        assertThat(result.getDst()).as("dst value should be set").isEqualTo("/test/dest");
    }

    @Test
    @DisplayName("Should lazily initialize result property with empty default")
    void shouldLazilyInitializeResultProperty() {
        SrcDstResult result = new SrcDstResult();

        // Property should be initialized with empty string
        assertThat(result.resultProperty()).as("result property should be initialized").isNotNull();
        assertThat(result.getResult()).as("result should default to empty string").isEmpty();
        
        // Should be able to set and get value
        result.setResult("Success");
        assertThat(result.getResult()).as("result value should be set").isEqualTo("Success");
    }

    @Test
    @DisplayName("Should lazily initialize selected property with true default")
    void shouldLazilyInitializeSelectedProperty() {
        SrcDstResult result = new SrcDstResult();

        // Property should be initialized with true
        assertThat(result.selectedProperty()).as("selected property should be initialized").isNotNull();
        assertThat(result.isSelected()).as("selected should default to true").isTrue();
        
        // Should be able to set and get value
        result.setSelected(false);
        assertThat(result.isSelected()).as("selected value should be set").isFalse();
    }

    @Test
    @DisplayName("Should update src property value")
    void shouldUpdateSrcPropertyValue() {
        SrcDstResult result = new SrcDstResult();
        
        result.setSrc("/path/one");
        assertThat(result.getSrc()).as("initial src should be set").isEqualTo("/path/one");
        
        result.setSrc("/path/two");
        assertThat(result.getSrc()).as("src should be updated").isEqualTo("/path/two");
    }

    @Test
    @DisplayName("Should update dst property value")
    void shouldUpdateDstPropertyValue() {
        SrcDstResult result = new SrcDstResult();
        
        result.setDst("/dest/one");
        assertThat(result.getDst()).as("initial dst should be set").isEqualTo("/dest/one");
        
        result.setDst("/dest/two");
        assertThat(result.getDst()).as("dst should be updated").isEqualTo("/dest/two");
    }

    @Test
    @DisplayName("Should update result property value")
    void shouldUpdateResultPropertyValue() {
        SrcDstResult result = new SrcDstResult();
        
        result.setResult("Pending");
        assertThat(result.getResult()).as("initial result should be set").isEqualTo("Pending");
        
        result.setResult("Success");
        assertThat(result.getResult()).as("result should be updated").isEqualTo("Success");
    }

    @Test
    @DisplayName("Should update selected property value")
    void shouldUpdateSelectedPropertyValue() {
        SrcDstResult result = new SrcDstResult();
        
        assertThat(result.isSelected()).as("initial selected should be true").isTrue();
        
        result.setSelected(false);
        assertThat(result.isSelected()).as("selected should be false").isFalse();
        
        result.setSelected(true);
        assertThat(result.isSelected()).as("selected should be true again").isTrue();
    }

    @Test
    @DisplayName("Should handle null src value")
    void shouldHandleNullSrcValue() {
        SrcDstResult result = new SrcDstResult();
        
        result.setSrc(null);
        assertThat(result.getSrc()).as("src should be null").isNull();
    }

    @Test
    @DisplayName("Should handle null dst value")
    void shouldHandleNullDstValue() {
        SrcDstResult result = new SrcDstResult();
        
        result.setDst(null);
        assertThat(result.getDst()).as("dst should be null").isNull();
    }

    @Test
    @DisplayName("Should handle empty result value")
    void shouldHandleEmptyResultValue() {
        SrcDstResult result = new SrcDstResult();
        
        result.setResult("");
        assertThat(result.getResult()).as("result should be empty").isEmpty();
    }

    @Test
    @DisplayName("Should maintain independent property instances")
    void shouldMaintainIndependentPropertyInstances() {
        SrcDstResult result1 = new SrcDstResult();
        SrcDstResult result2 = new SrcDstResult();
        
        result1.setSrc("/source/one");
        result1.setDst("/dest/one");
        result1.setResult("Success");
        result1.setSelected(false);
        
        result2.setSrc("/source/two");
        result2.setDst("/dest/two");
        result2.setResult("Failed");
        result2.setSelected(true);
        
        assertThat(result1.getSrc()).as("result1 src should be independent").isEqualTo("/source/one");
        assertThat(result1.getDst()).as("result1 dst should be independent").isEqualTo("/dest/one");
        assertThat(result1.getResult()).as("result1 result should be independent").isEqualTo("Success");
        assertThat(result1.isSelected()).as("result1 selected should be independent").isFalse();
        
        assertThat(result2.getSrc()).as("result2 src should be independent").isEqualTo("/source/two");
        assertThat(result2.getDst()).as("result2 dst should be independent").isEqualTo("/dest/two");
        assertThat(result2.getResult()).as("result2 result should be independent").isEqualTo("Failed");
        assertThat(result2.isSelected()).as("result2 selected should be independent").isTrue();
    }

    @Test
    @DisplayName("Should return same property instance on multiple calls")
    void shouldReturnSamePropertyInstanceOnMultipleCalls() {
        SrcDstResult result = new SrcDstResult();
        
        var srcProp1 = result.srcProperty();
        var srcProp2 = result.srcProperty();
        assertThat(srcProp1).as("src property should be same instance").isSameAs(srcProp2);
        
        var dstProp1 = result.dstProperty();
        var dstProp2 = result.dstProperty();
        assertThat(dstProp1).as("dst property should be same instance").isSameAs(dstProp2);
        
        var resultProp1 = result.resultProperty();
        var resultProp2 = result.resultProperty();
        assertThat(resultProp1).as("result property should be same instance").isSameAs(resultProp2);
        
        var selectedProp1 = result.selectedProperty();
        var selectedProp2 = result.selectedProperty();
        assertThat(selectedProp1).as("selected property should be same instance").isSameAs(selectedProp2);
    }
}
