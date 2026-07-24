package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * TestFX tests for {@link JRMScene}.
 * <p>
 * Tests the custom Scene subclass that manages application-wide style sheets
 * and provides predefined size variants.
 *
 * @since 3.0.5
 */
@TestFxApplication(JRMSceneTest.TestApp.class)
@DisplayName("JRMScene Tests")
class JRMSceneTest {

    /**
     * Test application for JavaFX component tests.
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

    /** The root pane used to create scenes in tests. */
    private StackPane root;

    @BeforeEach
    void setUp() {
        root = new StackPane();
    }

    @Test
    @DisplayName("Should create JRMScene with root only")
    void shouldCreateJRMSceneWithRootOnly() {
        JRMScene scene = new JRMScene(root);

        assertThat(scene).as("Scene should be created").isNotNull();
        assertThat(scene.getRoot()).as("Root should be set").isSameAs(root);
    }

    @Test
    @DisplayName("Should create JRMScene with root and dimensions")
    void shouldCreateJRMSceneWithRootAndDimensions() {
        JRMScene scene = new JRMScene(root, 800, 600);

        assertThat(scene).as("Scene should be created").isNotNull();
        assertThat(scene.getWidth()).as("Width should be 800").isEqualTo(800);
        assertThat(scene.getHeight()).as("Height should be 600").isEqualTo(600);
    }

    @Test
    @DisplayName("Should create JRMScene with root, dimensions, and depth buffer")
    void shouldCreateJRMSceneWithRootDimensionsAndDepthBuffer() {
        JRMScene scene = new JRMScene(root, 800, 600, true);

        assertThat(scene).as("Scene should be created").isNotNull();
        assertThat(scene.getWidth()).as("Width should be 800").isEqualTo(800);
        assertThat(scene.getHeight()).as("Height should be 600").isEqualTo(600);
    }

    @Test
    @DisplayName("Should create JRMScene with root, dimensions, depth buffer, and anti-aliasing")
    void shouldCreateJRMSceneWithAllParameters() {
        JRMScene scene = new JRMScene(root, 800, 600, true, javafx.scene.SceneAntialiasing.BALANCED);

        assertThat(scene).as("Scene should be created").isNotNull();
        assertThat(scene.getWidth()).as("Width should be 800").isEqualTo(800);
        assertThat(scene.getHeight()).as("Height should be 600").isEqualTo(600);
    }

    @Test
    @DisplayName("Should have StyleSheet enum with all variants")
    void shouldHaveStyleSheetEnumWithAllVariants() {
        JRMScene.StyleSheet[] styles = JRMScene.StyleSheet.values();

        assertThat(styles)
            .as("Should have 8 style variants").hasSize(8)
            .as("Should contain SYSTEM").contains(JRMScene.StyleSheet.SYSTEM)
            .as("Should contain XXS").contains(JRMScene.StyleSheet.XXS)
            .as("Should contain XS").contains(JRMScene.StyleSheet.XS)
            .as("Should contain S").contains(JRMScene.StyleSheet.S)
            .as("Should contain M").contains(JRMScene.StyleSheet.M)
            .as("Should contain L").contains(JRMScene.StyleSheet.L)
            .as("Should contain XL").contains(JRMScene.StyleSheet.XL)
            .as("Should contain XXL").contains(JRMScene.StyleSheet.XXL);
    }

    @Test
    @DisplayName("Should have correct file names for StyleSheet variants")
    void shouldHaveCorrectFileNamesForStyleSheetVariants() {
        assertThat(JRMScene.StyleSheet.SYSTEM.getFileName()).as("SYSTEM should have null fileName").isNull();
        assertThat(JRMScene.StyleSheet.XXS.getFileName()).as("XXS should have XXS.css").isEqualTo("XXS.css");
        assertThat(JRMScene.StyleSheet.XS.getFileName()).as("XS should have XS.css").isEqualTo("XS.css");
        assertThat(JRMScene.StyleSheet.S.getFileName()).as("S should have S.css").isEqualTo("S.css");
        assertThat(JRMScene.StyleSheet.M.getFileName()).as("M should have M.css").isEqualTo("M.css");
        assertThat(JRMScene.StyleSheet.L.getFileName()).as("L should have L.css").isEqualTo("L.css");
        assertThat(JRMScene.StyleSheet.XL.getFileName()).as("XL should have XL.css").isEqualTo("XL.css");
        assertThat(JRMScene.StyleSheet.XXL.getFileName()).as("XXL should have XXL.css").isEqualTo("XXL.css");
    }

    @Test
    @DisplayName("Should have ScenePrefs enum with style_sheet preference")
    void shouldHaveScenePrefsEnumWithStyleSheetPreference() {
        JRMScene.ScenePrefs[] prefs = JRMScene.ScenePrefs.values();

        assertThat(prefs)
            .as("Should have 1 preference").hasSize(1)
            .as("Should contain style_sheet").contains(JRMScene.ScenePrefs.style_sheet);
    }

    @Test
    @DisplayName("Should have default value for style_sheet preference")
    void shouldHaveDefaultValueForStyleSheetPreference() {
        Object defaultValue = JRMScene.ScenePrefs.style_sheet.getDefault();

        assertThat(defaultValue).as("Default should be SYSTEM").isEqualTo(JRMScene.StyleSheet.SYSTEM);
    }

    @Test
    @DisplayName("Should get and set current style sheet")
    void shouldGetAndSetCurrentStyleSheet() {
        JRMScene.StyleSheet original = JRMScene.getSheet();

        JRMScene.setSheet(JRMScene.StyleSheet.M);
        assertThat(JRMScene.getSheet()).as("Sheet should be M").isEqualTo(JRMScene.StyleSheet.M);

        JRMScene.setSheet(JRMScene.StyleSheet.L);
        assertThat(JRMScene.getSheet()).as("Sheet should be L").isEqualTo(JRMScene.StyleSheet.L);

        // Restore original
        JRMScene.setSheet(original);
    }

    @Test
    @DisplayName("Should apply sheet to scene")
    void shouldApplySheetToScene() {
        JRMScene scene = new JRMScene(root);
        int initialSheetCount = scene.getStylesheets().size();

        scene.applySheet();

        // Should have at least the same or more stylesheets after applying
        assertThat(scene.getStylesheets()).as("Should have stylesheets after applying")
            .hasSizeGreaterThanOrEqualTo(initialSheetCount);
    }

    @Test
    @DisplayName("Should apply specific style sheet to scene")
    void shouldApplySpecificStyleSheetToScene() {
        JRMScene scene = new JRMScene(root);

        scene.applySheet(JRMScene.StyleSheet.M);

        assertThat(JRMScene.getSheet()).as("Current sheet should be M").isEqualTo(JRMScene.StyleSheet.M);
    }

    @Test
    @DisplayName("Should apply static sheet to any scene")
    void shouldApplyStaticSheetToAnyScene() {
        Scene scene = new Scene(root);
        JRMScene.StyleSheet original = JRMScene.getSheet();

        JRMScene.setSheet(JRMScene.StyleSheet.L);
        JRMScene.applySheet(scene);

        // Should have stylesheets applied
        assertThat(scene.getStylesheets()).as("Should have stylesheets applied").isNotEmpty();

        // Restore original
        JRMScene.setSheet(original);
    }

    @Test
    @DisplayName("Should not add stylesheet for SYSTEM style")
    void shouldNotAddStylesheetForSystemStyle() {
        Scene scene = new Scene(root);
        JRMScene.StyleSheet original = JRMScene.getSheet();

        JRMScene.setSheet(JRMScene.StyleSheet.SYSTEM);
        int beforeCount = scene.getStylesheets().size();
        JRMScene.applySheet(scene);
        int afterCount = scene.getStylesheets().size();

        assertThat(afterCount).as("Should not add stylesheet for SYSTEM style").isEqualTo(beforeCount);

        // Restore original
        JRMScene.setSheet(original);
    }

    @Test
    @DisplayName("Should restore original stylesheets when applying sheet")
    void shouldRestoreOriginalStylesheetsWhenApplyingSheet() {
        JRMScene scene = new JRMScene(root);
        
        // Add a custom stylesheet after construction
        String customSheet = "custom.css";
        scene.getStylesheets().add(customSheet);
        
        // Verify it was added
        assertThat(scene.getStylesheets()).as("Custom stylesheet should be added")
            .contains(customSheet);

        // Apply a new sheet - should clear and restore original (empty) stylesheets
        scene.applySheet(JRMScene.StyleSheet.M);

        // The custom stylesheet should be removed (only orgSheets restored)
        assertThat(scene.getStylesheets()).as("Should not contain custom stylesheet after applySheet")
            .doesNotContain(customSheet);
    }
}
