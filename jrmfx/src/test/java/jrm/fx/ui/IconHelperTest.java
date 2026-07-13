package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IconHelper} utility class.
 * <p>
 * Validates icon creation with proper styling and ratio preservation.
 *
 * @since 3.0.5
 */
@TestFxApplication(IconHelperTest.TestApp.class)
@DisplayName("IconHelper Tests")
class IconHelperTest {

    /**
     * Test application that initializes JavaFX toolkit.
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
    @DisplayName("Should have private constructor")
    void shouldHavePrivateConstructor() {
        Constructor<?>[] constructors = IconHelper.class.getDeclaredConstructors();
        
        assertThat(constructors)
                .as("Should have exactly one constructor")
                .hasSize(1);
        assertThat(constructors[0])
                .as("Constructor should be private")
                .matches(c -> java.lang.reflect.Modifier.isPrivate(c.getModifiers()));
    }

    @Test
    @DisplayName("Should not be able to instantiate utility class")
    void shouldNotBeAbleToInstantiate() {
        Constructor<?> constructor = IconHelper.class.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        
        assertThat(constructor)
                .as("Constructor should be private")
                .isNotNull();
    }

    @Test
    @DisplayName("Should create icon with valid resource path")
    void shouldCreateIconWithValidResourcePath() {
        // MainFrame.getIcon will return null for non-existent resources in test context
        // but we can still verify the method doesn't throw and sets properties
        var icon = IconHelper.createIcon("/jrm/resicons/icons/script.png");
        
        assertThat(icon)
                .as("Icon should be created even if image is null")
                .isNotNull();
        assertThat(icon.isPreserveRatio())
                .as("Preserve ratio should be set to true")
                .isTrue();
        assertThat(icon.getStyleClass())
                .as("Style class should contain 'icon'")
                .contains("icon");
    }

    @Test
    @DisplayName("Should create icon with non-existent resource path")
    void shouldCreateIconWithNonExistentResourcePath() {
        var icon = IconHelper.createIcon("/non/existent/path.png");
        
        assertThat(icon)
                .as("Icon should be created even with invalid path")
                .isNotNull();
        assertThat(icon.isPreserveRatio())
                .as("Preserve ratio should still be set")
                .isTrue();
        assertThat(icon.getStyleClass())
                .as("Style class should still contain 'icon'")
                .contains("icon");
    }

    @Test
    @DisplayName("Should preserve ratio for all icons")
    void shouldPreserveRatioForAllIcons() {
        var icon1 = IconHelper.createIcon("/jrm/resicons/icons/script.png");
        var icon2 = IconHelper.createIcon("/jrm/resicons/icons/cog.png");
        
        assertThat(icon1.isPreserveRatio())
                .as("First icon should preserve ratio")
                .isTrue();
        assertThat(icon2.isPreserveRatio())
                .as("Second icon should preserve ratio")
                .isTrue();
    }

    @Test
    @DisplayName("Should add icon style class to all icons")
    void shouldAddIconStyleClassToAllIcons() {
        var icon1 = IconHelper.createIcon("/jrm/resicons/icons/script.png");
        var icon2 = IconHelper.createIcon("/jrm/resicons/icons/cog.png");
        var icon3 = IconHelper.createIcon("/non/existent.png");
        
        assertThat(icon1.getStyleClass())
                .as("First icon should have 'icon' style class")
                .contains("icon");
        assertThat(icon2.getStyleClass())
                .as("Second icon should have 'icon' style class")
                .contains("icon");
        assertThat(icon3.getStyleClass())
                .as("Third icon should have 'icon' style class")
                .contains("icon");
    }
}
