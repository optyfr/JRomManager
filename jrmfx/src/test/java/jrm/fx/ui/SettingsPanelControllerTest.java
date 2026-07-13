package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jrm.compressors.SevenZipOptions;
import jrm.compressors.ZipLevel;
import jrm.compressors.ZipTempThreshold;
import jrm.misc.GlobalSettings;
import jrm.misc.ProfileSettingsEnum;
import jrm.misc.SettingsEnum;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.security.User;

/**
 * Tests for {@link SettingsPanelController}.
 * <p>
 * Verifies the {@code initialize} method sets up general, compressor, and debug
 * settings sections with proper icons, choice boxes, and event handlers.
 *
 * @since 3.0.5
 */
@TestFxApplication(SettingsPanelControllerTest.TestApp.class)
@DisplayName("SettingsPanelController Tests")
class SettingsPanelControllerTest {

    /**
     * Minimal application that creates a {@link SettingsPanelController}
     * with all FXML fields injected via reflection and mocks for Sessions.
     */
    public static class TestApp extends Application implements TestFxRecordedStage {
        private Stage primaryStage;
        private static SettingsPanelController controller;
        private static MockedStatic<Sessions> sessionsMock;
        private static Map<Object, Object> settingsMap = new HashMap<>();

        @Override
        public void start(Stage primaryStage) throws Exception {
            this.primaryStage = primaryStage;
            
            // Mock Sessions.getSingleSession() with proper method stubs
            Session mockSession = mock(Session.class);
            User mockUser = mock(User.class);
            GlobalSettings mockSettings = mock(GlobalSettings.class);
            
            // Mock the session methods that GlobalSettings depends on
            when(mockSession.isServer()).thenReturn(false);
            when(mockSession.isMultiuser()).thenReturn(false);
            when(mockUser.getSession()).thenReturn(mockSession);
            when(mockUser.getSettings()).thenReturn(mockSettings);
            when(mockSession.getUser()).thenReturn(mockUser);
            
            // Setup settings map with default values
            settingsMap.clear();
            settingsMap.put(SettingsEnum.thread_count, 2);
            settingsMap.put(ProfileSettingsEnum.backup_dest_dir_enabled, false);
            settingsMap.put(ProfileSettingsEnum.backup_dest_dir, "/tmp/backup");
            settingsMap.put(JRMScene.ScenePrefs.style_sheet, JRMScene.StyleSheet.XL);
            settingsMap.put(SettingsEnum.zip_temp_threshold, ZipTempThreshold._5MB);
            settingsMap.put(SettingsEnum.zip_compression_level, ZipLevel.NORMAL);
            settingsMap.put(SettingsEnum.sevenzip_level, SevenZipOptions.NORMAL);
            settingsMap.put(SettingsEnum.sevenzip_threads, 2);
            settingsMap.put(SettingsEnum.sevenzip_solid, false);
            settingsMap.put(SettingsEnum.debug_level, Level.INFO.toString());
            
            // Mock getProperty calls
            when(mockSettings.getProperty(eq(SettingsEnum.thread_count), eq(Integer.class))) /* NOSONAR */
                .thenAnswer(inv -> settingsMap.get(SettingsEnum.thread_count));
            when(mockSettings.getProperty(eq(ProfileSettingsEnum.backup_dest_dir_enabled), eq(Boolean.class))) /* NOSONAR */
                .thenAnswer(inv -> settingsMap.get(ProfileSettingsEnum.backup_dest_dir_enabled));
            when(mockSettings.getProperty((ProfileSettingsEnum.backup_dest_dir)))
                .thenAnswer(inv -> settingsMap.get(ProfileSettingsEnum.backup_dest_dir));
            when(mockSettings.getEnumProperty(eq(JRMScene.ScenePrefs.style_sheet), eq(JRMScene.StyleSheet.class))) /* NOSONAR */
                .thenAnswer(inv -> settingsMap.get(JRMScene.ScenePrefs.style_sheet));
            when(mockSettings.getEnumProperty(eq(SettingsEnum.zip_temp_threshold), eq(ZipTempThreshold.class))) /* NOSONAR */
                .thenAnswer(inv -> settingsMap.get(SettingsEnum.zip_temp_threshold));
            when(mockSettings.getEnumProperty(eq(SettingsEnum.zip_compression_level), eq(ZipLevel.class))) /* NOSONAR */
                .thenAnswer(inv -> settingsMap.get(SettingsEnum.zip_compression_level));
            when(mockSettings.getEnumProperty(eq(SettingsEnum.sevenzip_level), eq(SevenZipOptions.class))) /* NOSONAR */
                .thenAnswer(inv -> settingsMap.get(SettingsEnum.sevenzip_level));
            when(mockSettings.getProperty(eq(SettingsEnum.sevenzip_threads), eq(Integer.class))) /* NOSONAR */
                .thenAnswer(inv -> settingsMap.get(SettingsEnum.sevenzip_threads));
            when(mockSettings.getProperty(eq(SettingsEnum.sevenzip_solid), eq(Boolean.class))) /* NOSONAR */
                .thenAnswer(inv -> settingsMap.get(SettingsEnum.sevenzip_solid));
            when(mockSettings.getProperty((SettingsEnum.debug_level)))
                .thenAnswer(inv -> settingsMap.get(SettingsEnum.debug_level));
            
            // Mock setProperty calls
            doAnswer(inv -> {
                settingsMap.put(inv.getArgument(0), inv.getArgument(1));
                return null;
            }).when(mockSettings).setProperty(any(SettingsEnum.class), any());
            doAnswer(inv -> {
                settingsMap.put(inv.getArgument(0), inv.getArgument(1));
                return null;
            }).when(mockSettings).setProperty(any(ProfileSettingsEnum.class), anyBoolean());
            doAnswer(inv -> {
                settingsMap.put(inv.getArgument(0), inv.getArgument(1));
                return null;
            }).when(mockSettings).setProperty(any(ProfileSettingsEnum.class), anyString());
            
            // Use shared mock to avoid thread conflicts
            SharedMockSession.getOrCreate().when(Sessions::getSingleSession).thenReturn(mockSession);
            
            controller = new SettingsPanelController();
            
            // Inject FXML fields via reflection
            injectField(controller, "paneGeneral", new TitledPane());
            injectField(controller, "cbThreading", new ChoiceBox<>());
            injectField(controller, "cbStyleSheet", new ChoiceBox<>());
            injectField(controller, "ckbBackupDst", new CheckBox());
            injectField(controller, "tfBackupDst", new TextField());
            injectField(controller, "btBackupDst", new Button());
            
            injectField(controller, "paneCompressors", new TitledPane());
            injectField(controller, "cbZipTempThreshold", new ChoiceBox<>());
            injectField(controller, "cbZipLevel", new ChoiceBox<>());
            injectField(controller, "cb7zArgs", new ChoiceBox<>());
            injectField(controller, "tf7zThreads", new Spinner<>());
            injectField(controller, "ckb7ZSolid", new CheckBox());
            
            injectField(controller, "paneDebug", new TitledPane());
            injectField(controller, "cbDbgLevel", new ChoiceBox<>());
            injectField(controller, "gc", new Button());
            injectField(controller, "status", new TextField());
            
            // Initialize the controller
            controller.initialize(null, null);
            
            VBox root = new VBox();
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        private static void injectField(Object target, String fieldName, Object value) {
            try {
                var field = SettingsPanelController.class.getDeclaredField(fieldName);
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
        public static SettingsPanelController getController() {
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
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.paneGeneral).as("paneGeneral").isNotNull();
        assertThat(controller.cbThreading).as("cbThreading").isNotNull();
        assertThat(controller.cbStyleSheet).as("cbStyleSheet").isNotNull();
        assertThat(controller.ckbBackupDst).as("ckbBackupDst").isNotNull();
        assertThat(controller.tfBackupDst).as("tfBackupDst").isNotNull();
        assertThat(controller.btBackupDst).as("btBackupDst").isNotNull();
        
        assertThat(controller.paneCompressors).as("paneCompressors").isNotNull();
        assertThat(controller.cbZipTempThreshold).as("cbZipTempThreshold").isNotNull();
        assertThat(controller.cbZipLevel).as("cbZipLevel").isNotNull();
        assertThat(controller.cb7zArgs).as("cb7zArgs").isNotNull();
        assertThat(controller.tf7zThreads).as("tf7zThreads").isNotNull();
        assertThat(controller.ckb7ZSolid).as("ckb7ZSolid").isNotNull();
        
        assertThat(controller.paneDebug).as("paneDebug").isNotNull();
        assertThat(controller.cbDbgLevel).as("cbDbgLevel").isNotNull();
        assertThat(controller.gc).as("gc").isNotNull();
        assertThat(controller.status).as("status").isNotNull();
    }

    @Test
    @DisplayName("Should initialize general settings with icon")
    void shouldInitializeGeneralSettings(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.paneGeneral.getGraphic()).as("paneGeneral icon").isNotNull();
        assertThat(controller.btBackupDst.getGraphic()).as("btBackupDst icon").isNotNull();
    }

    @Test
    @DisplayName("Should populate threading choices")
    void shouldPopulateThreadingChoices(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.cbThreading.getItems()).as("cbThreading items count").hasSizeGreaterThan(0);
        assertThat(controller.cbThreading.getSelectionModel().getSelectedIndex()).as("cbThreading selection index").isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should populate stylesheet choices")
    void shouldPopulateStyleSheetChoices(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.cbStyleSheet.getItems()).as("cbStyleSheet items").isNotEmpty();
        assertThat(controller.cbStyleSheet.getSelectionModel().getSelectedItem()).as("cbStyleSheet selection").isNotNull();
    }

    @Test
    @DisplayName("Should initialize compressor settings with icon")
    void shouldInitializeCompressorSettings(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.paneCompressors.getGraphic()).as("paneCompressors icon").isNotNull();
    }

    @Test
    @DisplayName("Should populate zip temp threshold choices")
    void shouldPopulateZipTempThresholdChoices(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.cbZipTempThreshold.getItems()).as("cbZipTempThreshold items").isNotEmpty();
        assertThat(controller.cbZipTempThreshold.getSelectionModel().getSelectedItem()).as("cbZipTempThreshold selection").isNotNull();
    }

    @Test
    @DisplayName("Should populate zip level choices")
    void shouldPopulateZipLevelChoices(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.cbZipLevel.getItems()).as("cbZipLevel items").isNotEmpty();
        assertThat(controller.cbZipLevel.getSelectionModel().getSelectedItem()).as("cbZipLevel selection").isNotNull();
    }

    @Test
    @DisplayName("Should populate 7z args choices")
    void shouldPopulate7zArgsChoices(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.cb7zArgs.getItems()).as("cb7zArgs items").isNotEmpty();
        assertThat(controller.cb7zArgs.getSelectionModel().getSelectedItem()).as("cb7zArgs selection").isNotNull();
    }

    @Test
    @DisplayName("Should initialize 7z threads spinner")
    void shouldInitialize7zThreadsSpinner(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.tf7zThreads.getValueFactory()).as("tf7zThreads value factory").isNotNull();
        assertThat(controller.tf7zThreads.getValue()).as("tf7zThreads value").isNotNull();
    }

    @Test
    @DisplayName("Should initialize debug settings with icon")
    void shouldInitializeDebugSettings(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.paneDebug.getGraphic()).as("paneDebug icon").isNotNull();
    }

    @Test
    @DisplayName("Should populate debug level choices")
    void shouldPopulateDebugLevelChoices(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.cbDbgLevel.getItems()).as("cbDbgLevel items").isNotEmpty();
        assertThat(controller.cbDbgLevel.getSelectionModel().getSelectedItem()).as("cbDbgLevel selection").isNotNull();
    }

    @Test
    @DisplayName("Should setup gc button handler")
    void shouldSetupGcButtonHandler(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.gc.getOnAction()).as("gc onAction").isNotNull();
    }

    @Test
    @DisplayName("Should update memory status")
    void shouldUpdateMemoryStatus(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        controller.updateMemory();
        
        assertThat(controller.status.getText()).as("status text").isNotEmpty();
        assertThat(controller.status.getText()).as("status text").contains("MiB");
    }

    @Test
    @DisplayName("Should have scheduler initialized")
    void shouldHaveSchedulerInitialized(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        assertThat(controller.scheduler).as("scheduler").isNotNull();
    }

    @Test
    @DisplayName("Should test 7z threads spinner increment")
    void shouldTest7zThreadsSpinnerIncrement(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        var factory = controller.tf7zThreads.getValueFactory();
        int initialValue = factory.getValue();
        
        factory.increment(1);
        int newValue = factory.getValue();
        
        assertThat(newValue).as("incremented value").isGreaterThan(initialValue);
    }

    @Test
    @DisplayName("Should test 7z threads spinner decrement")
    void shouldTest7zThreadsSpinnerDecrement(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        var factory = controller.tf7zThreads.getValueFactory();
        int initialValue = factory.getValue();
        
        factory.decrement(1);
        int newValue = factory.getValue();
        
        assertThat(newValue).as("decremented value").isLessThan(initialValue);
    }

    @Test
    @DisplayName("Should not decrement 7z threads below -1")
    void shouldNotDecrement7zThreadsBelowMinusOne(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        var factory = controller.tf7zThreads.getValueFactory();
        factory.setValue(-1);
        
        factory.decrement(1);
        int newValue = factory.getValue();
        
        assertThat(newValue).as("value after decrement from -1").isEqualTo(-1);
    }

    @Test
    @DisplayName("Should handle checkbox backup destination toggle")
    void shouldHandleCheckboxBackupDstToggle(TestApp application) {
        SettingsPanelController controller = TestApp.getController();
        
        // Toggle the checkbox
        controller.ckbBackupDst.setSelected(true);
        
        // Verify controls are enabled
        assertThat(controller.tfBackupDst.isDisable()).as("tfBackupDst disabled").isFalse();
        assertThat(controller.btBackupDst.isDisable()).as("btBackupDst disabled").isFalse();
        
        // Toggle back
        controller.ckbBackupDst.setSelected(false);
        
        // Verify controls are disabled
        assertThat(controller.tfBackupDst.isDisable()).as("tfBackupDst disabled").isTrue();
        assertThat(controller.btBackupDst.isDisable()).as("btBackupDst disabled").isTrue();
    }
}
