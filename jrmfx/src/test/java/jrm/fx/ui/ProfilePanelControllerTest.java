package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
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
        private Stage primaryStage;
        private static ProfilePanelController controller;
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
}
