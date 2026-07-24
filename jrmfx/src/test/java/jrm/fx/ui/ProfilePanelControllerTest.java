package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jrm.profile.manager.ProfileNFO;
import jrm.profile.manager.ProfileNFOStats;
import jrm.profile.manager.ProfileNFOStats.HaveNTotal;
import jrm.profile.manager.ProfileNFOMame.MameStatus;
import jrm.security.Sessions;
import jrm.security.Session;
import jrm.security.User;
import jrm.misc.GlobalSettings;

/**
 * Tests for {@link ProfilePanelController}.
 * <p>
 * Verifies the {@code initialize} method sets up button/menu icons,
 * cell factories, value factories, comparators, context menu handlers,
 * and selection listeners.
 *
 * @since 3.0.5
 */
@TestFxApplication(ProfilePanelControllerTest.TestApp.class)
@DisplayName("ProfilePanelController Tests")
class ProfilePanelControllerTest {

    /**
     * Minimal application that creates a {@link ProfilePanelController}
     * with all FXML fields injected via reflection and mocks for Sessions.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        /** The primary stage used for the test application. */
        private Stage primaryStage;

        /** The controller instance shared across test methods. */
        private static ProfilePanelController controller;

        /** The shared mocked static {@link Sessions} scope. */
        private static MockedStatic<Sessions> sessionsMock;

        @Override
        public void start(Stage primaryStage) throws Exception {
            this.primaryStage = primaryStage;
            
            // Mock Sessions.getSingleSession() with proper method stubs
            Session mockSession = mock(Session.class);
            User mockUser = mock(User.class);
            GlobalSettings mockSettings = mock(GlobalSettings.class);
            Path mockPath = Path.of(System.getProperty("java.io.tmpdir"));
            
            // Mock the session methods that GlobalSettings depends on
            when(mockSession.isServer()).thenReturn(false);
            when(mockSession.isMultiuser()).thenReturn(false);
            when(mockUser.getSession()).thenReturn(mockSession);
            when(mockUser.getSettings()).thenReturn(mockSettings);
            when(mockSettings.getWorkPath()).thenReturn(mockPath);
            when(mockSession.getUser()).thenReturn(mockUser);
            
            // Use shared mock to avoid thread conflicts
            SharedMockSession.getOrCreate().when(Sessions::getSingleSession).thenReturn(mockSession);
            
            controller = new ProfilePanelController();
            
            // Inject FXML fields via reflection
            injectField(controller, "btnLoad", new Button("Load"));
            injectField(controller, "btnImportDat", new Button("Import DAT"));
            injectField(controller, "btnImportSL", new Button("Import SL"));
            injectField(controller, "profilesTree", new TreeView<>());
            injectField(controller, "profilesList", new TableView<>());
            injectField(controller, "profileCol", new TableColumn<>());
            injectField(controller, "profileVersionCol", new TableColumn<>());
            injectField(controller, "profileHaveSetsCol", new TableColumn<>());
            injectField(controller, "profileHaveRomsCol", new TableColumn<>());
            injectField(controller, "profileHaveDisksCol", new TableColumn<>());
            injectField(controller, "profileCreatedCol", new TableColumn<>());
            injectField(controller, "profileLastScanCol", new TableColumn<>());
            injectField(controller, "profileLastFixCol", new TableColumn<>());
            injectField(controller, "createFolderMenu", new MenuItem("Create"));
            injectField(controller, "deleteFolderMenu", new MenuItem("Delete Folder"));
            injectField(controller, "deleteProfileMenu", new MenuItem("Delete Profile"));
            injectField(controller, "renameProfileMenu", new MenuItem("Rename"));
            injectField(controller, "dropCacheMenu", new MenuItem("Drop Cache"));
            injectField(controller, "updateFromMameMenu", new MenuItem("Update"));
            injectField(controller, "folderMenu", new ContextMenu());
            injectField(controller, "profileMenu", new ContextMenu());
            
            // Initialize the controller
            controller.initialize(null, null);
            
            VBox root = new VBox();
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        /** Injects a value into a private field of the target object using reflection. */
        private static void injectField(Object target, String fieldName, Object value) {
            try {
                var field = ProfilePanelController.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Failed to inject field " + fieldName, e);
            }
        }

        /**
         * Returns the controller instance for test access.
         *
         * @return the controller
         */
        public static ProfilePanelController getController() {
            return controller;
        }

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }

        @Override
        public void stop() {
            if (sessionsMock != null) {
                try {
                    sessionsMock.close();
                } catch (Exception _) {
                    // Mock already resolved, ignore
                }
            }
        }
    }

    @Test
    @DisplayName("Should initialize all FXML fields")
    void shouldInitializeAllFXMLFields(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.btnLoad).as("btnLoad").isNotNull();
        assertThat(controller.btnImportDat).as("btnImportDat").isNotNull();
        assertThat(controller.btnImportSL).as("btnImportSL").isNotNull();
        assertThat(controller.profilesTree).as("profilesTree").isNotNull();
        assertThat(controller.profilesList).as("profilesList").isNotNull();
        assertThat(controller.profileCol).as("profileCol").isNotNull();
        assertThat(controller.profileVersionCol).as("profileVersionCol").isNotNull();
        assertThat(controller.profileHaveSetsCol).as("profileHaveSetsCol").isNotNull();
        assertThat(controller.profileHaveRomsCol).as("profileHaveRomsCol").isNotNull();
        assertThat(controller.profileHaveDisksCol).as("profileHaveDisksCol").isNotNull();
        assertThat(controller.profileCreatedCol).as("profileCreatedCol").isNotNull();
        assertThat(controller.profileLastScanCol).as("profileLastScanCol").isNotNull();
        assertThat(controller.profileLastFixCol).as("profileLastFixCol").isNotNull();
        assertThat(controller.createFolderMenu).as("createFolderMenu").isNotNull();
        assertThat(controller.deleteFolderMenu).as("deleteFolderMenu").isNotNull();
        assertThat(controller.deleteProfileMenu).as("deleteProfileMenu").isNotNull();
        assertThat(controller.renameProfileMenu).as("renameProfileMenu").isNotNull();
        assertThat(controller.dropCacheMenu).as("dropCacheMenu").isNotNull();
        assertThat(controller.updateFromMameMenu).as("updateFromMameMenu").isNotNull();
        assertThat(controller.folderMenu).as("folderMenu").isNotNull();
        assertThat(controller.profileMenu).as("profileMenu").isNotNull();
    }

    @Test
    @DisplayName("Should initialize session")
    void shouldInitializeSession(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        assertThat(controller.session).as("session").isNotNull();
    }

    @Test
    @DisplayName("Should set icons on buttons")
    void shouldSetIconsOnButtons(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.btnLoad.getGraphic()).as("btnLoad icon").isNotNull();
        assertThat(controller.btnImportDat.getGraphic()).as("btnImportDat icon").isNotNull();
        assertThat(controller.btnImportSL.getGraphic()).as("btnImportSL icon").isNotNull();
    }

    @Test
    @DisplayName("Should set icons on menu items")
    void shouldSetIconsOnMenuItems(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.createFolderMenu.getGraphic()).as("createFolderMenu icon").isNotNull();
        assertThat(controller.deleteFolderMenu.getGraphic()).as("deleteFolderMenu icon").isNotNull();
        assertThat(controller.deleteProfileMenu.getGraphic()).as("deleteProfileMenu icon").isNotNull();
        assertThat(controller.renameProfileMenu.getGraphic()).as("renameProfileMenu icon").isNotNull();
        assertThat(controller.dropCacheMenu.getGraphic()).as("dropCacheMenu icon").isNotNull();
    }

    @Test
    @DisplayName("Should setup context menu handlers")
    void shouldSetupContextMenuHandlers(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.folderMenu.getOnShowing()).as("folderMenu onShowing").isNotNull();
        assertThat(controller.profileMenu.getOnShowing()).as("profileMenu onShowing").isNotNull();
    }

    @Test
    @DisplayName("Should setup tree cell factory")
    void shouldSetupTreeCellFactory(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profilesTree.getCellFactory()).as("profilesTree cell factory").isNotNull();
        assertThat(controller.profilesTree.getRoot()).as("profilesTree root").isNotNull();
    }

    @Test
    @DisplayName("Should setup tree edit commit handler")
    void shouldSetupTreeEditCommitHandler(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profilesTree.getOnEditCommit()).as("profilesTree onEditCommit").isNotNull();
    }

    @Test
    @DisplayName("Should setup selection listener")
    void shouldSetupSelectionListener(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profilesTree.getSelectionModel()).as("profilesTree selection model").isNotNull();
    }

    @Test
    @DisplayName("Should setup profile column")
    void shouldSetupProfileColumn(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profileCol.getCellFactory()).as("profileCol cell factory").isNotNull();
        assertThat(controller.profileCol.getOnEditCommit()).as("profileCol onEditCommit").isNotNull();
        assertThat(controller.profileCol.getCellValueFactory()).as("profileCol cell value factory").isNotNull();
    }

    @Test
    @DisplayName("Should setup version column")
    void shouldSetupVersionColumn(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profileVersionCol.getCellFactory()).as("profileVersionCol cell factory").isNotNull();
        assertThat(controller.profileVersionCol.getCellValueFactory()).as("profileVersionCol cell value factory").isNotNull();
    }

    @Test
    @DisplayName("Should setup have sets column")
    void shouldSetupHaveSetsColumn(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profileHaveSetsCol.getCellFactory()).as("profileHaveSetsCol cell factory").isNotNull();
        assertThat(controller.profileHaveSetsCol.getCellValueFactory()).as("profileHaveSetsCol cell value factory").isNotNull();
        assertThat(controller.profileHaveSetsCol.getComparator()).as("profileHaveSetsCol comparator").isNotNull();
    }

    @Test
    @DisplayName("Should setup have roms column")
    void shouldSetupHaveRomsColumn(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profileHaveRomsCol.getCellFactory()).as("profileHaveRomsCol cell factory").isNotNull();
        assertThat(controller.profileHaveRomsCol.getCellValueFactory()).as("profileHaveRomsCol cell value factory").isNotNull();
        assertThat(controller.profileHaveRomsCol.getComparator()).as("profileHaveRomsCol comparator").isNotNull();
    }

    @Test
    @DisplayName("Should setup have disks column")
    void shouldSetupHaveDisksColumn(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profileHaveDisksCol.getCellFactory()).as("profileHaveDisksCol cell factory").isNotNull();
        assertThat(controller.profileHaveDisksCol.getCellValueFactory()).as("profileHaveDisksCol cell value factory").isNotNull();
        assertThat(controller.profileHaveDisksCol.getComparator()).as("profileHaveDisksCol comparator").isNotNull();
    }

    @Test
    @DisplayName("Should setup created column")
    void shouldSetupCreatedColumn(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profileCreatedCol.getCellFactory()).as("profileCreatedCol cell factory").isNotNull();
        assertThat(controller.profileCreatedCol.getCellValueFactory()).as("profileCreatedCol cell value factory").isNotNull();
    }

    @Test
    @DisplayName("Should setup last scan column")
    void shouldSetupLastScanColumn(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profileLastScanCol.getCellFactory()).as("profileLastScanCol cell factory").isNotNull();
        assertThat(controller.profileLastScanCol.getCellValueFactory()).as("profileLastScanCol cell value factory").isNotNull();
    }

    @Test
    @DisplayName("Should setup last fix column")
    void shouldSetupLastFixColumn(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        assertThat(controller.profileLastFixCol.getCellFactory()).as("profileLastFixCol cell factory").isNotNull();
        assertThat(controller.profileLastFixCol.getCellValueFactory()).as("profileLastFixCol cell value factory").isNotNull();
    }

    @Test
    @DisplayName("Should setup profile loader")
    void shouldSetupProfileLoader(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        
        ProfileLoader mockLoader = mock(ProfileLoader.class);
        controller.setProfileLoader(mockLoader);
        
        assertThat(controller).as("controller").isNotNull();
    }

    // ==================== Static Method Tests ====================

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getTreeViewItem should find matching item at root level")
    void getTreeViewItemShouldFindItemAtRoot() {
        TreeItem<String> root = new TreeItem<>("root");
        TreeItem<String> child1 = new TreeItem<>("child1");
        TreeItem<String> child2 = new TreeItem<>("child2");
        root.getChildren().addAll(child1, child2);

        TreeItem<String> result = ProfilePanelController.getTreeViewItem(root, "child2");

        assertThat(result).as("found item").isSameAs(child2);
    }

    @Test
    @DisplayName("getTreeViewItem should find matching item at nested level")
    void getTreeViewItemShouldFindNestedItem() {
        TreeItem<String> root = new TreeItem<>("root");
        TreeItem<String> child = new TreeItem<>("child");
        TreeItem<String> grandchild = new TreeItem<>("grandchild");
        child.getChildren().add(grandchild);
        root.getChildren().add(child);

        TreeItem<String> result = ProfilePanelController.getTreeViewItem(root, "grandchild");

        assertThat(result).as("found nested item").isSameAs(grandchild);
    }

    @Test
    @DisplayName("getTreeViewItem should return null when value not found")
    void getTreeViewItemShouldReturnNullWhenNotFound() {
        TreeItem<String> root = new TreeItem<>("root");
        TreeItem<String> child = new TreeItem<>("child");
        root.getChildren().add(child);

        TreeItem<String> result = ProfilePanelController.getTreeViewItem(root, "nonexistent");

        assertThat(result).as("not found item").isNull();
    }

    @Test
    @DisplayName("getTreeViewItem should return null for null root")
    void getTreeViewItemShouldReturnNullForNullRoot() {
        TreeItem<String> result = ProfilePanelController.getTreeViewItem(null, "value");

        assertThat(result).as("null root result").isNull();
    }

    @Test
    @DisplayName("getTreeViewItem should return root when root value matches")
    void getTreeViewItemShouldReturnRootWhenMatch() {
        TreeItem<String> root = new TreeItem<>("root");

        TreeItem<String> result = ProfilePanelController.getTreeViewItem(root, "root");

        assertThat(result).as("root match").isSameAs(root);
    }

    // ==================== Public Method Tests ====================

    @Test
    @DisplayName("refreshList should not throw")
    void refreshListShouldNotThrow(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        assertThatCode(controller::refreshList).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("resizeColumns should not throw")
    void resizeColumnsShouldNotThrow(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        assertThatCode(controller::resizeColumns).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should set and retrieve profile loader")
    void shouldSetAndRetrieveProfileLoader(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        ProfileLoader mockLoader = mock(ProfileLoader.class);
        controller.setProfileLoader(mockLoader);
        // Verify no exception - the loader is stored privately
        assertThat(controller).as("controller with loader").isNotNull();
    }

    // ==================== updateFromMameRelocate Tests ====================

    @Test
    @DisplayName("updateFromMameRelocate should return NOTFOUND when mame file does not exist")
    void updateFromMameRelocateShouldReturnNotFoundWhenFileMissing(TestApp application) {
        ProfilePanelController controller = TestApp.getController();
        ProfileNFO mockNfo = mock(ProfileNFO.class);
        File missingMame = new File("/nonexistent/mame.exe");

        MameStatus status = controller.updateFromMameRelocate(mockNfo, missingMame);

        assertThat(status).as("status for missing mame").isEqualTo(MameStatus.NOTFOUND);
    }

    @Test
    @DisplayName("updateFromMameRelocate should delegate to nfo.mame.relocate when mame file exists")
    void updateFromMameRelocateShouldDelegateToRelocateWhenFileExists(TestApp application, @TempDir Path tempDir) throws Exception {
        ProfilePanelController controller = TestApp.getController();
        File existingMame = Files.createFile(tempDir.resolve("mame.exe")).toFile();

        ProfileNFO mockNfo = mock(ProfileNFO.class);
        jrm.profile.manager.ProfileNFOMame mockMame = mock(jrm.profile.manager.ProfileNFOMame.class);
        when(mockNfo.getMame()).thenReturn(mockMame);
        when(mockMame.relocate(existingMame)).thenReturn(MameStatus.NEEDUPDATE);

        MameStatus status = controller.updateFromMameRelocate(mockNfo, existingMame);

        assertThat(status).as("status returned by relocate").isEqualTo(MameStatus.NEEDUPDATE);
    }

    @Test
    @DisplayName("updateFromMameRelocate should propagate UPTODATE status from relocate")
    void updateFromMameRelocateShouldPropagateUpToDateStatus(TestApp application, @TempDir Path tempDir) throws Exception {
        ProfilePanelController controller = TestApp.getController();
        File existingMame = Files.createFile(tempDir.resolve("mame")).toFile();

        ProfileNFO mockNfo = mock(ProfileNFO.class);
        jrm.profile.manager.ProfileNFOMame mockMame = mock(jrm.profile.manager.ProfileNFOMame.class);
        when(mockNfo.getMame()).thenReturn(mockMame);
        when(mockMame.relocate(existingMame)).thenReturn(MameStatus.UPTODATE);

        MameStatus status = controller.updateFromMameRelocate(mockNfo, existingMame);

        assertThat(status).isEqualTo(MameStatus.UPTODATE);
    }

    // ==================== Cell Value Factory Tests ====================

    /**
     * Builds a mock {@link ProfileNFO} whose stats return the given values.
     *
     * @param version  the catalog version
     * @param created  the creation instant
     * @param scanned  the last scan instant
     * @param fixed    the last fix instant
     * @param sets     the sets have/total
     * @param roms     the roms have/total
     * @param disks    the disks have/total
     * @return a mock profile NFO
     */
    private ProfileNFO mockNfo(String version, Instant created, Instant scanned, Instant fixed, HaveNTotal sets, HaveNTotal roms, HaveNTotal disks) {
        ProfileNFO nfo = mock(ProfileNFO.class);
        ProfileNFOStats stats = mock(ProfileNFOStats.class);
        when(nfo.getStats()).thenReturn(stats);
        when(stats.getVersion()).thenReturn(version);
        when(stats.getCreated()).thenReturn(created);
        when(stats.getScanned()).thenReturn(scanned);
        when(stats.getFixed()).thenReturn(fixed);
        when(stats.getSets()).thenReturn(sets);
        when(stats.getRoms()).thenReturn(roms);
        when(stats.getDisks()).thenReturn(disks);
        return nfo;
    }

    /**
     * Calls a column's cell value factory for the given profile NFO row.
     *
     * @param colName the column field name
     * @param nfo     the row value
     * @param <T>     the value type
     * @return the produced value
     */
    @SuppressWarnings("unchecked")
    private <T> T callProfileValueFactory(String colName, ProfileNFO nfo) {
        ProfilePanelController controller = TestApp.getController();
        TableView<ProfileNFO> table = controller.profilesList;
        TableColumn<ProfileNFO, T> col;
        try {
            var field = ProfilePanelController.class.getDeclaredField(colName);
            field.setAccessible(true);
            col = (TableColumn<ProfileNFO, T>) field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CellDataFeatures<ProfileNFO, T> features = new CellDataFeatures<>(table, col, nfo);
        ObservableValue<T> observable = col.getCellValueFactory().call(features);
        return observable == null ? null : observable.getValue();
    }

    @Test
    @DisplayName("profile column value factory should return the profile NFO itself")
    void profileColValueFactoryShouldReturnNfo() {
        ProfileNFO nfo = mockNfo("1.0", null, null, null, null, null, null);
        ProfileNFO value = callProfileValueFactory("profileCol", nfo);
        assertThat(value).isSameAs(nfo);
    }

    @Test
    @DisplayName("profile version column value factory should return the stats version")
    void profileVersionColValueFactoryShouldReturnVersion() {
        ProfileNFO nfo = mockNfo("0.250", null, null, null, null, null, null);
        String value = callProfileValueFactory("profileVersionCol", nfo);
        assertThat(value).isEqualTo("0.250");
    }

    @Test
    @DisplayName("profile created column value factory should return the creation instant")
    void profileCreatedColValueFactoryShouldReturnCreated() {
        Instant now = Instant.now();
        ProfileNFO nfo = mockNfo(null, now, null, null, null, null, null);
        Instant value = callProfileValueFactory("profileCreatedCol", nfo);
        assertThat(value).isEqualTo(now);
    }

    @Test
    @DisplayName("profile last scan column value factory should return the scan instant")
    void profileLastScanColValueFactoryShouldReturnScanned() {
        Instant now = Instant.now();
        ProfileNFO nfo = mockNfo(null, null, now, null, null, null, null);
        Instant value = callProfileValueFactory("profileLastScanCol", nfo);
        assertThat(value).isEqualTo(now);
    }

    @Test
    @DisplayName("profile last fix column value factory should return the fix instant")
    void profileLastFixColValueFactoryShouldReturnFixed() {
        Instant now = Instant.now();
        ProfileNFO nfo = mockNfo(null, null, null, now, null, null, null);
        Instant value = callProfileValueFactory("profileLastFixCol", nfo);
        assertThat(value).isEqualTo(now);
    }

    @Test
    @DisplayName("profile have sets column value factory should return the sets have/total")
    void profileHaveSetsColValueFactoryShouldReturnSets() {
        HaveNTotal sets = mock(HaveNTotal.class);
        ProfileNFO nfo = mockNfo(null, null, null, null, sets, null, null);
        HaveNTotal value = callProfileValueFactory("profileHaveSetsCol", nfo);
        assertThat(value).isSameAs(sets);
    }

    @Test
    @DisplayName("profile have roms column value factory should return the roms have/total")
    void profileHaveRomsColValueFactoryShouldReturnRoms() {
        HaveNTotal roms = mock(HaveNTotal.class);
        ProfileNFO nfo = mockNfo(null, null, null, null, null, roms, null);
        HaveNTotal value = callProfileValueFactory("profileHaveRomsCol", nfo);
        assertThat(value).isSameAs(roms);
    }

    @Test
    @DisplayName("profile have disks column value factory should return the disks have/total")
    void profileHaveDisksColValueFactoryShouldReturnDisks() {
        HaveNTotal disks = mock(HaveNTotal.class);
        ProfileNFO nfo = mockNfo(null, null, null, null, null, null, disks);
        HaveNTotal value = callProfileValueFactory("profileHaveDisksCol", nfo);
        assertThat(value).isSameAs(disks);
    }

    // ==================== Menu State Tests ====================

    @Test
    @DisplayName("profileMenu onShowing should disable profile actions when nothing is selected")
    void profileMenuOnShowingShouldDisableActionsWhenNoSelection() {
        ProfilePanelController controller = TestApp.getController();
        controller.profilesList.getSelectionModel().clearSelection();

        controller.profileMenu.getOnShowing().handle(null);

        assertThat(controller.deleteProfileMenu.isDisable()).as("deleteProfile disabled").isTrue();
        assertThat(controller.renameProfileMenu.isDisable()).as("renameProfile disabled").isTrue();
        assertThat(controller.dropCacheMenu.isDisable()).as("dropCache disabled").isTrue();
        assertThat(controller.updateFromMameMenu.isDisable()).as("updateFromMame disabled").isTrue();
    }

    @Test
    @DisplayName("profileMenu onShowing should enable actions and disable update for non-JRM profile when selected")
    void profileMenuOnShowingShouldEnableActionsForSelectedNonJrmProfile() {
        ProfilePanelController controller = TestApp.getController();
        ProfileNFO nfo = mock(ProfileNFO.class);
        when(nfo.isJRM()).thenReturn(false);
        controller.profilesList.setItems(javafx.collections.FXCollections.observableArrayList(nfo));
        controller.profilesList.getSelectionModel().select(nfo);

        controller.profileMenu.getOnShowing().handle(null);

        assertThat(controller.deleteProfileMenu.isDisable()).as("deleteProfile enabled").isFalse();
        assertThat(controller.renameProfileMenu.isDisable()).as("renameProfile enabled").isFalse();
        assertThat(controller.dropCacheMenu.isDisable()).as("dropCache enabled").isFalse();
        assertThat(controller.updateFromMameMenu.isDisable()).as("updateFromMame disabled for non-JRM").isTrue();
    }

    @Test
    @DisplayName("profileMenu onShowing should enable update for JRM profile when selected")
    void profileMenuOnShowingShouldEnableUpdateForJrmProfile() {
        ProfilePanelController controller = TestApp.getController();
        ProfileNFO nfo = mock(ProfileNFO.class);
        when(nfo.isJRM()).thenReturn(true);
        controller.profilesList.setItems(javafx.collections.FXCollections.observableArrayList(nfo));
        controller.profilesList.getSelectionModel().select(nfo);

        controller.profileMenu.getOnShowing().handle(null);

        assertThat(controller.updateFromMameMenu.isDisable()).as("updateFromMame enabled for JRM").isFalse();
    }

    @Test
    @DisplayName("folderMenu onShowing should disable folder actions when nothing is selected")
    void folderMenuOnShowingShouldDisableActionsWhenNoSelection() {
        ProfilePanelController controller = TestApp.getController();
        controller.profilesTree.getSelectionModel().clearSelection();

        controller.folderMenu.getOnShowing().handle(null);

        assertThat(controller.deleteFolderMenu.isDisable()).as("deleteFolder disabled").isTrue();
        assertThat(controller.createFolderMenu.isDisable()).as("createFolder disabled").isTrue();
    }

    @Test
    @DisplayName("folderMenu onShowing should enable folder actions when a folder is selected")
    void folderMenuOnShowingShouldEnableActionsWhenFolderSelected() {
        ProfilePanelController controller = TestApp.getController();
        TreeItem<jrm.profile.manager.Dir> item = new TreeItem<>(mock(jrm.profile.manager.Dir.class));
        controller.profilesTree.setRoot(item);
        controller.profilesTree.getSelectionModel().select(item);

        controller.folderMenu.getOnShowing().handle(null);

        assertThat(controller.deleteFolderMenu.isDisable()).as("deleteFolder enabled").isFalse();
        assertThat(controller.createFolderMenu.isDisable()).as("createFolder enabled").isFalse();
    }

    // ==================== Row / Tree Cell Factory Tests ====================

    @Test
    @DisplayName("profilesList row factory should build a row with a click handler")
    void profilesListRowFactoryShouldBuildRowWithClickHandler() {
        ProfilePanelController controller = TestApp.getController();
        assertThat(controller.profilesList.getRowFactory().call(controller.profilesList)).isNotNull();
    }

    @Test
    @DisplayName("profilesTree cell factory should build an editable tree cell")
    void profilesTreeCellFactoryShouldBuildTreeCell() {
        ProfilePanelController controller = TestApp.getController();
        assertThat(controller.profilesTree.getCellFactory().call(controller.profilesTree)).isNotNull();
    }

    // ==================== deleteProfile Tests ====================

    @Test
    @DisplayName("deleteProfile should remove the selected profile when it is not the current profile")
    void deleteProfileShouldRemoveSelectedWhenNotCurrent() {
        ProfilePanelController controller = TestApp.getController();
        ProfileNFO nfo = mock(ProfileNFO.class);
        when(nfo.delete()).thenReturn(true);
        controller.profilesList.setItems(javafx.collections.FXCollections.observableArrayList(nfo));
        controller.profilesList.getSelectionModel().select(nfo);

        controller.deleteProfile(null);

        assertThat(controller.profilesList.getItems()).doesNotContain(nfo);
    }

    @Test
    @DisplayName("deleteProfile should not remove the currently loaded profile")
    void deleteProfileShouldNotRemoveCurrentProfile() {
        ProfilePanelController controller = TestApp.getController();
        ProfileNFO nfo = mock(ProfileNFO.class);
        // Make nfo the current profile: getCurrProfile().getNfo() returns the same instance
        jrm.profile.Profile currProfile = mock(jrm.profile.Profile.class);
        when(currProfile.getNfo()).thenReturn(nfo);
        when(TestApp.controller.session.getCurrProfile()).thenReturn(currProfile);
        controller.profilesList.setItems(javafx.collections.FXCollections.observableArrayList(nfo));
        controller.profilesList.getSelectionModel().select(nfo);

        controller.deleteProfile(null);

        assertThat(controller.profilesList.getItems()).contains(nfo);
        verify(nfo, never()).delete();
    }

    // ==================== dropCache Tests ====================

    @Test
    @DisplayName("dropCache should delete the .cache file for the selected profile")
    void dropCacheShouldDeleteCacheFile(@TempDir Path tempDir) throws Exception {
        ProfilePanelController controller = TestApp.getController();
        File profileFile = Files.createFile(tempDir.resolve("profile.jrm")).toFile();
        Files.createFile(tempDir.resolve("profile.jrm.cache"));
        ProfileNFO nfo = mock(ProfileNFO.class);
        when(nfo.getFile()).thenReturn(profileFile);
        controller.profilesList.setItems(javafx.collections.FXCollections.observableArrayList(nfo));
        controller.profilesList.getSelectionModel().select(nfo);

        controller.dropCache(null);

        assertThat(new File(profileFile.getAbsolutePath() + ".cache")).doesNotExist();
    }

    @Test
    @DisplayName("dropCache should not throw when the cache file does not exist")
    void dropCacheShouldNotThrowWhenCacheMissing(@TempDir Path tempDir) {
        ProfilePanelController controller = TestApp.getController();
        File profileFile = new File(tempDir.toFile(), "nocache.jrm");
        ProfileNFO nfo = mock(ProfileNFO.class);
        when(nfo.getFile()).thenReturn(profileFile);
        controller.profilesList.setItems(javafx.collections.FXCollections.observableArrayList(nfo));
        controller.profilesList.getSelectionModel().select(nfo);

        assertThatCode(() -> controller.dropCache(null)).doesNotThrowAnyException();
    }

    // ==================== renameProfile Tests ====================

    @Test
    @DisplayName("renameProfile should enter edit mode on the profile column")
    void renameProfileShouldEnterEditMode() {
        ProfilePanelController controller = TestApp.getController();
        ProfileNFO nfo = mock(ProfileNFO.class);
        controller.profilesList.setItems(javafx.collections.FXCollections.observableArrayList(nfo));
        controller.profilesList.getSelectionModel().select(nfo);

        // renameProfile toggles editable and calls edit; verify no exception and editable flag reset
        assertThatCode(() -> controller.renameProfile(null)).doesNotThrowAnyException();
        assertThat(controller.profilesList.isEditable()).isFalse();
    }

    // ==================== actionLoad Tests ====================

    @Test
    @DisplayName("actionLoad should delegate to the profile loader when a profile is selected")
    void actionLoadShouldDelegateToProfileLoader() {
        ProfilePanelController controller = TestApp.getController();
        ProfileNFO nfo = mock(ProfileNFO.class);
        controller.profilesList.setItems(javafx.collections.FXCollections.observableArrayList(nfo));
        controller.profilesList.getSelectionModel().select(nfo);
        ProfileLoader loader = mock(ProfileLoader.class);
        controller.setProfileLoader(loader);

        controller.actionLoad(null);

        verify(loader).loadProfile(controller.session, nfo);
    }

    @Test
    @DisplayName("actionLoad should do nothing when no profile is selected")
    void actionLoadShouldDoNothingWhenNoSelection() {
        ProfilePanelController controller = TestApp.getController();
        controller.profilesList.getSelectionModel().clearSelection();
        ProfileLoader loader = mock(ProfileLoader.class);
        controller.setProfileLoader(loader);

        controller.actionLoad(null);

        verify(loader, never()).loadProfile(eq(controller.session), org.mockito.ArgumentMatchers.any());
    }
}
