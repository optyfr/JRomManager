package jrm.fx.ui.profile.manager;

import static org.assertj.core.api.Assertions.assertThat;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jrm.profile.manager.Dir;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TestFX tests for {@link DirItem}.
 * <p>
 * Tests the tree item that represents a directory in the profile manager,
 * including directory tree building and reload functionality.
 *
 * @since 3.0.5
 */
@TestFxApplication(DirItemTest.TestApp.class)
@DisplayName("DirItem Tests")
class DirItemTest {

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

    @TempDir
    Path tempDir;

    private File testDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create a test directory structure
        testDir = tempDir.toFile();
        
        // Create subdirectories
        Files.createDirectory(tempDir.resolve("subdir1"));
        Files.createDirectory(tempDir.resolve("subdir2"));
        Files.createDirectory(tempDir.resolve("subdir3"));
        
        // Create nested subdirectories
        Files.createDirectory(tempDir.resolve("subdir1/nested1"));
        Files.createDirectory(tempDir.resolve("subdir1/nested2"));
        Files.createDirectory(tempDir.resolve("subdir2/nested3"));
    }

    @Test
    @DisplayName("Should create DirItem from File")
    void shouldCreateDirItemFromFile() {
        DirItem item = new DirItem(testDir);

        assertThat(item).as("DirItem should be created").isNotNull();
        assertThat(item.getValue()).as("Dir should be set").isNotNull();
        assertThat(item.getValue().getFile()).as("File should match").isEqualTo(testDir);
    }

    @Test
    @DisplayName("Should expand DirItem by default")
    void shouldExpandDirItemByDefault() {
        DirItem item = new DirItem(testDir);

        assertThat(item.isExpanded()).as("DirItem should be expanded by default").isTrue();
    }

    @Test
    @DisplayName("Should have graphic icon")
    void shouldHaveGraphicIcon() {
        DirItem item = new DirItem(testDir);

        assertThat(item.getGraphic()).as("Graphic should be set").isNotNull();
    }

    @Test
    @DisplayName("Should build directory tree with subdirectories")
    void shouldBuildDirectoryTreeWithSubdirectories() {
        DirItem item = new DirItem(testDir);

        assertThat(item.getChildren()).as("Should have children").isNotEmpty();
        assertThat(item.getChildren()).as("Should have 3 subdirectories").hasSize(3);
    }

    @Test
    @DisplayName("Should build nested directory tree")
    void shouldBuildNestedDirectoryTree() {
        DirItem item = new DirItem(testDir);

        // Find subdir1
        DirItem subdir1 = item.getChildren().stream()
                .filter(child -> child.getValue().getFile().getName().equals("subdir1"))
                .map(DirItem.class::cast)
                .findFirst()
                .orElse(null);

        assertThat(subdir1).as("subdir1 should exist").isNotNull();
        assertThat(subdir1.getChildren()).as("subdir1 should have 2 nested directories").hasSize(2);
    }

    @Test
    @DisplayName("Should reload directory tree")
    void shouldReloadDirectoryTree() throws IOException {
        DirItem item = new DirItem(testDir);
        
        int initialChildCount = item.getChildren().size();
        assertThat(initialChildCount).as("Should have 3 initial subdirectories").isEqualTo(3);

        // Add a new subdirectory
        Files.createDirectory(tempDir.resolve("subdir4"));

        // Reload the tree
        item.reload();

        assertThat(item.getChildren()).as("Should have 4 subdirectories after reload").hasSize(4);
    }

    @Test
    @DisplayName("Should remain expanded after reload")
    void shouldRemainExpandedAfterReload() {
        DirItem item = new DirItem(testDir);

        assertThat(item.isExpanded()).as("Should be expanded initially").isTrue();

        item.reload();

        assertThat(item.isExpanded()).as("Should remain expanded after reload").isTrue();
    }

    @Test
    @DisplayName("Should handle empty directory")
    void shouldHandleEmptyDirectory() throws IOException {
        Path emptyDir = tempDir.resolve("empty");
        Files.createDirectory(emptyDir);

        DirItem item = new DirItem(emptyDir.toFile());

        assertThat(item.getChildren()).as("Empty directory should have no children").isEmpty();
    }

    @Test
    @DisplayName("Should only include directories in tree")
    void shouldOnlyIncludeDirectoriesInTree() throws IOException {
        // Create a file (not a directory)
        Files.createFile(tempDir.resolve("file.txt"));

        DirItem item = new DirItem(testDir);

        // Should only have 3 subdirectories, not the file
        assertThat(item.getChildren()).as("Should only include directories, not files").hasSize(3);
        
        // Verify all children are directories
        assertThat(item.getChildren())
                .as("All children should be directories")
                .allMatch(child -> child.getValue().getFile().isDirectory());
    }

    @Test
    @DisplayName("Should handle directory with no subdirectories")
    void shouldHandleDirectoryWithNoSubdirectories() throws IOException {
        Path leafDir = tempDir.resolve("leaf");
        Files.createDirectory(leafDir);
        Files.createFile(leafDir.resolve("file.txt"));

        DirItem item = new DirItem(leafDir.toFile());

        assertThat(item.getChildren()).as("Leaf directory should have no children").isEmpty();
        assertThat(item.isLeaf()).as("Should be a leaf node").isTrue();
    }

    @Test
    @DisplayName("Should clear children on reload")
    void shouldClearChildrenOnReload() throws IOException {
        DirItem item = new DirItem(testDir);
        
        assertThat(item.getChildren()).as("Should have children initially").isNotEmpty();

        // Delete all subdirectories
        Files.walk(tempDir)
                .filter(Files::isDirectory)
                .filter(path -> !path.equals(tempDir))
                .sorted((a, b) -> b.getNameCount() - a.getNameCount()) // Delete deepest first
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException _) {
                        // Ignore
                    }
                });

        item.reload();

        assertThat(item.getChildren()).as("Should have no children after deleting all subdirectories").isEmpty();
    }

    @Test
    @DisplayName("Should preserve Dir value after reload")
    void shouldPreserveDirValueAfterReload() {
        DirItem item = new DirItem(testDir);
        Dir originalDir = item.getValue();

        item.reload();

        assertThat(item.getValue()).as("Dir value should be preserved after reload").isEqualTo(originalDir);
    }
}
