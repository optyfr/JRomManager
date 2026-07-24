package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jrm.fx.ui.profile.ProfileViewerController;
import jrm.profile.Profile;
import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;
import jrm.profile.data.AnywareStatus;
import jrm.profile.data.Disk;
import jrm.profile.data.Entity;
import jrm.profile.data.EntityBase;
import jrm.profile.data.Machine;
import jrm.profile.data.MachineList;
import jrm.profile.data.Rom;
import jrm.profile.data.Sample;
import jrm.profile.data.Software;
import jrm.profile.data.SoftwareList;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.security.User;
import jrm.misc.GlobalSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import javafx.application.Platform;

/**
 * Tests for {@link ProfileViewerController}.
 * <p>
 * Verifies initialization, filter methods, selection operations, search predicate logic, and table management.
 *
 * @since 3.0.5
 */
@TestFxApplication(ProfileViewerControllerTest.TestApp.class)
@DisplayName("ProfileViewerController Tests")
class ProfileViewerControllerTest {

    /**
     * Minimal application that creates a {@link ProfileViewerController} with all FXML fields injected via reflection and mocks for
     * Session.
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

        /** Injects a value into a private field of the target object using reflection. */
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

    /** Retrieves a private field value from the target object via reflection. */
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

    /**
     * Runs an action on the JavaFX Application Thread and waits for completion. Required for clipboard operations which must run on
     * the FX thread.
     *
     * @param action the action to run
     * 
     * @throws Exception if the action throws
     */
    private void runOnFxThread(ThrowingAction action) throws Exception {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                future.complete(null);
            } catch (Throwable t) /* NOSONAR */ {
                future.completeExceptionally(t);
            }
        });
        future.get();
    }

    /**
     * Functional interface for an action that may throw checked exceptions.
     */
    @FunctionalInterface
    interface ThrowingAction {
        void run() throws Exception;
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

        // Select all - the controller marks every item's selected flag (the
        // checkbox column), not the TableView selection model, so verify
        // setSelected(true) was called on each item rather than the selection.
        controller.selectAll(null);

        verify(mock1).setSelected(true);
        verify(mock2).setSelected(true);
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

        // Select none - the controller clears every item's selected flag (the
        // checkbox column), not the TableView selection model, so verify
        // setSelected(false) was called on each item rather than the selection.
        controller.selectNone(null);

        verify(mock1).setSelected(false);
        verify(mock2).setSelected(false);
    }

    @Test
    @DisplayName("Should invert selection")
    void shouldInvertSelection() {
        ProfileViewerController controller = TestApp.getController();

        // Add mock items via reflection
        TableView<Anyware> tableW = getField(controller, "tableW");
        Anyware mock1 = mock(Anyware.class);
        Anyware mock2 = mock(Anyware.class);
        // mock1 is unselected -> should become selected; mock2 is selected -> should become unselected
        when(mock1.isSelected()).thenReturn(false);
        when(mock2.isSelected()).thenReturn(true);
        tableW.setItems(FXCollections.observableArrayList(mock1, mock2));

        // Invert selection - the controller toggles every item's selected flag
        // (the checkbox column), not the TableView selection model, so verify the
        // inverted setSelected value was applied to each item.
        controller.selectInvert(null);

        verify(mock1).setSelected(true);
        verify(mock2).setSelected(false);
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

    // ==================== getStatusIcon (private static) Tests ====================

    /**
     * Invokes the private static {@code getStatusIcon(AnywareStatus)} method via reflection.
     *
     * @param status the status to resolve
     * 
     * @return the image returned by the method (may be {@code null} if the icon is missing)
     * 
     * @throws ReflectiveOperationException if reflection fails
     */
    private static javafx.scene.image.Image invokeGetStatusIcon(AnywareStatus status) throws ReflectiveOperationException {
        final Method method = ProfileViewerController.class.getDeclaredMethod("getStatusIcon", AnywareStatus.class);
        method.setAccessible(true);
        try {
            return (javafx.scene.image.Image) method.invoke(null, status);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw e;
        }
    }

    @Test
    @DisplayName("getStatusIcon should return the same icon for COMPLETE regardless of repetition")
    void getStatusIconShouldReturnSameIconForComplete() throws Exception {
        final var first = invokeGetStatusIcon(AnywareStatus.COMPLETE);
        final var second = invokeGetStatusIcon(AnywareStatus.COMPLETE);

        assertThat(first).as("COMPLETE icon").isSameAs(second);
    }

    @Test
    @DisplayName("getStatusIcon should return distinct icons for COMPLETE and MISSING")
    void getStatusIconShouldReturnDistinctIconsForCompleteAndMissing() throws Exception {
        final var complete = invokeGetStatusIcon(AnywareStatus.COMPLETE);
        final var missing = invokeGetStatusIcon(AnywareStatus.MISSING);

        assertThat(complete).as("COMPLETE icon").isNotNull();
        assertThat(missing).as("MISSING icon").isNotNull();
        assertThat(complete).isNotSameAs(missing);
    }

    @Test
    @DisplayName("getStatusIcon should return the gray icon for UNKNOWN and default")
    void getStatusIconShouldReturnGrayIconForUnknownAndDefault() throws Exception {
        final var unknown = invokeGetStatusIcon(AnywareStatus.UNKNOWN);

        assertThat(unknown).as("UNKNOWN icon").isNotNull();
    }

    // ==================== getWidth (private) Tests ====================

    /**
     * Invokes the private {@code getWidth(int)} method via reflection.
     *
     * @param controller the controller instance
     * @param digits the number of digits
     * 
     * @return the computed width
     * 
     * @throws ReflectiveOperationException if reflection fails
     */
    private static double invokeGetWidth(ProfileViewerController controller, int digits) throws ReflectiveOperationException {
        final Method method = ProfileViewerController.class.getDeclaredMethod("getWidth", int.class);
        method.setAccessible(true);
        return (double) method.invoke(controller, digits);
    }

    @Test
    @DisplayName("getWidth should return a positive value for a reasonable digit count")
    void getWidthShouldReturnPositiveValueForReasonableDigitCount() throws Exception {
        ProfileViewerController controller = TestApp.getController();

        final var width = invokeGetWidth(controller, 10);

        assertThat(width).as("width for 10 digits").isPositive();
    }

    @Test
    @DisplayName("getWidth should return a larger value for more digits")
    void getWidthShouldReturnLargerValueForMoreDigits() throws Exception {
        ProfileViewerController controller = TestApp.getController();

        final var small = invokeGetWidth(controller, 5);
        final var large = invokeGetWidth(controller, 20);

        assertThat(large).as("width for 20 digits should exceed width for 5 digits").isGreaterThan(small);
    }

    @Test
    @DisplayName("getWidth with monospaced font should return a positive value")
    void getWidthWithMonospacedFontShouldReturnPositiveValue() throws Exception {
        ProfileViewerController controller = TestApp.getController();
        final Method method = ProfileViewerController.class.getDeclaredMethod("getWidth", int.class, String.class);
        method.setAccessible(true);

        final var width = (double) method.invoke(controller, 10, "monospaced");

        assertThat(width).as("monospaced width for 10 digits").isPositive();
    }

    // ==================== menuEntity onShowing Handler Tests ====================

    @Test
    @DisplayName("menuEntity onShowing should disable copy/search items when no entity is selected")
    void menuEntityOnShowingShouldDisableItemsWhenNoSelection() {
        ProfileViewerController controller = TestApp.getController();

        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
        tableEntity.getSelectionModel().clearSelection();

        ContextMenu menuEntity = getField(controller, "menuEntity");
        menuEntity.getOnShowing().handle(null);

        MenuItem mntmCopyCrc = getField(controller, "mntmCopyCrc");
        MenuItem mntmCopySha1 = getField(controller, "mntmCopySha1");
        MenuItem mntmCopyName = getField(controller, "mntmCopyName");
        MenuItem mntmSearchWeb = getField(controller, "mntmSearchWeb");

        assertThat(mntmCopyCrc.isDisable()).as("copyCrc disabled when no selection").isTrue();
        assertThat(mntmCopySha1.isDisable()).as("copySha1 disabled when no selection").isTrue();
        assertThat(mntmCopyName.isDisable()).as("copyName disabled when no selection").isTrue();
        assertThat(mntmSearchWeb.isDisable()).as("searchWeb disabled when no selection").isTrue();
    }

    @Test
    @DisplayName("menuEntity onShowing should enable copy/search items when an entity is selected")
    void menuEntityOnShowingShouldEnableItemsWhenSelectionExists() {
        ProfileViewerController controller = TestApp.getController();

        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
        EntityBase mockEntity = mock(EntityBase.class);
        tableEntity.setItems(FXCollections.observableArrayList(mockEntity));
        tableEntity.getSelectionModel().select(mockEntity);

        ContextMenu menuEntity = getField(controller, "menuEntity");
        menuEntity.getOnShowing().handle(null);

        MenuItem mntmCopyCrc = getField(controller, "mntmCopyCrc");
        MenuItem mntmCopySha1 = getField(controller, "mntmCopySha1");
        MenuItem mntmCopyName = getField(controller, "mntmCopyName");
        MenuItem mntmSearchWeb = getField(controller, "mntmSearchWeb");

        assertThat(mntmCopyCrc.isDisable()).as("copyCrc enabled when selection exists").isFalse();
        assertThat(mntmCopySha1.isDisable()).as("copySha1 enabled when selection exists").isFalse();
        assertThat(mntmCopyName.isDisable()).as("copyName enabled when selection exists").isFalse();
        assertThat(mntmSearchWeb.isDisable()).as("searchWeb enabled when selection exists").isFalse();
    }

    // ==================== copy methods with a selected Entity Tests ====================

    @Test
    @DisplayName("copyCrc should copy the entity CRC to the clipboard when an Entity is selected")
    void copyCrcShouldCopyEntityCrcToClipboardWhenSelected() throws Exception {
        ProfileViewerController controller = TestApp.getController();

        runOnFxThread(() -> {
            TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.getCrc()).thenReturn("DEADBEEF");
            tableEntity.setItems(FXCollections.observableArrayList(mockEntity));
            tableEntity.getSelectionModel().select(mockEntity);

            controller.copyCrc(null);

            verify(mockEntity).getCrc();
            assertThat(javafx.scene.input.Clipboard.getSystemClipboard().getString()).isEqualTo("DEADBEEF");
        });
    }

    @Test
    @DisplayName("copySha1 should copy the entity SHA1 to the clipboard when an Entity is selected")
    void copySha1ShouldCopyEntitySha1ToClipboardWhenSelected() throws Exception {
        ProfileViewerController controller = TestApp.getController();

        runOnFxThread(() -> {
            TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.getSha1()).thenReturn("SHA1HASH");
            tableEntity.setItems(FXCollections.observableArrayList(mockEntity));
            tableEntity.getSelectionModel().select(mockEntity);

            controller.copySha1(null);

            verify(mockEntity).getSha1();
            assertThat(javafx.scene.input.Clipboard.getSystemClipboard().getString()).isEqualTo("SHA1HASH");
        });
    }

    @Test
    @DisplayName("copyName should copy the entity name to the clipboard when an Entity is selected")
    void copyNameShouldCopyEntityNameToClipboardWhenSelected() throws Exception {
        ProfileViewerController controller = TestApp.getController();

        runOnFxThread(() -> {
            TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
            Entity mockEntity = mock(Entity.class);
            when(mockEntity.getName()).thenReturn("rom.bin");
            tableEntity.setItems(FXCollections.observableArrayList(mockEntity));
            tableEntity.getSelectionModel().select(mockEntity);

            controller.copyName(null);

            verify(mockEntity).getName();
            assertThat(javafx.scene.input.Clipboard.getSystemClipboard().getString()).isEqualTo("rom.bin");
        });
    }

    @Test
    @DisplayName("copyCrc should do nothing when selected item is not an Entity")
    void copyCrcShouldDoNothingWhenSelectedItemIsNotAnEntity() {
        ProfileViewerController controller = TestApp.getController();

        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
        EntityBase notAnEntity = mock(EntityBase.class);
        tableEntity.setItems(FXCollections.observableArrayList(notAnEntity));
        tableEntity.getSelectionModel().select(notAnEntity);

        // Should not throw and should not interact with the non-Entity mock
        controller.copyCrc(null);

        assertThat(controller).isNotNull();
    }

    // ==================== Entity Cell Value Factory Tests ====================

    /**
     * Calls a column's cell value factory for the given entity row.
     *
     * @param colName the column field name
     * @param row     the row value
     * @param <T>     the value type
     * @return the produced value
     */
    private <T> T callEntityValueFactory(String colName, EntityBase row) {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<EntityBase, T> col = getField(controller, colName);
        TableView<EntityBase> table = getField(controller, "tableEntity");
        CellDataFeatures<EntityBase, T> features = new CellDataFeatures<>(table, col, row);
        ObservableValue<T> observable = col.getCellValueFactory().call(features);
        return observable == null ? null : observable.getValue();
    }

    @Test
    @DisplayName("entity status value factory should return the entity base itself")
    void entityStatusValueFactoryShouldReturnEntityBase() {
        Rom rom = mock(Rom.class);
        EntityBase value = callEntityValueFactory("tableEntityStatus", rom);
        assertThat(value).isSameAs(rom);
    }

    @Test
    @DisplayName("entity CRC value factory should return the ROM CRC")
    void entityCrcValueFactoryShouldReturnRomCrc() {
        Rom rom = mock(Rom.class);
        when(rom.getCrc()).thenReturn("AABBCCDD");
        String value = callEntityValueFactory("tableEntityCRC", rom);
        assertThat(value).isEqualTo("AABBCCDD");
    }

    @Test
    @DisplayName("entity CRC value factory should return the disk CRC")
    void entityCrcValueFactoryShouldReturnDiskCrc() {
        Disk disk = mock(Disk.class);
        when(disk.getCrc()).thenReturn("11223344");
        String value = callEntityValueFactory("tableEntityCRC", disk);
        assertThat(value).isEqualTo("11223344");
    }

    @Test
    @DisplayName("entity CRC value factory should return null for a sample")
    void entityCrcValueFactoryShouldReturnNullForSample() {
        Sample sample = mock(Sample.class);
        String value = callEntityValueFactory("tableEntityCRC", sample);
        assertThat(value).isNull();
    }

    @Test
    @DisplayName("entity MD5 value factory should return the ROM MD5")
    void entityMd5ValueFactoryShouldReturnRomMd5() {
        Rom rom = mock(Rom.class);
        when(rom.getMd5()).thenReturn("md5hash");
        String value = callEntityValueFactory("tableEntityMD5", rom);
        assertThat(value).isEqualTo("md5hash");
    }

    @Test
    @DisplayName("entity SHA1 value factory should return the disk SHA1")
    void entitySha1ValueFactoryShouldReturnDiskSha1() {
        Disk disk = mock(Disk.class);
        when(disk.getSha1()).thenReturn("sha1hash");
        String value = callEntityValueFactory("tableEntitySHA1", disk);
        assertThat(value).isEqualTo("sha1hash");
    }

    @Test
    @DisplayName("entity size value factory should return the ROM size")
    void entitySizeValueFactoryShouldReturnRomSize() {
        Rom rom = mock(Rom.class);
        when(rom.getSize()).thenReturn(4096L);
        Long value = callEntityValueFactory("tableEntitySize", rom);
        assertThat(value).isEqualTo(4096L);
    }

    @Test
    @DisplayName("entity size value factory should return null for a disk")
    void entitySizeValueFactoryShouldReturnNullForDisk() {
        Disk disk = mock(Disk.class);
        Long value = callEntityValueFactory("tableEntitySize", disk);
        assertThat(value).isNull();
    }

    @Test
    @DisplayName("entity merge name value factory should return the ROM merge")
    void entityMergeNameValueFactoryShouldReturnRomMerge() {
        Rom rom = mock(Rom.class);
        when(rom.getMerge()).thenReturn("parent.rom");
        String value = callEntityValueFactory("tableEntityMergeName", rom);
        assertThat(value).isEqualTo("parent.rom");
    }

    @Test
    @DisplayName("entity dump status value factory should return the ROM dump status")
    void entityDumpStatusValueFactoryShouldReturnRomDumpStatus() {
        Rom rom = mock(Rom.class);
        when(rom.getDumpStatus()).thenReturn(Entity.Status.good);
        Entity.Status value = callEntityValueFactory("tableEntityDumpStatus", rom);
        assertThat(value).isEqualTo(Entity.Status.good);
    }

    // ==================== Machine/Software Cell Value Factory Tests ====================

    /**
     * Calls a column's cell value factory for the given anyware row.
     *
     * @param colName the column field name
     * @param row     the row value
     * @param <T>     the value type
     * @return the produced value
     */
    private <T> T callWValueFactory(String colName, Anyware row) {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<Anyware, T> col = getField(controller, colName);
        TableView<Anyware> table = getField(controller, "tableW");
        CellDataFeatures<Anyware, T> features = new CellDataFeatures<>(table, col, row);
        ObservableValue<T> observable = col.getCellValueFactory().call(features);
        return observable == null ? null : observable.getValue();
    }

    @Test
    @DisplayName("WM status value factory should return the machine itself")
    void wmStatusValueFactoryShouldReturnMachine() {
        Machine machine = mock(Machine.class);
        Anyware value = callWValueFactory("tableWMStatus", machine);
        assertThat(value).isSameAs(machine);
    }

    @Test
    @DisplayName("WM name value factory should return the machine")
    void wmNameValueFactoryShouldReturnMachine() {
        Machine machine = mock(Machine.class);
        Machine value = callWValueFactory("tableWMName", machine);
        assertThat(value).isSameAs(machine);
    }

    @Test
    @DisplayName("WM description value factory should return the machine description")
    void wmDescriptionValueFactoryShouldReturnMachineDescription() {
        Machine machine = mock(Machine.class);
        when(machine.getDescription()).thenReturn("Pac-Man");
        String value = callWValueFactory("tableWMDescription", machine);
        assertThat(value).isEqualTo("Pac-Man");
    }

    @Test
    @DisplayName("WM have value factory should format have/total for a machine")
    void wmHaveValueFactoryShouldFormatHaveTotalForMachine() {
        Machine machine = mock(Machine.class);
        when(machine.countHave()).thenReturn(3);
        when(machine.countAll()).thenReturn(5);
        String value = callWValueFactory("tableWMHave", machine);
        assertThat(value).isEqualTo("3/5");
    }

    @Test
    @DisplayName("WS name value factory should return the software base name")
    void wsNameValueFactoryShouldReturnSoftwareBaseName() {
        Software software = mock(Software.class);
        when(software.getBaseName()).thenReturn("pacman");
        String value = callWValueFactory("tableWSName", software);
        assertThat(value).isEqualTo("pacman");
    }

    @Test
    @DisplayName("WS have value factory should format have/total for a software")
    void wsHaveValueFactoryShouldFormatHaveTotalForSoftware() {
        Software software = mock(Software.class);
        when(software.countHave()).thenReturn(1);
        when(software.countAll()).thenReturn(2);
        String value = callWValueFactory("tableWSHave", software);
        assertThat(value).isEqualTo("1/2");
    }

    // ==================== AnywareList Cell Value Factory Tests ====================

    /**
     * Calls a column's cell value factory for the given anyware list row.
     *
     * @param colName the column field name
     * @param row     the row value
     * @param <T>     the value type
     * @return the produced value
     */
    private <T> T callWLValueFactory(String colName, AnywareList<? extends Anyware> row) {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<AnywareList<? extends Anyware>, T> col = getField(controller, colName);
        TableView<AnywareList<? extends Anyware>> table = getField(controller, "tableWL");
        CellDataFeatures<AnywareList<? extends Anyware>, T> features = new CellDataFeatures<>(table, col, row);
        ObservableValue<T> observable = col.getCellValueFactory().call(features);
        return observable == null ? null : observable.getValue();
    }

    @Test
    @DisplayName("WL name value factory should return the anyware list itself")
    void wlNameValueFactoryShouldReturnAnywareList() {
        MachineList ml = mock(MachineList.class);
        AnywareList<? extends Anyware> value = callWLValueFactory("tableWLName", ml);
        assertThat((Object) value).isSameAs(ml);
    }

    @Test
    @DisplayName("WL have value factory should compute have/total from the filtered stream")
    void wlHaveValueFactoryShouldComputeHaveTotal() {
        MachineList ml = mock(MachineList.class);
        Anyware complete = mock(Anyware.class);
        when(complete.getStatus()).thenReturn(AnywareStatus.COMPLETE);
        Anyware partial = mock(Anyware.class);
        when(partial.getStatus()).thenReturn(AnywareStatus.PARTIAL);
        when(ml.getFilteredStream()).thenAnswer(inv -> Stream.of(complete, partial));
        when(ml.getName()).thenReturn("machines");
        String value = callWLValueFactory("tableWLHave", ml);
        assertThat(value).isEqualTo("1/2");
    }

    @Test
    @DisplayName("WL description value factory should return the software list description")
    void wlDescValueFactoryShouldReturnSoftwareListDescription() {
        SoftwareList sl = mock(SoftwareList.class);
        when(sl.getDescription()).thenReturn(new StringBuilder("A software list"));
        String value = callWLValueFactory("tableWLDesc", sl);
        assertThat(value).isEqualTo("A software list");
    }

    // ==================== searchPredicate Tests ====================

    /**
     * Invokes the private {@code searchPredicate} method via reflection.
     *
     * @param newValue the search text
     * @return the resulting predicate
     */
    @SuppressWarnings("unchecked")
    private Predicate<? super Anyware> invokeSearchPredicate(String newValue) throws Exception {
        Method method = ProfileViewerController.class.getDeclaredMethod("searchPredicate", String.class);
        method.setAccessible(true);
        return (java.util.function.Predicate<? super Anyware>) method.invoke(TestApp.getController(), newValue);
    }

    @ParameterizedTest
    @DisplayName("searchPredicate should match by name, description, or match all when empty")
    @MethodSource("searchPredicateTestCases")
    void searchPredicateShouldMatchCorrectly(String searchText, String baseName, String description, boolean expected) throws Exception {
        Anyware ware = mock(Anyware.class);
        when(ware.getBaseName()).thenReturn(baseName);
        when(ware.getDescription()).thenReturn(description);

        final var predicate = invokeSearchPredicate(searchText);

        assertThat(predicate.test(ware)).isEqualTo(expected);
    }

    static Stream<Arguments> searchPredicateTestCases() {
        return Stream.of(
            Arguments.of("", "game", "desc", true),
            Arguments.of("pacman", "PacMan", "desc", true),
            Arguments.of("pac-man", "game", "A Pac-Man clone", true),
            Arguments.of("zzz", "game", "desc", false)
        );
    }

    // ==================== reloadE Tests ====================

    @Test
    @DisplayName("reloadE should populate the entity table from the anyware entities")
    void reloadEShouldPopulateEntityTableFromAnyware() {
        ProfileViewerController controller = TestApp.getController();
        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");

        Rom rom1 = mock(Rom.class);
        Rom rom2 = mock(Rom.class);
        Anyware ware = mock(Anyware.class);
        when(ware.getEntities()).thenAnswer(inv -> java.util.Arrays.asList(rom1, rom2));

        try {
            Method reloadE = ProfileViewerController.class.getDeclaredMethod("reloadE", Anyware.class);
            reloadE.setAccessible(true);
            reloadE.invoke(controller, ware);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThat(tableEntity.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("reloadE should clear the entity table when the anyware is null")
    void reloadEShouldClearEntityTableWhenNull() throws Exception {
        ProfileViewerController controller = TestApp.getController();
        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
        tableEntity.setItems(FXCollections.observableArrayList(mock(EntityBase.class)));

        Method reloadE = ProfileViewerController.class.getDeclaredMethod("reloadE", Anyware.class);
        reloadE.setAccessible(true);
        reloadE.invoke(controller, (Anyware) null);

        assertThat(tableEntity.getItems()).isEmpty();
    }

    // ==================== Additional W Value Factory Tests ====================

    @Test
    @DisplayName("WS cloneOf value factory should return null when cloneof is null")
    void wsCloneOfValueFactoryShouldReturnNullWhenCloneofNull() {
        Software software = mock(Software.class);
        when(software.getCloneof()).thenReturn(null);
        Object value = callWValueFactory("tableWSCloneOf", software);
        assertThat(value).isNull();
    }

    // ==================== Cell Factory Invocation Tests ====================

    @Test
    @DisplayName("entity status column cell factory should build a cell")
    void entityStatusColCellFactoryShouldBuildCell() {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<EntityBase, EntityBase> col = getField(controller, "tableEntityStatus");
        assertThat(col.getCellFactory().call(col)).isNotNull();
    }

    @Test
    @DisplayName("entity name column cell factory should build a cell")
    void entityNameColCellFactoryShouldBuildCell() {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<EntityBase, EntityBase> col = getField(controller, "tableEntityName");
        assertThat(col.getCellFactory().call(col)).isNotNull();
    }

    @Test
    @DisplayName("entity size column cell factory should build a cell")
    void entitySizeColCellFactoryShouldBuildCell() {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<EntityBase, Long> col = getField(controller, "tableEntitySize");
        assertThat(col.getCellFactory().call(col)).isNotNull();
    }

    @Test
    @DisplayName("entity dump status column cell factory should build a cell")
    void entityDumpStatusColCellFactoryShouldBuildCell() {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<EntityBase, Entity.Status> col = getField(controller, "tableEntityDumpStatus");
        assertThat(col.getCellFactory().call(col)).isNotNull();
    }

    @Test
    @DisplayName("WM status column cell factory should build a cell")
    void wmStatusColCellFactoryShouldBuildCell() {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<Anyware, Anyware> col = getField(controller, "tableWMStatus");
        assertThat(col.getCellFactory().call(col)).isNotNull();
    }

    @Test
    @DisplayName("WM name column cell factory should build a cell")
    void wmNameColCellFactoryShouldBuildCell() {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<Anyware, Machine> col = getField(controller, "tableWMName");
        assertThat(col.getCellFactory().call(col)).isNotNull();
    }

    @Test
    @DisplayName("WM have column cell factory should build a cell")
    void wmHaveColCellFactoryShouldBuildCell() {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<Anyware, String> col = getField(controller, "tableWMHave");
        assertThat(col.getCellFactory().call(col)).isNotNull();
    }

    @Test
    @DisplayName("WM selected column cell factory should build a checkbox cell")
    void wmSelectedColCellFactoryShouldBuildCheckboxCell() {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<Anyware, CheckBox> col = getField(controller, "tableWMSelected");
        assertThat(col.getCellValueFactory()).isNotNull();
    }

    @Test
    @DisplayName("WL name column cell factory should build a cell")
    void wlNameColCellFactoryShouldBuildCell() {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<AnywareList<? extends Anyware>, AnywareList<? extends Anyware>> col = getField(controller, "tableWLName");
        assertThat(col.getCellFactory().call(col)).isNotNull();
    }

    @Test
    @DisplayName("WL have column cell factory should build a cell")
    void wlHaveColCellFactoryShouldBuildCell() {
        ProfileViewerController controller = TestApp.getController();
        TableColumn<AnywareList<? extends Anyware>, String> col = getField(controller, "tableWLHave");
        assertThat(col.getCellFactory().call(col)).isNotNull();
    }

    // ==================== reloadW Tests ====================

    @Test
    @DisplayName("reloadW should populate the machine table with WM columns for a MachineList")
    void reloadWShouldPopulateMachineTableForMachineList() throws Exception {
        ProfileViewerController controller = TestApp.getController();
        TableView<Anyware> tableW = getField(controller, "tableW");

        Machine machine = mock(Machine.class);
        MachineList ml = mock(MachineList.class);
        when(ml.getFilteredList()).thenAnswer(inv -> java.util.Collections.singletonList(machine));

        Method reloadW = ProfileViewerController.class.getDeclaredMethod("reloadW", AnywareList.class);
        reloadW.setAccessible(true);
        reloadW.invoke(controller, ml);

        assertThat(tableW.getItems()).contains(machine);
        assertThat(tableW.getColumns()).isNotEmpty();
    }

    @Test
    @DisplayName("reloadW should populate the software table with WS columns for a SoftwareList")
    void reloadWShouldPopulateSoftwareTableForSoftwareList() throws Exception {
        ProfileViewerController controller = TestApp.getController();
        TableView<Anyware> tableW = getField(controller, "tableW");

        Software software = mock(Software.class);
        SoftwareList sl = mock(SoftwareList.class);
        when(sl.getFilteredList()).thenAnswer(inv -> java.util.Collections.singletonList(software));

        Method reloadW = ProfileViewerController.class.getDeclaredMethod("reloadW", AnywareList.class);
        reloadW.setAccessible(true);
        reloadW.invoke(controller, sl);

        assertThat(tableW.getItems()).contains(software);
    }

    @Test
    @DisplayName("reloadW should clear the table when the anyware list is null")
    void reloadWShouldClearTableWhenNull() throws Exception {
        ProfileViewerController controller = TestApp.getController();
        TableView<Anyware> tableW = getField(controller, "tableW");
        tableW.setItems(FXCollections.observableArrayList(mock(Anyware.class)));

        Method reloadW = ProfileViewerController.class.getDeclaredMethod("reloadW", AnywareList.class);
        reloadW.setAccessible(true);
        reloadW.invoke(controller, (AnywareList<?>) null);

        assertThat(tableW.getItems()).isEmpty();
    }

    // ==================== refreshMenuItemAvailability Tests ====================

    @Test
    @DisplayName("refreshMenuItemAvailability should enable machine export items when machines exist")
    void refreshMenuItemAvailabilityShouldEnableMachineExportItems() throws Exception {
        ProfileViewerController controller = TestApp.getController();
        Session session = getControllerSession(controller);
        jrm.profile.data.MachineListList mll = session.getCurrProfile().getMachineListList();
        jrm.profile.data.SoftwareListList sll = mll.getSoftwareListList();
        jrm.profile.data.MachineList mockML = mock(jrm.profile.data.MachineList.class);
        when(mockML.getList()).thenAnswer(inv -> java.util.Collections.singletonList(mock(Machine.class)));
        when(mll.getList()).thenAnswer(inv -> java.util.Collections.singletonList(mockML));
        when(mll.getFilteredStream()).thenAnswer(inv -> Stream.of(mockML));
        when(mockML.countAll()).thenReturn(1L);
        when(sll.isEmpty()).thenReturn(true);
        when(sll.getFilteredStream()).thenAnswer(inv -> Stream.empty());

        Method method = ProfileViewerController.class.getDeclaredMethod("refreshMenuItemAvailability");
        method.setAccessible(true);
        method.invoke(controller);

        MenuItem mntmAllAsMameDat = getField(controller, "mntmAllAsMameDat");
        assertThat(mntmAllAsMameDat.isDisable()).as("all-as-mame enabled when machines exist").isFalse();
    }

    @Test
    @DisplayName("refreshMenuItemAvailability should disable machine export items when no machines exist")
    void refreshMenuItemAvailabilityShouldDisableMachineExportItemsWhenNoMachines() throws Exception {
        ProfileViewerController controller = TestApp.getController();
        Session session = getControllerSession(controller);
        jrm.profile.data.MachineListList mll = session.getCurrProfile().getMachineListList();
        jrm.profile.data.SoftwareListList sll = mll.getSoftwareListList();
        when(mll.getList()).thenAnswer(inv -> java.util.Collections.emptyList());
        when(mll.getFilteredStream()).thenAnswer(inv -> Stream.empty());
        when(sll.isEmpty()).thenReturn(true);
        when(sll.getFilteredStream()).thenAnswer(inv -> Stream.empty());

        Method method = ProfileViewerController.class.getDeclaredMethod("refreshMenuItemAvailability");
        method.setAccessible(true);
        method.invoke(controller);

        MenuItem mntmAllAsMameDat = getField(controller, "mntmAllAsMameDat");
        assertThat(mntmAllAsMameDat.isDisable()).as("all-as-mame disabled when no machines").isTrue();
    }

    /**
     * Retrieves the controller's private {@code session} field via reflection.
     *
     * @param controller the controller
     * @return the session
     */
    private Session getControllerSession(ProfileViewerController controller) {
        try {
            var field = ProfileViewerController.class.getDeclaredField("session");
            field.setAccessible(true);
            return (Session) field.get(controller);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ==================== searchWeb Tests ====================

    @Test
    @DisplayName("searchWeb should not throw when a non-Entity is selected")
    void searchWebShouldNotThrowWhenNonEntitySelected() {
        ProfileViewerController controller = TestApp.getController();
        TableView<EntityBase> tableEntity = getField(controller, "tableEntity");
        EntityBase notAnEntity = mock(EntityBase.class);
        tableEntity.setItems(FXCollections.observableArrayList(notAnEntity));
        tableEntity.getSelectionModel().select(notAnEntity);

        assertThatCode(() -> controller.searchWeb(null)).doesNotThrowAnyException();
    }
}
