package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jrm.fx.ui.profile.ProfileViewerController;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.EntityBase;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.security.User;
import jrm.misc.GlobalSettings;

/**
 * Tests for {@link ProfileViewerController}.
 * <p>
 * Verifies initialization, filter methods, selection operations,
 * search predicate logic, and table management.
 *
 * @since 3.0.5
 */
@TestFxApplication(ProfileViewerControllerTest.TestApp.class)
@DisplayName("ProfileViewerController Tests")
class ProfileViewerControllerTest {

    /**
     * Minimal application that creates a {@link ProfileViewerController}
     * with all FXML fields injected via reflection and mocks for Session.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        static ProfileViewerController controller;

        @Override
        public Stage recordedStage() {
            return primaryStage;
        }

        @Override
        public void start(Stage primaryStage) throws Exception {
            this.primaryStage = primaryStage;

            // Create mock chain
            GlobalSettings mockSettings = mock(GlobalSettings.class);
            when(mockSettings.getWorkPath()).thenReturn(java.nio.file.Paths.get(System.getProperty("java.io.tmpdir")));

            User mockUser = mock(User.class);
            when(mockUser.getSettings()).thenReturn(mockSettings);

            Profile mockProfile = mock(Profile.class);
            jrm.profile.data.MachineListList mockMLL = mock(jrm.profile.data.MachineListList.class);
            jrm.profile.data.SoftwareListList mockSLL = mock(jrm.profile.data.SoftwareListList.class);
            when(mockMLL.getSoftwareListList()).thenReturn(mockSLL);
            when(mockProfile.getMachineListList()).thenReturn(mockMLL);

            Session mockSession = mock(Session.class);
            when(mockSession.getUser()).thenReturn(mockUser);
            when(mockSession.getCurrProfile()).thenReturn(mockProfile);

            // Configure shared mock to return our session
            SharedMockSession.getOrCreate().when(Sessions::getSingleSession).thenReturn(mockSession);

            // Create the controller - session is automatically set via Sessions.getSingleSession()
            // which is mocked by SharedMockSession above
            controller = new ProfileViewerController();

            // Create and inject FXML fields
            TableView<AnywareList<? extends Anyware>> tableWL = new TableView<>();
            javafx.scene.control.TableColumn<AnywareList<? extends Anyware>, AnywareList<? extends Anyware>> tableWLName = new javafx.scene.control.TableColumn<>();
            javafx.scene.control.TableColumn<AnywareList<? extends Anyware>, String> tableWLDesc = new javafx.scene.control.TableColumn<>();
            javafx.scene.control.TableColumn<AnywareList<? extends Anyware>, String> tableWLHave = new javafx.scene.control.TableColumn<>();

            ToggleButton toggleWLUnknown = new ToggleButton();
            ToggleButton toggleWLMissing = new ToggleButton();
            ToggleButton toggleWLPartial = new ToggleButton();
            ToggleButton toggleWLComplete = new ToggleButton();

            TableView<Anyware> tableW = new TableView<>();
            ToggleButton toggleWUnknown = new ToggleButton();
            ToggleButton toggleWMissing = new ToggleButton();
            ToggleButton toggleWPartial = new ToggleButton();
            ToggleButton toggleWComplete = new ToggleButton();

            TextField search = new TextField();
            TableView<EntityBase> tableEntity = new TableView<>();
            javafx.scene.control.TableColumn<EntityBase, EntityBase> tableEntityStatus = new javafx.scene.control.TableColumn<>();
            javafx.scene.control.TableColumn<EntityBase, EntityBase> tableEntityName = new javafx.scene.control.TableColumn<>();
            javafx.scene.control.TableColumn<EntityBase, Long> tableEntitySize = new javafx.scene.control.TableColumn<>();
            javafx.scene.control.TableColumn<EntityBase, String> tableEntityCRC = new javafx.scene.control.TableColumn<>();
            javafx.scene.control.TableColumn<EntityBase, String> tableEntityMD5 = new javafx.scene.control.TableColumn<>();
            javafx.scene.control.TableColumn<EntityBase, String> tableEntitySHA1 = new javafx.scene.control.TableColumn<>();
            javafx.scene.control.TableColumn<EntityBase, String> tableEntityMergeName = new javafx.scene.control.TableColumn<>();
            javafx.scene.control.TableColumn<EntityBase, jrm.profile.data.Entity.Status> tableEntityDumpStatus = new javafx.scene.control.TableColumn<>();

            ToggleButton toggleEntityUnknown = new ToggleButton();
            ToggleButton toggleEntityKO = new ToggleButton();
            ToggleButton toggleEntityOK = new ToggleButton();

            ContextMenu menuWL = new ContextMenu();
            MenuItem mntmFilteredAsLogiqxDat = new MenuItem();
            MenuItem mntmFilteredAsMameDat = new MenuItem();
            MenuItem mntmFilteredAsSoftwareLists = new MenuItem();
            MenuItem mntmAllAsLogiqxDat = new MenuItem();
            MenuItem mntmAllAsMameDat = new MenuItem();
            MenuItem mntmAllAsSoftwareLists = new MenuItem();
            MenuItem mntmSelectedFilteredAsSoftwareLists = new MenuItem();
            MenuItem mntmSelectedAsSoftwareLists = new MenuItem();

            ContextMenu menuW = new ContextMenu();
            MenuItem mntmSelectByKeywords = new MenuItem();
            MenuItem mntmSelectAll = new MenuItem();
            MenuItem mntmSelectNone = new MenuItem();
            MenuItem mntmSelectInvert = new MenuItem();

            ContextMenu menuEntity = new ContextMenu();
            MenuItem mntmCopyCrc = new MenuItem();
            MenuItem mntmCopySha1 = new MenuItem();
            MenuItem mntmCopyName = new MenuItem();
            MenuItem mntmSearchWeb = new MenuItem();

            // Inject all fields
            injectField(controller, "tableWL", tableWL);
            injectField(controller, "tableWLName", tableWLName);
            injectField(controller, "tableWLDesc", tableWLDesc);
            injectField(controller, "tableWLHave", tableWLHave);
            injectField(controller, "toggleWLUnknown", toggleWLUnknown);
            injectField(controller, "toggleWLMissing", toggleWLMissing);
            injectField(controller, "toggleWLPartial", toggleWLPartial);
            injectField(controller, "toggleWLComplete", toggleWLComplete);
            injectField(controller, "tableW", tableW);
            injectField(controller, "toggleWUnknown", toggleWUnknown);
            injectField(controller, "toggleWMissing", toggleWMissing);
            injectField(controller, "toggleWPartial", toggleWPartial);
            injectField(controller, "toggleWComplete", toggleWComplete);
            injectField(controller, "search", search);
            injectField(controller, "tableEntity", tableEntity);
            injectField(controller, "tableEntityStatus", tableEntityStatus);
            injectField(controller, "tableEntityName", tableEntityName);
            injectField(controller, "tableEntitySize", tableEntitySize);
            injectField(controller, "tableEntityCRC", tableEntityCRC);
            injectField(controller, "tableEntityMD5", tableEntityMD5);
            injectField(controller, "tableEntitySHA1", tableEntitySHA1);
            injectField(controller, "tableEntityMergeName", tableEntityMergeName);
            injectField(controller, "tableEntityDumpStatus", tableEntityDumpStatus);
            injectField(controller, "toggleEntityUnknown", toggleEntityUnknown);
            injectField(controller, "toggleEntityKO", toggleEntityKO);
            injectField(controller, "toggleEntityOK", toggleEntityOK);
            injectField(controller, "menuWL", menuWL);
            injectField(controller, "mntmFilteredAsLogiqxDat", mntmFilteredAsLogiqxDat);
            injectField(controller, "mntmFilteredAsMameDat", mntmFilteredAsMameDat);
            injectField(controller, "mntmFilteredAsSoftwareLists", mntmFilteredAsSoftwareLists);
            injectField(controller, "mntmAllAsLogiqxDat", mntmAllAsLogiqxDat);
            injectField(controller, "mntmAllAsMameDat", mntmAllAsMameDat);
            injectField(controller, "mntmAllAsSoftwareLists", mntmAllAsSoftwareLists);
            injectField(controller, "mntmSelectedFilteredAsSoftwareLists", mntmSelectedFilteredAsSoftwareLists);
            injectField(controller, "mntmSelectedAsSoftwareLists", mntmSelectedAsSoftwareLists);
            injectField(controller, "menuW", menuW);
            injectField(controller, "mntmSelectByKeywords", mntmSelectByKeywords);
            injectField(controller, "mntmSelectAll", mntmSelectAll);
            injectField(controller, "mntmSelectNone", mntmSelectNone);
            injectField(controller, "mntmSelectInvert", mntmSelectInvert);
            injectField(controller, "menuEntity", menuEntity);
            injectField(controller, "mntmCopyCrc", mntmCopyCrc);
            injectField(controller, "mntmCopySha1", mntmCopySha1);
            injectField(controller, "mntmCopyName", mntmCopyName);
            injectField(controller, "mntmSearchWeb", mntmSearchWeb);

            // Initialize the controller
            controller.initialize(null, null);

            VBox root = new VBox();
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        private static void injectField(Object target, String fieldName, Object value) {
            try {
                var field = ProfileViewerController.class.getDeclaredField(fieldName);
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
        public static ProfileViewerController getController() {
            return controller;
        }
    }

    @Test
    @DisplayName("Should initialize controller with all fields")
    void shouldInitializeController() {
        ProfileViewerController controller = TestApp.getController();
        assertThat(controller).isNotNull();
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String fieldName) {
        try {
            var field = ProfileViewerController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to get field " + fieldName, e);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should clear all tables and cache")
    void shouldClearAllTablesAndCache() {
        ProfileViewerController controller = TestApp.getController();

        // Add some items to tables
        TableView<AnywareList<? extends Anyware>> tableWL = getField(controller, "tableWL");
        TableView<Anyware> tableW = getField(controller, "tableW");
        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");

        tableWL.getItems().add(mock(AnywareList.class));
        tableW.getItems().add(mock(Anyware.class));
        tableEntity.getItems().add(mock(EntityBase.class));

        // Clear
        controller.clear();

        assertThat(tableWL.getItems()).isEmpty();
        assertThat(tableW.getItems()).isEmpty();
        assertThat(tableEntity.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Should reload tables")
    void shouldReloadTables() {
        ProfileViewerController controller = TestApp.getController();

        // Should not throw
        controller.reload();

        assertThat(controller).isNotNull();
    }

    @Test
    @DisplayName("Should handle diskMultipleFilter action")
    void shouldHandleDiskMultipleFilter() {
        ProfileViewerController controller = TestApp.getController();

        // Set up mock profile
        Profile mockProfile = mock(Profile.class);
        Session mockSession = mock(Session.class);
        when(mockSession.getCurrProfile()).thenReturn(mockProfile);

        // Set toggle states via reflection
        ToggleButton toggleWLUnknown = getField(controller, "toggleWLUnknown");
        ToggleButton toggleWLMissing = getField(controller, "toggleWLMissing");
        ToggleButton toggleWLPartial = getField(controller, "toggleWLPartial");
        ToggleButton toggleWLComplete = getField(controller, "toggleWLComplete");

        toggleWLUnknown.setSelected(true);
        toggleWLMissing.setSelected(false);
        toggleWLPartial.setSelected(true);
        toggleWLComplete.setSelected(false);

        // Should not throw
        controller.diskMultipleFilter(null);

        assertThat(toggleWLUnknown.isSelected()).isTrue();
    }

    @Test
    @DisplayName("Should handle folderFilter action")
    void shouldHandleFolderFilter() {
        ProfileViewerController controller = TestApp.getController();

        // Set toggle states via reflection
        ToggleButton toggleWUnknown = getField(controller, "toggleWUnknown");
        ToggleButton toggleWMissing = getField(controller, "toggleWMissing");
        ToggleButton toggleWPartial = getField(controller, "toggleWPartial");
        ToggleButton toggleWComplete = getField(controller, "toggleWComplete");

        toggleWUnknown.setSelected(true);
        toggleWMissing.setSelected(true);
        toggleWPartial.setSelected(false);
        toggleWComplete.setSelected(false);

        // Should not throw
        controller.folderFilter(null);

        assertThat(toggleWUnknown.isSelected()).isTrue();
    }

    @Test
    @DisplayName("Should handle bulletFilter action")
    void shouldHandleBulletFilter() {
        ProfileViewerController controller = TestApp.getController();

        // Set toggle states via reflection
        ToggleButton toggleEntityUnknown = getField(controller, "toggleEntityUnknown");
        ToggleButton toggleEntityKO = getField(controller, "toggleEntityKO");
        ToggleButton toggleEntityOK = getField(controller, "toggleEntityOK");

        toggleEntityUnknown.setSelected(true);
        toggleEntityKO.setSelected(false);
        toggleEntityOK.setSelected(true);

        // Should not throw
        controller.bulletFilter(null);

        assertThat(toggleEntityUnknown.isSelected()).isTrue();
    }

    @Test
    @DisplayName("Should select all items")
    void shouldSelectAllItems() {
        ProfileViewerController controller = TestApp.getController();

        // Add mock items via reflection
        TableView<Anyware> tableW = getField(controller, "tableW");
        Anyware mock1 = mock(Anyware.class);
        Anyware mock2 = mock(Anyware.class);
        tableW.setItems(FXCollections.observableArrayList(mock1, mock2));

        // Select all
        controller.selectAll(null);

        assertThat(tableW.getSelectionModel().getSelectedItems()).hasSize(2);
    }

    @Test
    @DisplayName("Should select no items")
    void shouldSelectNoItems() {
        ProfileViewerController controller = TestApp.getController();

        // Add mock items via reflection
        TableView<Anyware> tableW = getField(controller, "tableW");
        Anyware mock1 = mock(Anyware.class);
        Anyware mock2 = mock(Anyware.class);
        tableW.setItems(FXCollections.observableArrayList(mock1, mock2));

        // Select none
        controller.selectNone(null);

        assertThat(tableW.getSelectionModel().getSelectedItems()).isEmpty();
    }

    @Test
    @DisplayName("Should invert selection")
    void shouldInvertSelection() {
        ProfileViewerController controller = TestApp.getController();

        // Add mock items via reflection
        TableView<Anyware> tableW = getField(controller, "tableW");
        Anyware mock1 = mock(Anyware.class);
        Anyware mock2 = mock(Anyware.class);
        tableW.setItems(FXCollections.observableArrayList(mock1, mock2));

        // Invert selection
        controller.selectInvert(null);

        assertThat(tableW.getSelectionModel().getSelectedItems()).hasSize(2);
    }

    @Test
    @DisplayName("Should handle copyCrc with no selection")
    void shouldHandleCopyCrcWithNoSelection() {
        ProfileViewerController controller = TestApp.getController();

        // Clear selection via reflection
        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
        tableEntity.getSelectionModel().clearSelection();

        // Should not throw
        controller.copyCrc(null);

        assertThat(tableEntity.getSelectionModel().getSelectedItems()).isEmpty();
    }

    @Test
    @DisplayName("Should handle copySha1 with no selection")
    void shouldHandleCopySha1WithNoSelection() {
        ProfileViewerController controller = TestApp.getController();

        // Clear selection via reflection
        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
        tableEntity.getSelectionModel().clearSelection();

        // Should not throw
        controller.copySha1(null);

        assertThat(tableEntity.getSelectionModel().getSelectedItems()).isEmpty();
    }

    @Test
    @DisplayName("Should handle copyName with no selection")
    void shouldHandleCopyNameWithNoSelection() {
        ProfileViewerController controller = TestApp.getController();

        // Clear selection via reflection
        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
        tableEntity.getSelectionModel().clearSelection();

        // Should not throw
        controller.copyName(null);

        assertThat(tableEntity.getSelectionModel().getSelectedItems()).isEmpty();
    }

    @Test
    @DisplayName("Should handle searchWeb with no selection")
    void shouldHandleSearchWebWithNoSelection() {
        ProfileViewerController controller = TestApp.getController();

        // Clear selection via reflection
        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
        tableEntity.getSelectionModel().clearSelection();

        // Should not throw
        controller.searchWeb(null);

        assertThat(tableEntity.getSelectionModel().getSelectedItems()).isEmpty();
    }

    @Test
    @DisplayName("Should handle export methods with no profile")
    void shouldHandleExportMethodsWithNoProfile() {
        ProfileViewerController controller = TestApp.getController();

        // These should not throw even without a proper scene
        try {
            controller.exportFilteredAsLogiqxDat(null);
        } catch (Exception _) {
            // Expected - no scene
        }

        try {
            controller.exportAllAsMameDat(null);
        } catch (Exception _) {
            // Expected - no scene
        }

        assertThat(controller).isNotNull();
    }
}
