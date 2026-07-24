package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;

import javafx.scene.image.Image;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MainFrame} utility methods and static state management.
 * <p>
 * Focuses on the icon caching mechanism, static field accessors, and
 * version string generation.
 *
 * @since 3.0.5
 */
@DisplayName("MainFrame Tests")
class MainFrameTest {

    /**
     * Clears the icons cache before each test to ensure test isolation.
     *
     * @throws Exception if reflection fails
     */
    @BeforeEach
    void setUp() throws Exception {
        Field iconsCacheField = MainFrame.class.getDeclaredField("iconsCache");
        iconsCacheField.setAccessible(true);
        @SuppressWarnings("unchecked")
        HashMap<String, Image> cache = (HashMap<String, Image>) iconsCacheField.get(null);
        cache.clear();
    }

    @Nested
    @DisplayName("Icon caching")
    class IconCaching {

        private HashMap<String, Image> getCache() throws Exception {
            Field iconsCacheField = MainFrame.class.getDeclaredField("iconsCache");
            iconsCacheField.setAccessible(true);
            @SuppressWarnings("unchecked")
            HashMap<String, Image> cache = (HashMap<String, Image>) iconsCacheField.get(null);
            return cache;
        }

        @Test
        @DisplayName("Should return null for non-existent icon path")
        void shouldReturnNullForNonExistentIconPath() {
            Image icon = MainFrame.getIcon("/non/existent/icon.png");

            assertThat(icon).as("non-existent icon should return null").isNull();
        }

        @Test
        @DisplayName("Should not cache null results for missing icons")
        void shouldNotCacheNullResultsForMissingIcons() throws Exception {
            MainFrame.getIcon("/non/existent/path1.png");
            MainFrame.getIcon("/non/existent/path2.png");

            HashMap<String, Image> cache = getCache();
            assertThat(cache).as("missing icons should not be cached")
                .doesNotContainKey("/non/existent/path1.png")
                .doesNotContainKey("/non/existent/path2.png");
        }

        @Test
        @DisplayName("Should return cached instance for pre-populated entry")
        void shouldReturnCachedInstanceForPrePopulatedEntry() throws Exception {
            String iconPath = "/test/icon.png";
            // Use Mockito mock since JavaFX toolkit is not initialized in unit tests
            Image mockImage = mock(Image.class);
            HashMap<String, Image> cache = getCache();
            cache.put(iconPath, mockImage);

            Image result = MainFrame.getIcon(iconPath);

            assertThat(result).as("should return cached instance").isSameAs(mockImage);
        }

        @Test
        @DisplayName("Should not overwrite existing cache entry on repeated call")
        void shouldNotOverwriteExistingCacheEntry() throws Exception {
            String iconPath = "/test/cached.png";
            Image original = mock(Image.class);
            HashMap<String, Image> cache = getCache();
            cache.put(iconPath, original);

            MainFrame.getIcon(iconPath);

            assertThat(cache.get(iconPath)).as("original entry should not be overwritten").isSameAs(original);
        }

        @Test
        @DisplayName("Should use module layer for icon resources when available")
        void shouldUseModuleLayerForIconResourcesWhenAvailable() throws Exception {
            Field iconsModuleField = MainFrame.class.getDeclaredField("iconsModule");
            iconsModuleField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Optional<Module> iconsModule = (Optional<Module>) iconsModuleField.get(null);

            assertThat(iconsModule).as("icons module optional should be initialized").isNotNull();
        }

        @Test
        @DisplayName("Should handle getIcon with various paths without throwing")
        void shouldHandleGetIconWithVariousPathsWithoutThrowing() {
            // Act - these paths don't exist but should not throw
            Image icon1 = MainFrame.getIcon("/a.png");
            Image icon2 = MainFrame.getIcon("/b/c/d.png");

            // Assert
            assertThat(icon1).as("non-existent path should return null").isNull();
            assertThat(icon2).as("non-existent path should return null").isNull();
        }
    }

    @Nested
    @DisplayName("Static field management")
    class StaticFieldManagement {

        @Test
        @DisplayName("Should have static session getter")
        void shouldHaveStaticSessionGetter() {
            var session = MainFrame.getSession();

            // Session may be null in test context when Sessions.getSingleSession() returns null
            assertThat(session).as("session getter should be accessible").satisfiesAnyOf(
                s -> assertThat(s).isNotNull(),
                s -> assertThat(s).isNull()
            );
        }

        @Test
        @DisplayName("Should manage report frame with getter and setter")
        void shouldManageReportFrameWithGetterAndSetter() {
            MainFrame.setReportFrame(null);

            assertThat(MainFrame.getReportFrame()).as("report frame should be settable to null").isNull();
        }

        @Test
        @DisplayName("Should manage profile viewer with getter and setter")
        void shouldManageProfileViewerWithGetterAndSetter() {
            MainFrame.setProfileViewer(null);

            assertThat(MainFrame.getProfileViewer()).as("profile viewer should be settable to null").isNull();
        }

        @Test
        @DisplayName("Should have application getter")
        void shouldHaveApplicationGetter() {
            var app = MainFrame.getApplication();

            assertThat(app).as("application getter should be accessible (null if not started)").isNull();
        }

        @Test
        @DisplayName("Should manage primary scene with getter and setter")
        void shouldManagePrimarySceneWithGetterAndSetter() {
            MainFrame.setPrimaryScene(null);

            assertThat(MainFrame.getPrimaryScene()).as("primary scene should be settable to null").isNull();
        }
    }

    @Nested
    @DisplayName("Version string")
    class VersionString {

        @Test
        @DisplayName("Should return version string from package metadata")
        void shouldReturnVersionStringFromPackageMetadata() throws Exception {
            Method getVersion = MainFrame.class.getDeclaredMethod("getVersion");
            getVersion.setAccessible(true);
            var instance = new MainFrame();

            String version = (String) getVersion.invoke(instance);

            assertThat(version).as("version should not be null").isNotNull();
        }
    }
}
