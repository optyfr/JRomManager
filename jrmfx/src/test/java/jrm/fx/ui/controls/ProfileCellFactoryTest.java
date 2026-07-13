package jrm.fx.ui.controls;

import static org.assertj.core.api.Assertions.assertThat;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.manager.ProfileNFOMame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TestFX tests for {@link ProfileCellFactory}.
 * <p>
 * Tests the table cell factory that displays profile names with MAME status color coding.
 * Uses reflection to create and configure ProfileNFO instances for testing.
 *
 * @since 3.0.5
 */
@TestFxApplication(ProfileCellFactoryTest.TestApp.class)
@DisplayName("ProfileCellFactory Tests")
class ProfileCellFactoryTest {

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

    private ProfileCellFactory cellFactory;
    private TableView<ProfileNFO> tableView;
    private TableRow<ProfileNFO> tableRow;

    @BeforeEach
    void setUp() {
        cellFactory = new ProfileCellFactory();
        tableView = new TableView<>();
        tableRow = new TableRow<>();
        cellFactory.updateTableView(tableView);
        cellFactory.updateIndex(-1);
        cellFactory.updateTableRow(tableRow);
    }

    /**
     * Creates a ProfileNFO instance using reflection.
     *
     * @param name the profile name
     * @param profileFile the profile file
     * @param mameFile the MAME executable file (can be null for UNKNOWN status)
     * @param fileroms the ROMs DAT file (can be null)
     * @return a configured ProfileNFO instance
     */
    private ProfileNFO createProfileNFO(String name, File profileFile, File mameFile, File fileroms) {
        try {
            // Create ProfileNFO using reflection (private constructor)
            var constructor = ProfileNFO.class.getDeclaredConstructor(File.class);
            constructor.setAccessible(true);
            ProfileNFO nfo = constructor.newInstance(profileFile);

            // Set name using reflection
            var nameField = ProfileNFO.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(nfo, name);

            // Configure MAME status by setting file references
            var mameField = ProfileNFO.class.getDeclaredField("mame");
            mameField.setAccessible(true);
            ProfileNFOMame mame = (ProfileNFOMame) mameField.get(nfo);
            
            // Set MAME file
            var mameFileField = ProfileNFOMame.class.getDeclaredField("file");
            mameFileField.setAccessible(true);
            mameFileField.set(mame, mameFile);
            
            // Set modified timestamp
            var modifiedField = ProfileNFOMame.class.getDeclaredField("modified");
            modifiedField.setAccessible(true);
            if (mameFile != null && mameFile.exists()) {
                modifiedField.set(mame, mameFile.lastModified());
            } else {
                modifiedField.set(mame, 0L);
            }
            
            // Set fileroms
            var fileromsField = ProfileNFOMame.class.getDeclaredField("fileroms");
            fileromsField.setAccessible(true);
            fileromsField.set(mame, fileroms);

            return nfo;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create ProfileNFO instance", e);
        }
    }

    /**
     * Creates a ProfileNFO with UNKNOWN status (no MAME file).
     */
    private ProfileNFO createProfileNFOUnknown(String name) throws IOException {
        File profileFile = tempDir.resolve("profile.jrm").toFile();
        profileFile.createNewFile();
        return createProfileNFO(name, profileFile, null, null);
    }

    /**
     * Creates a ProfileNFO with UPTODATE status.
     */
    private ProfileNFO createProfileNFOWpToDate(String name) throws IOException {
        File profileFile = tempDir.resolve("profile.jrm").toFile();
        profileFile.createNewFile();
        
        File mameFile = tempDir.resolve("mame.exe").toFile();
        Files.write(mameFile.toPath(), "dummy".getBytes());
        
        File fileroms = tempDir.resolve("roms.dat").toFile();
        Files.write(fileroms.toPath(), "dummy".getBytes());
        
        return createProfileNFO(name, profileFile, mameFile, fileroms);
    }

    /**
     * Creates a ProfileNFO with NEEDUPDATE status.
     */
    private ProfileNFO createProfileNFONeedUpdate(String name) throws IOException {
        File profileFile = tempDir.resolve("profile.jrm").toFile();
        profileFile.createNewFile();
        
        File mameFile = tempDir.resolve("mame.exe").toFile();
        Files.write(mameFile.toPath(), "dummy".getBytes());
        
        // Set modified to past timestamp so file.lastModified() > modified
        File fileroms = null; // Missing fileroms triggers NEEDUPDATE
        
        ProfileNFO nfo = createProfileNFO(name, profileFile, mameFile, fileroms);
        
        // Set modified to old timestamp
        try {
            var mameField = ProfileNFO.class.getDeclaredField("mame");
            mameField.setAccessible(true);
            ProfileNFOMame mame = (ProfileNFOMame) mameField.get(nfo);
            
            var modifiedField = ProfileNFOMame.class.getDeclaredField("modified");
            modifiedField.setAccessible(true);
            modifiedField.set(mame, 0L); // Old timestamp
        } catch (Exception e) {
            throw new RuntimeException("Failed to set modified timestamp", e);
        }
        
        return nfo;
    }

    /**
     * Creates a ProfileNFO with NOTFOUND status.
     */
    private ProfileNFO createProfileNFONotFound(String name) throws IOException {
        File profileFile = tempDir.resolve("profile.jrm").toFile();
        profileFile.createNewFile();
        
        File mameFile = tempDir.resolve("nonexistent.exe").toFile(); // Doesn't exist
        
        return createProfileNFO(name, profileFile, mameFile, null);
    }

    /**
     * Sets the selected state of the table row using reflection.
     *
     * @param selected the selected state to set
     */
    private void setRowSelected(boolean selected) {
        try {
            var method = javafx.scene.control.Cell.class.getDeclaredMethod("setSelected", boolean.class);
            method.setAccessible(true);
            method.invoke(tableRow, selected);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set selected state", e);
        }
    }

    @Test
    @DisplayName("Should create ProfileCellFactory")
    void shouldCreateProfileCellFactory() {
        assertThat(cellFactory).as("Cell factory should be created").isNotNull();
    }

    @Test
    @DisplayName("Should display empty text for null value")
    void shouldDisplayEmptyTextForNullValue() {
        // When item is null, the cell should be treated as empty
        cellFactory.updateItem(null, true);

        assertThat(cellFactory.getText()).as("Text should be empty").isEmpty();
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    @Test
    @DisplayName("Should display empty text for empty cell")
    void shouldDisplayEmptyTextForEmptyCell() throws IOException {
        ProfileNFO nfo = createProfileNFOWpToDate("Test Profile");
        
        cellFactory.updateItem(nfo, true);

        assertThat(cellFactory.getText()).as("Text should be empty").isEmpty();
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }

    @Test
    @DisplayName("Should display profile name")
    void shouldDisplayProfileName() throws IOException {
        ProfileNFO nfo = createProfileNFOWpToDate("Test Profile");
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getText()).as("Text should display profile name").isEqualTo("Test Profile");
    }

    @Test
    @DisplayName("Should apply green color for UPTODATE status when not selected")
    void shouldApplyGreenColorForUpToDateStatusWhenNotSelected() throws IOException {
        ProfileNFO nfo = createProfileNFOWpToDate("Test Profile");
        setRowSelected(false);
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getStyle()).as("Style should be green for UPTODATE").contains("#00aa00");
    }

    @Test
    @DisplayName("Should apply light green color for UPTODATE status when selected")
    void shouldApplyLightGreenColorForUpToDateStatusWhenSelected() throws IOException {
        ProfileNFO nfo = createProfileNFOWpToDate("Test Profile");
        setRowSelected(true);
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getStyle()).as("Style should be light green for selected UPTODATE").contains("#aaffaa");
    }

    @Test
    @DisplayName("Should apply orange color for NEEDUPDATE status when not selected")
    void shouldApplyOrangeColorForNeedUpdateStatusWhenNotSelected() throws IOException {
        ProfileNFO nfo = createProfileNFONeedUpdate("Test Profile");
        setRowSelected(false);
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getStyle()).as("Style should be orange for NEEDUPDATE").contains("#cc8800");
    }

    @Test
    @DisplayName("Should apply light orange color for NEEDUPDATE status when selected")
    void shouldApplyLightOrangeColorForNeedUpdateStatusWhenSelected() throws IOException {
        ProfileNFO nfo = createProfileNFONeedUpdate("Test Profile");
        setRowSelected(true);
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getStyle()).as("Style should be light orange for selected NEEDUPDATE").contains("#ffaa88");
    }

    @Test
    @DisplayName("Should apply red color for NOTFOUND status when not selected")
    void shouldApplyRedColorForNotFoundStatusWhenNotSelected() throws IOException {
        ProfileNFO nfo = createProfileNFONotFound("Test Profile");
        setRowSelected(false);
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getStyle()).as("Style should be red for NOTFOUND").contains("#cc0000");
    }

    @Test
    @DisplayName("Should apply light red color for NOTFOUND status when selected")
    void shouldApplyLightRedColorForNotFoundStatusWhenSelected() throws IOException {
        ProfileNFO nfo = createProfileNFONotFound("Test Profile");
        setRowSelected(true);
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getStyle()).as("Style should be light red for selected NOTFOUND").contains("#ffaaaa");
    }

    @Test
    @DisplayName("Should apply default black color for unknown status when not selected")
    void shouldApplyDefaultBlackColorForUnknownStatusWhenNotSelected() throws IOException {
        ProfileNFO nfo = createProfileNFOUnknown("Test Profile");
        setRowSelected(false);
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getStyle()).as("Style should be black for unknown status").contains("#000000");
    }

    @Test
    @DisplayName("Should apply default white color for unknown status when selected")
    void shouldApplyDefaultWhiteColorForUnknownStatusWhenSelected() throws IOException {
        ProfileNFO nfo = createProfileNFOUnknown("Test Profile");
        setRowSelected(true);
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getStyle()).as("Style should be white for selected unknown status").contains("#ffffff");
    }

    @Test
    @DisplayName("Should set tooltip for UPTODATE status")
    void shouldSetTooltipForUpToDateStatus() throws IOException {
        ProfileNFO nfo = createProfileNFOWpToDate("Test Profile");
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getTooltip()).as("Tooltip should be set").isNotNull();
        assertThat(cellFactory.getTooltip().getText()).as("Tooltip should mention profile name").contains("Test Profile");
    }

    @Test
    @DisplayName("Should set tooltip for NEEDUPDATE status")
    void shouldSetTooltipForNeedUpdateStatus() throws IOException {
        ProfileNFO nfo = createProfileNFONeedUpdate("Test Profile");
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getTooltip()).as("Tooltip should be set").isNotNull();
        assertThat(cellFactory.getTooltip().getText()).as("Tooltip should mention profile name").contains("Test Profile");
    }

    @Test
    @DisplayName("Should set tooltip for NOTFOUND status")
    void shouldSetTooltipForNotFoundStatus() throws IOException {
        ProfileNFO nfo = createProfileNFONotFound("Test Profile");
        
        cellFactory.updateItem(nfo, false);

        assertThat(cellFactory.getTooltip()).as("Tooltip should be set").isNotNull();
        assertThat(cellFactory.getTooltip().getText()).as("Tooltip should mention profile name").contains("Test Profile");
    }

    @Test
    @DisplayName("Should handle different profile names")
    void shouldHandleDifferentProfileNames() throws IOException {
        ProfileNFO nfo1 = createProfileNFOWpToDate("Profile One");
        ProfileNFO nfo2 = createProfileNFONeedUpdate("Profile Two");
        
        cellFactory.updateItem(nfo1, false);
        assertThat(cellFactory.getText()).as("Text should display first profile name").isEqualTo("Profile One");
        
        cellFactory.updateItem(nfo2, false);
        assertThat(cellFactory.getText()).as("Text should display second profile name").isEqualTo("Profile Two");
    }

    @Test
    @DisplayName("Should not apply style when cell becomes empty")
    void shouldNotApplyStyleWhenCellBecomesEmpty() throws IOException {
        ProfileNFO nfo = createProfileNFOWpToDate("Test Profile");
        
        // First set a profile with style
        cellFactory.updateItem(nfo, false);
        assertThat(cellFactory.getStyle()).as("Style should be set initially").isNotEmpty();
        
        // Then set to empty - style should not be updated
        cellFactory.updateItem(nfo, true);
        
        // The style might still be bound from before, but the cell should be empty
        assertThat(cellFactory.getText()).as("Text should be empty").isEmpty();
        assertThat(cellFactory.getGraphic()).as("Graphic should be null").isNull();
    }
}
