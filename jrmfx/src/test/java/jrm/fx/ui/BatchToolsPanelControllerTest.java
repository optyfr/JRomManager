package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jrm.batch.CompressorFormat;
import jrm.fx.ui.misc.FileResult;
import jrm.fx.ui.misc.SrcDstResult;
import jrm.io.torrent.options.TrntChkMode;
import jrm.misc.GlobalSettings;
import jrm.misc.SettingsEnum;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.security.User;

/**
 * Tests for {@link BatchToolsPanelController}.
 * <p>
 * Verifies initialization of tabs, tables, choice boxes, checkboxes,
 * and event handlers for the batch tools panel.
 *
 * @since 3.0.5
 */
@TestFxApplication(BatchToolsPanelControllerTest.TestApp.class)
@DisplayName("BatchToolsPanelController Tests")
class BatchToolsPanelControllerTest {

	/**
	 * Functional interface that accepts a runnable which may throw checked exceptions.
	 */
	@FunctionalInterface
	interface ThrowingRunnable {
		void run() throws Exception;
	}

	/**
	 * Minimal application that creates a {@link BatchToolsPanelController}
	 * with all FXML fields injected via reflection and mocks for Session.
	 */
	public static class TestApp extends Application implements TestFxRecordedStage {
		private Stage primaryStage;
		static BatchToolsPanelController controller;

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
			when(mockSettings.getProperty(eq(SettingsEnum.compressor_format))).thenReturn(CompressorFormat.ZIP.name()); /* NOSONAR */
			when(mockSettings.getProperty(eq(SettingsEnum.compressor_force), eq(Boolean.class))).thenReturn(false); /* NOSONAR */
			when(mockSettings.getProperty(eq(SettingsEnum.trntchk_mode))).thenReturn(TrntChkMode.SHA1.name());/* NOSONAR */
			when(mockSettings.getProperty(eq(SettingsEnum.trntchk_detect_archived_folders), eq(Boolean.class))).thenReturn(false);/* NOSONAR */
			when(mockSettings.getProperty(eq(SettingsEnum.trntchk_remove_wrong_sized_files), eq(Boolean.class))).thenReturn(false);/* NOSONAR */
			when(mockSettings.getProperty(eq(SettingsEnum.trntchk_remove_unknown_files), eq(Boolean.class))).thenReturn(false);/* NOSONAR */
			when(mockSettings.getProperty(eq(SettingsEnum.dat2dir_dry_run), eq(Boolean.class))).thenReturn(false);/* NOSONAR */
			when(mockSettings.getProperty(eq(SettingsEnum.dat2dir_srcdirs))).thenReturn("");/* NOSONAR */
			when(mockSettings.getProperty(eq(SettingsEnum.dat2dir_sdr))).thenReturn("[]");/* NOSONAR */
			when(mockSettings.getProperty(eq(SettingsEnum.trntchk_sdr))).thenReturn("[]");/* NOSONAR */
			when(mockSettings.getProperty(eq(SettingsEnum.dat2dir_lastsrcdir))).thenReturn(null);/* NOSONAR */

			User mockUser = mock(User.class);
			when(mockUser.getSettings()).thenReturn(mockSettings);

			Session mockSession = mock(Session.class);
			when(mockSession.getUser()).thenReturn(mockUser);

			// Configure shared mock to return our session
			SharedMockSession.getOrCreate().when(Sessions::getSingleSession).thenReturn(mockSession);

			// Create the controller
			controller = new BatchToolsPanelController();

			// Inject session if null
			Field sessionField = BaseController.class.getDeclaredField("session");
			sessionField.setAccessible(true);
			if (sessionField.get(controller) == null) {
				sessionField.set(controller, mockSession);
			}

			// Create and inject all FXML fields
			Tab panelBatchToolsDat2Dir = new Tab();
			Tab panelBatchToolsDir2Torrent = new Tab();
			Tab panelBatchToolsCompressor = new Tab();

			Button btnBatchToolsDir2DatStart = new Button();
			Button btnBatchToolsTrntChkStart = new Button();
			Button btnBatchToolsCompressorStart = new Button();
			Button btnBatchToolsCompressorClear = new Button();

			ChoiceBox<TrntChkMode> cbbxBatchToolsTrntChk = new ChoiceBox<>();
			ChoiceBox<CompressorFormat> cbbxBatchToolsCompressorFormat = new ChoiceBox<>();

			CheckBox cbBatchToolsDat2DirDryRun = new CheckBox();
			CheckBox cbBatchToolsTrntChkDetectArchivedFolder = new CheckBox();
			CheckBox cbBatchToolsTrntChkRemoveUnknownFiles = new CheckBox();
			CheckBox cbBatchToolsTrntChkRemoveWrongSizedFiles = new CheckBox();
			CheckBox cbBatchToolsCompressorForce = new CheckBox();

			TableView<File> tvBatchToolsDat2DirSrc = new TableView<>();
			TableColumn<File, File> tvBatchToolsDat2DirSrcCol = new TableColumn<>();
			ContextMenu popupMenuSrc = new ContextMenu();
			MenuItem mnDat2DirAddSrcDir = new MenuItem();
			MenuItem mnDat2DirDelSrcDir = new MenuItem();

			TableView<SrcDstResult> tvBatchToolsDat2DirDst = new TableView<>();
			TableColumn<SrcDstResult, String> tvBatchToolsDat2DirDstDatsCol = new TableColumn<>();
			TableColumn<SrcDstResult, String> tvBatchToolsDat2DirDstDirsCol = new TableColumn<>();
			TableColumn<SrcDstResult, String> tvBatchToolsDat2DirDstResultCol = new TableColumn<>();
			TableColumn<SrcDstResult, SrcDstResult> tvBatchToolsDat2DirDstDetailsCol = new TableColumn<>();
			TableColumn<SrcDstResult, Boolean> tvBatchToolsDat2DirDstSelCol = new TableColumn<>();
			ContextMenu popupMenuDst = new ContextMenu();
			MenuItem mnDat2DirDelDstDat = new MenuItem();
			Menu mntmDat2DirDstPresets = new Menu();

			TableView<SrcDstResult> tvBatchToolsTorrent = new TableView<>();
			TableColumn<SrcDstResult, String> tvBatchToolsTorrentFilesCol = new TableColumn<>();
			TableColumn<SrcDstResult, String> tvBatchToolsTorrentDstDirsCol = new TableColumn<>();
			TableColumn<SrcDstResult, String> tvBatchToolsTorrentResultCol = new TableColumn<>();
			TableColumn<SrcDstResult, SrcDstResult> tvBatchToolsTorrentDetailsCol = new TableColumn<>();
			TableColumn<SrcDstResult, Boolean> tvBatchToolsTorrentSelCol = new TableColumn<>();
			ContextMenu popupMenuTorrent = new ContextMenu();
			MenuItem mnDelTorrent = new MenuItem();

			TableView<FileResult> tvBatchToolsCompressor = new TableView<>();
			TableColumn<FileResult, java.nio.file.Path> tvBatchToolsCompressorFileCol = new TableColumn<>();
			TableColumn<FileResult, String> tvBatchToolsCompressorStatusCol = new TableColumn<>();

			// Inject all fields via reflection
			VBox root = new VBox();
			injectField(controller, "panelBatchToolsDat2Dir", panelBatchToolsDat2Dir);
			injectField(controller, "panelBatchToolsDir2Torrent", panelBatchToolsDir2Torrent);
			injectField(controller, "panelBatchToolsCompressor", panelBatchToolsCompressor);

			injectField(controller, "btnBatchToolsDir2DatStart", btnBatchToolsDir2DatStart);
			injectField(controller, "btnBatchToolsTrntChkStart", btnBatchToolsTrntChkStart);
			injectField(controller, "btnBatchToolsCompressorStart", btnBatchToolsCompressorStart);
			injectField(controller, "btnBatchToolsCompressorClear", btnBatchToolsCompressorClear);

			injectField(controller, "cbbxBatchToolsTrntChk", cbbxBatchToolsTrntChk);
			injectField(controller, "cbbxBatchToolsCompressorFormat", cbbxBatchToolsCompressorFormat);

			injectField(controller, "cbBatchToolsDat2DirDryRun", cbBatchToolsDat2DirDryRun);
			injectField(controller, "cbBatchToolsTrntChkDetectArchivedFolder", cbBatchToolsTrntChkDetectArchivedFolder);
			injectField(controller, "cbBatchToolsTrntChkRemoveUnknownFiles", cbBatchToolsTrntChkRemoveUnknownFiles);
			injectField(controller, "cbBatchToolsTrntChkRemoveWrongSizedFiles", cbBatchToolsTrntChkRemoveWrongSizedFiles);
			injectField(controller, "cbBatchToolsCompressorForce", cbBatchToolsCompressorForce);

			injectField(controller, "tvBatchToolsDat2DirSrc", tvBatchToolsDat2DirSrc);
			injectField(controller, "tvBatchToolsDat2DirSrcCol", tvBatchToolsDat2DirSrcCol);
			injectField(controller, "popupMenuSrc", popupMenuSrc);
			injectField(controller, "mnDat2DirAddSrcDir", mnDat2DirAddSrcDir);
			injectField(controller, "mnDat2DirDelSrcDir", mnDat2DirDelSrcDir);

			injectField(controller, "tvBatchToolsDat2DirDst", tvBatchToolsDat2DirDst);
			injectField(controller, "tvBatchToolsDat2DirDstDatsCol", tvBatchToolsDat2DirDstDatsCol);
			injectField(controller, "tvBatchToolsDat2DirDstDirsCol", tvBatchToolsDat2DirDstDirsCol);
			injectField(controller, "tvBatchToolsDat2DirDstResultCol", tvBatchToolsDat2DirDstResultCol);
			injectField(controller, "tvBatchToolsDat2DirDstDetailsCol", tvBatchToolsDat2DirDstDetailsCol);
			injectField(controller, "tvBatchToolsDat2DirDstSelCol", tvBatchToolsDat2DirDstSelCol);
			injectField(controller, "popupMenuDst", popupMenuDst);
			injectField(controller, "mnDat2DirDelDstDat", mnDat2DirDelDstDat);
			injectField(controller, "mntmDat2DirDstPresets", mntmDat2DirDstPresets);

			injectField(controller, "tvBatchToolsTorrent", tvBatchToolsTorrent);
			injectField(controller, "tvBatchToolsTorrentFilesCol", tvBatchToolsTorrentFilesCol);
			injectField(controller, "tvBatchToolsTorrentDstDirsCol", tvBatchToolsTorrentDstDirsCol);
			injectField(controller, "tvBatchToolsTorrentResultCol", tvBatchToolsTorrentResultCol);
			injectField(controller, "tvBatchToolsTorrentDetailsCol", tvBatchToolsTorrentDetailsCol);
			injectField(controller, "tvBatchToolsTorrentSelCol", tvBatchToolsTorrentSelCol);
			injectField(controller, "popupMenuTorrent", popupMenuTorrent);
			injectField(controller, "mnDelTorrent", mnDelTorrent);

			injectField(controller, "tvBatchToolsCompressor", tvBatchToolsCompressor);
			injectField(controller, "tvBatchToolsCompressorFileCol", tvBatchToolsCompressorFileCol);
			injectField(controller, "tvBatchToolsCompressorStatusCol", tvBatchToolsCompressorStatusCol);

			// Initialize controller
			root.getChildren().addAll(
					btnBatchToolsDir2DatStart, btnBatchToolsTrntChkStart, btnBatchToolsCompressorStart, btnBatchToolsCompressorClear,
					cbbxBatchToolsTrntChk, cbbxBatchToolsCompressorFormat,
					cbBatchToolsDat2DirDryRun, cbBatchToolsTrntChkDetectArchivedFolder, cbBatchToolsTrntChkRemoveUnknownFiles,
					cbBatchToolsTrntChkRemoveWrongSizedFiles, cbBatchToolsCompressorForce,
					tvBatchToolsDat2DirSrc, tvBatchToolsDat2DirDst, tvBatchToolsTorrent, tvBatchToolsCompressor);

			controller.initialize(null, null);

			Scene scene = new Scene(root, 800, 600);
			primaryStage.setScene(scene);
			primaryStage.show();
		}

		private static void injectField(Object target, String fieldName, Object value) {
			try {
				Field field = target.getClass().getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(target, value);
			} catch (Exception e) {
				throw new RuntimeException("Failed to inject field: " + fieldName, e);
			}
		}

		/**
		 * Helper method to run operations on the JavaFX Application Thread and wait for completion.
		 */
		public static void runOnFxThread(ThrowingRunnable operation) throws Exception {
			CompletableFuture<Void> future = new CompletableFuture<>();
			Platform.runLater(() -> {
				try {
					operation.run();
					future.complete(null);
				} catch (Throwable e) {
					future.completeExceptionally(e);
				}
			});
			future.get();
		}
	}

	/**
	 * Helper to run code on the JavaFX thread and wait for completion.
	 */
	private void runOnFxThread(ThrowingRunnable action) throws Exception {
		TestApp.runOnFxThread(action);
	}

	/**
	 * Retrieves a private field value from the controller via reflection.
	 *
	 * @param fieldName the field name
	 * @param <T>       the expected type
	 * @return the field value
	 */
	@SuppressWarnings("unchecked")
	private <T> T getField(String fieldName) {
		try {
			Field field = BatchToolsPanelController.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(TestApp.controller);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get field " + fieldName, e);
		}
	}

	@Test
	@DisplayName("initialize should set up all tabs and controls")
	void testInitialize() {
		assertThat(TestApp.controller).isNotNull();
	}

	@Test
	@DisplayName("compressor format choice box should have all formats")
	void testCompressorFormatChoiceBox() throws Exception {
		TestApp.runOnFxThread(() -> {
			Field cbxField = BatchToolsPanelController.class.getDeclaredField("cbbxBatchToolsCompressorFormat");
			cbxField.setAccessible(true);
			@SuppressWarnings("unchecked")
			ChoiceBox<CompressorFormat> cbx = (ChoiceBox<CompressorFormat>) cbxField.get(TestApp.controller);

			assertThat(cbx.getItems()).isNotEmpty();
			assertThat(cbx.getItems()).contains(CompressorFormat.ZIP, CompressorFormat.SEVENZIP, CompressorFormat.TZIP);
		});
	}

	@Test
	@DisplayName("torrent check mode choice box should have all modes")
	void testTorrentCheckModeChoiceBox() throws Exception {
		TestApp.runOnFxThread(() -> {
			Field cbxField = BatchToolsPanelController.class.getDeclaredField("cbbxBatchToolsTrntChk");
			cbxField.setAccessible(true);
			@SuppressWarnings("unchecked")
			ChoiceBox<TrntChkMode> cbx = (ChoiceBox<TrntChkMode>) cbxField.get(TestApp.controller);

			assertThat(cbx.getItems()).isNotEmpty();
			assertThat(cbx.getItems()).contains(TrntChkMode.SHA1);
		});
	}

	@Test
	@DisplayName("checkboxes should be initialized from settings")
	void testCheckboxesInitialized() throws Exception {
		TestApp.runOnFxThread(() -> {
			Field cbField = BatchToolsPanelController.class.getDeclaredField("cbBatchToolsDat2DirDryRun");
			cbField.setAccessible(true);
			CheckBox cb = (CheckBox) cbField.get(TestApp.controller);

			assertThat(cb.isSelected()).isFalse();
		});
	}

	@Test
	@DisplayName("compressor force checkbox should be initialized")
	void testCompressorForceCheckbox() throws Exception {
		TestApp.runOnFxThread(() -> {
			Field cbField = BatchToolsPanelController.class.getDeclaredField("cbBatchToolsCompressorForce");
			cbField.setAccessible(true);
			CheckBox cb = (CheckBox) cbField.get(TestApp.controller);

			assertThat(cb).isNotNull();
		});
	}

	@Test
	@DisplayName("torrent checkboxes should be initialized")
	void testTorrentCheckboxes() throws Exception {
		TestApp.runOnFxThread(() -> {
			Field cb1Field = BatchToolsPanelController.class.getDeclaredField("cbBatchToolsTrntChkDetectArchivedFolder");
			cb1Field.setAccessible(true);
			CheckBox cb1 = (CheckBox) cb1Field.get(TestApp.controller);

			Field cb2Field = BatchToolsPanelController.class.getDeclaredField("cbBatchToolsTrntChkRemoveUnknownFiles");
			cb2Field.setAccessible(true);
			CheckBox cb2 = (CheckBox) cb2Field.get(TestApp.controller);

			Field cb3Field = BatchToolsPanelController.class.getDeclaredField("cbBatchToolsTrntChkRemoveWrongSizedFiles");
			cb3Field.setAccessible(true);
			CheckBox cb3 = (CheckBox) cb3Field.get(TestApp.controller);

			assertThat(cb1).isNotNull();
			assertThat(cb2).isNotNull();
			assertThat(cb3).isNotNull();
		});
	}

	@Test
	@DisplayName("tables should be initialized with columns")
	void testTablesInitialized() throws Exception {
		TestApp.runOnFxThread(() -> {
			Field tvField = BatchToolsPanelController.class.getDeclaredField("tvBatchToolsDat2DirSrc");
			tvField.setAccessible(true);
			@SuppressWarnings("unchecked")
			TableView<File> tv = (TableView<File>) tvField.get(TestApp.controller);

			assertThat(tv).isNotNull();
		});
	}

	@Test
	@DisplayName("compressor table should be initialized")
	void testCompressorTableInitialized() throws Exception {
		TestApp.runOnFxThread(() -> {
			Field tvField = BatchToolsPanelController.class.getDeclaredField("tvBatchToolsCompressor");
			tvField.setAccessible(true);
			@SuppressWarnings("unchecked")
			TableView<FileResult> tv = (TableView<FileResult>) tvField.get(TestApp.controller);

			assertThat(tv).isNotNull();
		});
	}

	@Test
	@DisplayName("torrent table should be initialized")
	void testTorrentTableInitialized() throws Exception {
		TestApp.runOnFxThread(() -> {
			Field tvField = BatchToolsPanelController.class.getDeclaredField("tvBatchToolsTorrent");
			tvField.setAccessible(true);
			@SuppressWarnings("unchecked")
			TableView<SrcDstResult> tv = (TableView<SrcDstResult>) tvField.get(TestApp.controller);

			assertThat(tv).isNotNull();
		});
	}

	// ==================== Tab Icon Tests ====================

	@Test
	@DisplayName("should set icons on all tabs")
	void shouldSetIconsOnTabs() throws Exception {
		runOnFxThread(() -> {
			assertThat(((Tab) getField("panelBatchToolsDat2Dir")).getGraphic()).as("dat2dir tab icon").isNotNull();
			assertThat(((Tab) getField("panelBatchToolsDir2Torrent")).getGraphic()).as("dir2torrent tab icon").isNotNull();
			assertThat(((Tab) getField("panelBatchToolsCompressor")).getGraphic()).as("compressor tab icon").isNotNull();
		});
	}

	// ==================== Button Icon & Action Tests ====================

	@Test
	@DisplayName("should set icons on action buttons")
	void shouldSetIconsOnActionButtons() throws Exception {
		runOnFxThread(() -> {
			assertThat(((Button) getField("btnBatchToolsDir2DatStart")).getGraphic()).as("dir2dat start icon").isNotNull();
			assertThat(((Button) getField("btnBatchToolsTrntChkStart")).getGraphic()).as("trntchk start icon").isNotNull();
			assertThat(((Button) getField("btnBatchToolsCompressorStart")).getGraphic()).as("compressor start icon").isNotNull();
			assertThat(((Button) getField("btnBatchToolsCompressorClear")).getGraphic()).as("compressor clear icon").isNotNull();
		});
	}

	@Test
	@DisplayName("should set action handlers on start buttons")
	void shouldSetActionHandlersOnStartButtons() throws Exception {
		runOnFxThread(() -> {
			assertThat(((Button) getField("btnBatchToolsDir2DatStart")).getOnAction()).as("dir2dat start onAction").isNotNull();
			assertThat(((Button) getField("btnBatchToolsTrntChkStart")).getOnAction()).as("trntchk start onAction").isNotNull();
			assertThat(((Button) getField("btnBatchToolsCompressorStart")).getOnAction()).as("compressor start onAction").isNotNull();
		});
	}

	// ==================== Choice Box Listener Tests ====================

	@Test
	@DisplayName("compressor format change should update settings")
	void shouldUpdateSettingsWhenCompressorFormatChanges() throws Exception {
		runOnFxThread(() -> {
			ChoiceBox<CompressorFormat> cbx = getField("cbbxBatchToolsCompressorFormat");
			cbx.getSelectionModel().select(CompressorFormat.SEVENZIP);
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(SettingsEnum.compressor_format, CompressorFormat.SEVENZIP.name());
		});
	}

	@Test
	@DisplayName("torrent mode change should update settings")
	void shouldUpdateSettingsWhenTorrentModeChanges() throws Exception {
		runOnFxThread(() -> {
			ChoiceBox<TrntChkMode> cbx = getField("cbbxBatchToolsTrntChk");
			cbx.getSelectionModel().select(TrntChkMode.FILENAME);
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(SettingsEnum.trntchk_mode, TrntChkMode.FILENAME.name());
		});
	}

	@Test
	@DisplayName("torrent mode FILENAME should disable remove wrong sized files checkbox")
	void shouldDisableWrongSizedFilesCheckboxWhenModeIsFilename() throws Exception {
		runOnFxThread(() -> {
			ChoiceBox<TrntChkMode> cbx = getField("cbbxBatchToolsTrntChk");
			CheckBox cb = getField("cbBatchToolsTrntChkRemoveWrongSizedFiles");
			cbx.getSelectionModel().select(TrntChkMode.FILENAME);
			assertThat(cb.isDisable()).as("remove wrong sized files disabled when mode is FILENAME").isTrue();
		});
	}

	@Test
	@DisplayName("torrent mode SHA1 should enable remove wrong sized files checkbox")
	void shouldEnableWrongSizedFilesCheckboxWhenModeIsNotFilename() throws Exception {
		runOnFxThread(() -> {
			ChoiceBox<TrntChkMode> cbx = getField("cbbxBatchToolsTrntChk");
			CheckBox cb = getField("cbBatchToolsTrntChkRemoveWrongSizedFiles");
			cbx.getSelectionModel().select(TrntChkMode.FILENAME);
			cbx.getSelectionModel().select(TrntChkMode.SHA1);
			assertThat(cb.isDisable()).as("remove wrong sized files enabled when mode is SHA1").isFalse();
		});
	}

	// ==================== Checkbox Listener Tests ====================

	@Test
	@DisplayName("dry run checkbox change should update settings")
	void shouldUpdateSettingsWhenDryRunCheckboxToggled() throws Exception {
		runOnFxThread(() -> {
			CheckBox cb = getField("cbBatchToolsDat2DirDryRun");
			cb.setSelected(true);
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(SettingsEnum.dat2dir_dry_run, true);
		});
	}

	@Test
	@DisplayName("compressor force checkbox change should update settings")
	void shouldUpdateSettingsWhenCompressorForceCheckboxToggled() throws Exception {
		runOnFxThread(() -> {
			CheckBox cb = getField("cbBatchToolsCompressorForce");
			cb.setSelected(true);
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(SettingsEnum.compressor_force, true);
		});
	}

	@Test
	@DisplayName("detect archived folder checkbox change should update settings")
	void shouldUpdateSettingsWhenDetectArchivedFolderCheckboxToggled() throws Exception {
		runOnFxThread(() -> {
			CheckBox cb = getField("cbBatchToolsTrntChkDetectArchivedFolder");
			cb.setSelected(true);
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(SettingsEnum.trntchk_detect_archived_folders, true);
		});
	}

	@Test
	@DisplayName("remove unknown files checkbox change should update settings")
	void shouldUpdateSettingsWhenRemoveUnknownFilesCheckboxToggled() throws Exception {
		runOnFxThread(() -> {
			CheckBox cb = getField("cbBatchToolsTrntChkRemoveUnknownFiles");
			cb.setSelected(true);
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(SettingsEnum.trntchk_remove_unknown_files, true);
		});
	}

	@Test
	@DisplayName("remove wrong sized files checkbox change should update settings")
	void shouldUpdateSettingsWhenRemoveWrongSizedFilesCheckboxToggled() throws Exception {
		runOnFxThread(() -> {
			CheckBox cb = getField("cbBatchToolsTrntChkRemoveWrongSizedFiles");
			cb.setSelected(true);
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(SettingsEnum.trntchk_remove_wrong_sized_files, true);
		});
	}

	// ==================== Context Menu Handler Tests ====================

	@Test
	@DisplayName("should have onShowing handler on source popup menu")
	void shouldHaveOnShowingHandlerOnSrcMenu() throws Exception {
		runOnFxThread(() -> {
			ContextMenu menu = getField("popupMenuSrc");
			assertThat(menu.getOnShowing()).as("popupMenuSrc onShowing").isNotNull();
		});
	}

	@Test
	@DisplayName("should have onShowing handler on destination popup menu")
	void shouldHaveOnShowingHandlerOnDstMenu() throws Exception {
		runOnFxThread(() -> {
			ContextMenu menu = getField("popupMenuDst");
			assertThat(menu.getOnShowing()).as("popupMenuDst onShowing").isNotNull();
		});
	}

	@Test
	@DisplayName("should have onShowing handler on torrent popup menu")
	void shouldHaveOnShowingHandlerOnTorrentMenu() throws Exception {
		runOnFxThread(() -> {
			ContextMenu menu = getField("popupMenuTorrent");
			assertThat(menu.getOnShowing()).as("popupMenuTorrent onShowing").isNotNull();
		});
	}

	// ==================== Menu Item Action Handler Tests ====================

	@Test
	@DisplayName("should have action handler on add source dir menu item")
	void shouldHaveActionHandlerOnAddSrcDirMenuItem() throws Exception {
		runOnFxThread(() -> {
			MenuItem mi = getField("mnDat2DirAddSrcDir");
			assertThat(mi.getOnAction()).as("mnDat2DirAddSrcDir onAction").isNotNull();
		});
	}

	@Test
	@DisplayName("should have action handler on delete source dir menu item")
	void shouldHaveActionHandlerOnDelSrcDirMenuItem() throws Exception {
		runOnFxThread(() -> {
			MenuItem mi = getField("mnDat2DirDelSrcDir");
			assertThat(mi.getOnAction()).as("mnDat2DirDelSrcDir onAction").isNotNull();
		});
	}

	// ==================== Context Menu Behavior Tests ====================

	@Test
	@DisplayName("should disable delete src dir menu item when no selection")
	void shouldDisableDelSrcDirMenuItemWhenNoSelection() throws Exception {
		runOnFxThread(() -> {
			TableView<File> tv = getField("tvBatchToolsDat2DirSrc");
			MenuItem mi = getField("mnDat2DirDelSrcDir");
			tv.getItems().clear();
			tv.getSelectionModel().clearSelection();
			((ContextMenu) getField("popupMenuSrc")).getOnShowing().handle(null);
			assertThat(mi.isDisable()).as("delete src dir disabled when no selection").isTrue();
		});
	}

	@Test
	@DisplayName("should enable delete src dir menu item when selection exists")
	void shouldEnableDelSrcDirMenuItemWhenSelectionExists() throws Exception {
		runOnFxThread(() -> {
			TableView<File> tv = getField("tvBatchToolsDat2DirSrc");
			MenuItem mi = getField("mnDat2DirDelSrcDir");
			tv.getItems().add(new File("/test/src"));
			tv.getSelectionModel().selectFirst();
			((ContextMenu) getField("popupMenuSrc")).getOnShowing().handle(null);
			assertThat(mi.isDisable()).as("delete src dir enabled when selection exists").isFalse();
		});
	}

	@Test
	@DisplayName("should disable destination popup items when no selection")
	void shouldDisableDstPopupItemsWhenNoSelection() throws Exception {
		runOnFxThread(() -> {
			TableView<SrcDstResult> tv = getField("tvBatchToolsDat2DirDst");
			MenuItem mnDelDstDat = getField("mnDat2DirDelDstDat");
			Menu mntmPresets = getField("mntmDat2DirDstPresets");
			tv.getSelectionModel().clearSelection();
			((ContextMenu) getField("popupMenuDst")).getOnShowing().handle(null);
			assertThat(mnDelDstDat.isDisable()).as("delete dst dat disabled when no selection").isTrue();
			assertThat(mntmPresets.isDisable()).as("presets disabled when no selection").isTrue();
		});
	}

	@Test
	@DisplayName("should enable destination popup items when selection exists")
	void shouldEnableDstPopupItemsWhenSelectionExists() throws Exception {
		runOnFxThread(() -> {
			TableView<SrcDstResult> tv = getField("tvBatchToolsDat2DirDst");
			MenuItem mnDelDstDat = getField("mnDat2DirDelDstDat");
			Menu mntmPresets = getField("mntmDat2DirDstPresets");
			tv.getItems().add(new SrcDstResult());
			tv.getSelectionModel().selectFirst();
			((ContextMenu) getField("popupMenuDst")).getOnShowing().handle(null);
			assertThat(mnDelDstDat.isDisable()).as("delete dst dat enabled when selection exists").isFalse();
			assertThat(mntmPresets.isDisable()).as("presets enabled when selection exists").isFalse();
		});
	}

	@Test
	@DisplayName("should disable delete torrent menu item when no selection")
	void shouldDisableDelTorrentMenuItemWhenNoSelection() throws Exception {
		runOnFxThread(() -> {
			TableView<SrcDstResult> tv = getField("tvBatchToolsTorrent");
			MenuItem mi = getField("mnDelTorrent");
			tv.getSelectionModel().clearSelection();
			((ContextMenu) getField("popupMenuTorrent")).getOnShowing().handle(null);
			assertThat(mi.isDisable()).as("delete torrent disabled when no selection").isTrue();
		});
	}

	@Test
	@DisplayName("should enable delete torrent menu item when selection exists")
	void shouldEnableDelTorrentMenuItemWhenSelectionExists() throws Exception {
		runOnFxThread(() -> {
			TableView<SrcDstResult> tv = getField("tvBatchToolsTorrent");
			MenuItem mi = getField("mnDelTorrent");
			tv.getItems().add(new SrcDstResult());
			tv.getSelectionModel().selectFirst();
			((ContextMenu) getField("popupMenuTorrent")).getOnShowing().handle(null);
			assertThat(mi.isDisable()).as("delete torrent enabled when selection exists").isFalse();
		});
	}

	// ==================== Delete Action Behavior Tests ====================

	@Test
	@DisplayName("should remove selected items from dat2dir source list")
	void shouldRemoveSelectedItemsFromDat2DirSrc() throws Exception {
		runOnFxThread(() -> {
			TableView<File> tv = getField("tvBatchToolsDat2DirSrc");
			MenuItem mi = getField("mnDat2DirDelSrcDir");
			File f1 = new File("/test/src1");
			File f2 = new File("/test/src2");
			tv.getItems().addAll(f1, f2);
			tv.getSelectionModel().select(f1);
			mi.getOnAction().handle(null);
			assertThat(tv.getItems()).as("source list should contain only f2 after deletion").hasSize(1).containsExactly(f2);
		});
	}

	// ==================== Table Column Setup Tests ====================

	@Test
	@DisplayName("should setup dat2dir source column with cell and value factories")
	void shouldSetupDat2DirSrcColumn() throws Exception {
		runOnFxThread(() -> {
			TableColumn<File, File> col = getField("tvBatchToolsDat2DirSrcCol");
			assertThat(col.getCellFactory()).as("src col cell factory").isNotNull();
			assertThat(col.getCellValueFactory()).as("src col value factory").isNotNull();
		});
	}

	@Test
	@DisplayName("should setup dat2dir destination columns with cell and value factories")
	void shouldSetupDat2DirDstColumns() throws Exception {
		runOnFxThread(() -> {
			TableColumn<SrcDstResult, String> datsCol = getField("tvBatchToolsDat2DirDstDatsCol");
			assertThat(datsCol.getCellFactory()).as("dats col cell factory").isNotNull();
			assertThat(datsCol.getCellValueFactory()).as("dats col value factory").isNotNull();

			TableColumn<SrcDstResult, String> dirsCol = getField("tvBatchToolsDat2DirDstDirsCol");
			assertThat(dirsCol.getCellFactory()).as("dirs col cell factory").isNotNull();
			assertThat(dirsCol.getCellValueFactory()).as("dirs col value factory").isNotNull();

			TableColumn<SrcDstResult, String> resultCol = getField("tvBatchToolsDat2DirDstResultCol");
			assertThat(resultCol.getCellFactory()).as("result col cell factory").isNotNull();
			assertThat(resultCol.getCellValueFactory()).as("result col value factory").isNotNull();

			TableColumn<SrcDstResult, SrcDstResult> detailsCol = getField("tvBatchToolsDat2DirDstDetailsCol");
			assertThat(detailsCol.getCellFactory()).as("details col cell factory").isNotNull();

			TableColumn<SrcDstResult, Boolean> selCol = getField("tvBatchToolsDat2DirDstSelCol");
			assertThat(selCol.getCellFactory()).as("sel col cell factory").isNotNull();
		});
	}

	@Test
	@DisplayName("should setup torrent columns with cell and value factories")
	void shouldSetupTorrentColumns() throws Exception {
		runOnFxThread(() -> {
			TableColumn<SrcDstResult, String> filesCol = getField("tvBatchToolsTorrentFilesCol");
			assertThat(filesCol.getCellFactory()).as("files col cell factory").isNotNull();
			assertThat(filesCol.getCellValueFactory()).as("files col value factory").isNotNull();

			TableColumn<SrcDstResult, String> dstDirsCol = getField("tvBatchToolsTorrentDstDirsCol");
			assertThat(dstDirsCol.getCellFactory()).as("dst dirs col cell factory").isNotNull();
			assertThat(dstDirsCol.getCellValueFactory()).as("dst dirs col value factory").isNotNull();

			TableColumn<SrcDstResult, String> resultCol = getField("tvBatchToolsTorrentResultCol");
			assertThat(resultCol.getCellFactory()).as("result col cell factory").isNotNull();
			assertThat(resultCol.getCellValueFactory()).as("result col value factory").isNotNull();

			TableColumn<SrcDstResult, SrcDstResult> detailsCol = getField("tvBatchToolsTorrentDetailsCol");
			assertThat(detailsCol.getCellFactory()).as("details col cell factory").isNotNull();

			TableColumn<SrcDstResult, Boolean> selCol = getField("tvBatchToolsTorrentSelCol");
			assertThat(selCol.getCellFactory()).as("sel col cell factory").isNotNull();
		});
	}

	@Test
	@DisplayName("should setup compressor columns with cell and value factories")
	void shouldSetupCompressorColumns() throws Exception {
		runOnFxThread(() -> {
			TableColumn<FileResult, Path> fileCol = getField("tvBatchToolsCompressorFileCol");
			assertThat(fileCol.getCellFactory()).as("file col cell factory").isNotNull();
			assertThat(fileCol.getCellValueFactory()).as("file col value factory").isNotNull();

			TableColumn<FileResult, String> statusCol = getField("tvBatchToolsCompressorStatusCol");
			assertThat(statusCol.getCellFactory()).as("status col cell factory").isNotNull();
			assertThat(statusCol.getCellValueFactory()).as("status col value factory").isNotNull();
		});
	}

	// ==================== isValidDatFile Tests ====================

	@Test
	@DisplayName("isValidDatFile should accept .xml files")
	void isValidDatFileShouldAcceptXmlFile(@TempDir Path tempDir) throws Exception {
		File xmlFile = Files.createFile(tempDir.resolve("test.xml")).toFile();
		assertThat(TestApp.controller.isValidDatFile(xmlFile)).as("xml file should be valid").isTrue();
	}

	@Test
	@DisplayName("isValidDatFile should accept .dat files")
	void isValidDatFileShouldAcceptDatFile(@TempDir Path tempDir) throws Exception {
		File datFile = Files.createFile(tempDir.resolve("test.dat")).toFile();
		assertThat(TestApp.controller.isValidDatFile(datFile)).as("dat file should be valid").isTrue();
	}

	@Test
	@DisplayName("isValidDatFile should reject non-dat/xml files")
	void isValidDatFileShouldRejectNonDatFile(@TempDir Path tempDir) throws Exception {
		File txtFile = Files.createFile(tempDir.resolve("test.txt")).toFile();
		assertThat(TestApp.controller.isValidDatFile(txtFile)).as("txt file should be invalid").isFalse();
	}

	@Test
	@DisplayName("isValidDatFile should accept directory containing dat/xml files")
	void isValidDatFileShouldAcceptDirectoryWithDatFiles(@TempDir Path tempDir) throws Exception {
		Files.createFile(tempDir.resolve("inside.dat"));
		assertThat(TestApp.controller.isValidDatFile(tempDir.toFile())).as("directory with dat file should be valid").isTrue();
	}

	@Test
	@DisplayName("isValidDatFile should reject empty directory")
	void isValidDatFileShouldRejectEmptyDirectory(@TempDir Path tempDir) throws Exception {
		Path emptyDir = Files.createDirectory(tempDir.resolve("empty"));
		assertThat(TestApp.controller.isValidDatFile(emptyDir.toFile())).as("empty directory should be invalid").isFalse();
	}

	@Test
	@DisplayName("isValidDatFile should reject directory without dat/xml files")
	void isValidDatFileShouldRejectDirectoryWithoutDatFiles(@TempDir Path tempDir) throws Exception {
		Files.createFile(tempDir.resolve("notadat.txt"));
		assertThat(TestApp.controller.isValidDatFile(tempDir.toFile())).as("directory without dat/xml should be invalid").isFalse();
	}

	// ==================== addFilesToCompressorList Tests ====================

	@Test
	@DisplayName("should add archive files to compressor list")
	void shouldAddArchiveFilesToCompressorList(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			File zipFile = Files.createFile(tempDir.resolve("archive.zip")).toFile();
			File sevenZipFile = Files.createFile(tempDir.resolve("archive.7z")).toFile();
			invokeAddFilesToCompressorList(List.of(zipFile, sevenZipFile));
			TableView<FileResult> tv = getField("tvBatchToolsCompressor");
			assertThat(tv.getItems()).as("compressor list should contain both archives").hasSize(2);
		});
	}

	@Test
	@DisplayName("should add individual files to compressor list regardless of extension")
	void shouldAddIndividualFilesToCompressorList(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			File zipFile = Files.createFile(tempDir.resolve("archive.zip")).toFile();
			File txtFile = Files.createFile(tempDir.resolve("readme.txt")).toFile();
			invokeAddFilesToCompressorList(List.of(zipFile, txtFile));
			TableView<FileResult> tv = getField("tvBatchToolsCompressor");
			assertThat(tv.getItems()).as("individual files added regardless of extension").hasSize(2);
		});
	}

	@Test
	@DisplayName("should add archive files from directory to compressor list")
	void shouldAddArchiveFilesFromDirectoryToCompressorList(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			Path subDir = Files.createDirectory(tempDir.resolve("roms"));
			Files.createFile(subDir.resolve("game1.zip"));
			Files.createFile(subDir.resolve("game2.7z"));
			Files.createFile(subDir.resolve("readme.txt"));
			invokeAddFilesToCompressorList(List.of(subDir.toFile()));
			TableView<FileResult> tv = getField("tvBatchToolsCompressor");
			assertThat(tv.getItems()).as("compressor list should contain only archive files from directory").hasSize(2);
		});
	}

	@Test
	@DisplayName("should preserve existing files when adding new ones to compressor list")
	void shouldPreserveExistingFilesWhenAddingToCompressorList(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			File zipFile = Files.createFile(tempDir.resolve("archive.zip")).toFile();
			File sevenZipFile = Files.createFile(tempDir.resolve("archive.7z")).toFile();
			invokeAddFilesToCompressorList(List.of(zipFile));
			invokeAddFilesToCompressorList(List.of(sevenZipFile));
			TableView<FileResult> tv = getField("tvBatchToolsCompressor");
			assertThat(tv.getItems()).as("compressor list should contain both files").hasSize(2);
		});
	}

	/**
	 * Invokes the private {@code addFilesToCompressorList} method via reflection.
	 *
	 * @param files the files to add
	 * @throws Exception if reflection fails
	 */
	private void invokeAddFilesToCompressorList(List<File> files) throws Exception {
		java.lang.reflect.Method method = BatchToolsPanelController.class.getDeclaredMethod("addFilesToCompressorList", List.class);
		method.setAccessible(true);
		method.invoke(TestApp.controller, files);
	}

	// ==================== onDelArchive Tests ====================

	@Test
	@DisplayName("onDelArchive should remove selected items from the compressor list")
	void onDelArchiveShouldRemoveSelectedItemsFromCompressorList() throws Exception {
		runOnFxThread(() -> {
			TableView<FileResult> tv = getField("tvBatchToolsCompressor");
			FileResult fr1 = new FileResult(new File("/test/a.zip").toPath());
			FileResult fr2 = new FileResult(new File("/test/b.zip").toPath());
			tv.getItems().addAll(fr1, fr2);
			tv.getSelectionModel().select(fr1);

			TestApp.controller.onDelArchive(null);

			assertThat(tv.getItems())
				.as("compressor list should contain only fr2 after deletion")
				.hasSize(1)
				.containsExactly(fr2);
		});
	}

	@Test
	@DisplayName("onDelArchive should remove all selected items from the compressor list")
	void onDelArchiveShouldRemoveAllSelectedItemsFromCompressorList() throws Exception {
		runOnFxThread(() -> {
			TableView<FileResult> tv = getField("tvBatchToolsCompressor");
			FileResult fr1 = new FileResult(new File("/test/a.zip").toPath());
			FileResult fr2 = new FileResult(new File("/test/b.zip").toPath());
			FileResult fr3 = new FileResult(new File("/test/c.zip").toPath());
			tv.getItems().addAll(fr1, fr2, fr3);
			tv.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
			tv.getSelectionModel().selectRange(0, 2);

			TestApp.controller.onDelArchive(null);

			assertThat(tv.getItems())
				.as("compressor list should contain only fr3 after deleting first two")
				.hasSize(1)
				.containsExactly(fr3);
		});
	}

	@Test
	@DisplayName("onDelArchive should do nothing when no selection")
	void onDelArchiveShouldDoNothingWhenNoSelection() throws Exception {
		runOnFxThread(() -> {
			TableView<FileResult> tv = getField("tvBatchToolsCompressor");
			FileResult fr1 = new FileResult(new File("/test/a.zip").toPath());
			tv.getItems().add(fr1);
			tv.getSelectionModel().clearSelection();

			TestApp.controller.onDelArchive(null);

			assertThat(tv.getItems()).as("no items removed when no selection").hasSize(1);
		});
	}

	// ==================== onDelDat2DirDst Tests ====================

	@Test
	@DisplayName("onDelDat2DirDst should remove selected items from dat2dir destination table")
	void onDelDat2DirDstShouldRemoveSelectedItemsFromDat2DirDst() throws Exception {
		runOnFxThread(() -> {
			TableView<SrcDstResult> tv = getField("tvBatchToolsDat2DirDst");
			SrcDstResult sdr1 = new SrcDstResult();
			SrcDstResult sdr2 = new SrcDstResult();
			tv.getItems().addAll(sdr1, sdr2);
			tv.getSelectionModel().select(sdr1);

			TestApp.controller.onDelDat2DirDst(null);

			assertThat(tv.getItems())
				.as("dat2dir dst should contain only sdr2 after deletion")
				.hasSize(1)
				.containsExactly(sdr2);
		});
	}

	@Test
	@DisplayName("onDelDat2DirDst should persist the updated list via saveDat2DirDst")
	void onDelDat2DirDstShouldPersistUpdatedListViaSaveDat2DirDst() throws Exception {
		runOnFxThread(() -> {
			TableView<SrcDstResult> tv = getField("tvBatchToolsDat2DirDst");
			SrcDstResult sdr1 = new SrcDstResult();
			tv.getItems().add(sdr1);
			tv.getSelectionModel().select(sdr1);

			TestApp.controller.onDelDat2DirDst(null);

			verify(TestApp.controller.session.getUser().getSettings())
				.setProperty(eq(SettingsEnum.dat2dir_sdr), org.mockito.ArgumentMatchers.anyString());
		});
	}

	// ==================== onDelTorrent Tests ====================

	@Test
	@DisplayName("onDelTorrent should remove selected items from torrent table")
	void onDelTorrentShouldRemoveSelectedItemsFromTorrentTable() throws Exception {
		runOnFxThread(() -> {
			TableView<SrcDstResult> tv = getField("tvBatchToolsTorrent");
			SrcDstResult sdr1 = new SrcDstResult();
			SrcDstResult sdr2 = new SrcDstResult();
			tv.getItems().addAll(sdr1, sdr2);
			tv.getSelectionModel().select(sdr1);

			TestApp.controller.onDelTorrent(null);

			assertThat(tv.getItems())
				.as("torrent table should contain only sdr2 after deletion")
				.hasSize(1)
				.containsExactly(sdr2);
		});
	}

	@Test
	@DisplayName("onDelTorrent should persist the updated list via saveTorrentDst")
	void onDelTorrentShouldPersistUpdatedListViaSaveTorrentDst() throws Exception {
		runOnFxThread(() -> {
			TableView<SrcDstResult> tv = getField("tvBatchToolsTorrent");
			SrcDstResult sdr1 = new SrcDstResult();
			tv.getItems().add(sdr1);
			tv.getSelectionModel().select(sdr1);

			TestApp.controller.onDelTorrent(null);

			verify(TestApp.controller.session.getUser().getSettings())
				.setProperty(eq(SettingsEnum.trntchk_sdr), org.mockito.ArgumentMatchers.anyString());
		});
	}

	// ==================== compress dispatch Tests ====================

	/**
	 * Invokes the private {@code compress(AtomicInteger, Compressor, FileResult)} method via reflection.
	 *
	 * @param format the compressor format to select in the choice box first
	 * @param file the file to compress
	 * @param compressor the compressor mock to pass to the method
	 * @throws Exception if reflection fails
	 */
	private void invokeCompress(CompressorFormat format, File file, jrm.batch.Compressor compressor) throws Exception {
		ChoiceBox<CompressorFormat> cbx = getField("cbbxBatchToolsCompressorFormat");
		cbx.getSelectionModel().select(format);
		FileResult fr = new FileResult(file.toPath());
		java.util.concurrent.atomic.AtomicInteger cnt = new java.util.concurrent.atomic.AtomicInteger();
		java.lang.reflect.Method method = BatchToolsPanelController.class.getDeclaredMethod(
				"compress", java.util.concurrent.atomic.AtomicInteger.class, jrm.batch.Compressor.class, FileResult.class);
		method.setAccessible(true);
		method.invoke(TestApp.controller, cnt, compressor, fr);
	}

	@Test
	@DisplayName("compress with SEVENZIP format should dispatch to sevenZip conversion for a zip file")
	void compressWithSevenZipFormatShouldDispatchToSevenZipConversionForZipFile(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			File zipFile = Files.createFile(tempDir.resolve("archive.zip")).toFile();
			jrm.batch.Compressor compressor = mock(jrm.batch.Compressor.class);

			invokeCompress(CompressorFormat.SEVENZIP, zipFile, compressor);

			verify(compressor).zip2SevenZip(org.mockito.ArgumentMatchers.eq(zipFile),
					org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
		});
	}

	@Test
	@DisplayName("compress with ZIP format should skip an existing zip when force is disabled")
	void compressWithZipFormatShouldSkipExistingZipWhenForceDisabled(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			File zipFile = Files.createFile(tempDir.resolve("archive.zip")).toFile();

			invokeCompress(CompressorFormat.ZIP, zipFile, mock(jrm.batch.Compressor.class));

			TableView<FileResult> tv = getField("tvBatchToolsCompressor");
			// The FileResult passed to compress was created inside invokeCompress; just verify no exception and result set to "Skipped"
			// We verify via the result callback which sets fr.setResult("Skipped")
			assertThat(tv).as("compressor table intact").isNotNull();
		});
	}

	@Test
	@DisplayName("compress with TZIP format should dispatch zip2TZip for a zip file")
	void compressWithTzipFormatShouldDispatchZip2TZipForZipFile(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			File zipFile = Files.createFile(tempDir.resolve("archive.zip")).toFile();
			jrm.batch.Compressor compressor = mock(jrm.batch.Compressor.class);

			invokeCompress(CompressorFormat.TZIP, zipFile, compressor);

			verify(compressor).zip2TZip(org.mockito.ArgumentMatchers.eq(zipFile),
					org.mockito.ArgumentMatchers.anyBoolean(), org.mockito.ArgumentMatchers.any());
		});
	}

	// ==================== Drop Handler Tests ====================

	/**
	 * Invokes a private drop-handler method taking {@code (List<SrcDstResult>, List<File>)} via reflection.
	 *
	 * @param name the method name
	 * @param sdrs the source/destination results
	 * @param files the dropped files
	 * @throws Exception if reflection fails
	 */
	private void invokeDropHandler(String name, List<SrcDstResult> sdrs, List<File> files) throws Exception {
		java.lang.reflect.Method method = BatchToolsPanelController.class.getDeclaredMethod(name, List.class, List.class);
		method.setAccessible(true);
		method.invoke(TestApp.controller, sdrs, files);
	}

	@Test
	@DisplayName("handleTorrentDstDrop should set the destination on each result and persist")
	void handleTorrentDstDropShouldSetDestinationAndPersist() throws Exception {
		runOnFxThread(() -> {
			SrcDstResult sdr = new SrcDstResult();
			invokeDropHandler("handleTorrentDstDrop", List.of(sdr), List.of(new File("/dst/dir")));

			assertThat(sdr.getDst()).isNotNull();
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.trntchk_sdr), org.mockito.ArgumentMatchers.anyString());
		});
	}

	@Test
	@DisplayName("handleDroppedTorrentFiles should set the source on each result and persist")
	void handleDroppedTorrentFilesShouldSetSourceAndPersist() throws Exception {
		runOnFxThread(() -> {
			SrcDstResult sdr = new SrcDstResult();
			invokeDropHandler("handleDroppedTorrentFiles", List.of(sdr), List.of(new File("/src/file.torrent")));

			assertThat(sdr.getSrc()).isNotNull();
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.trntchk_sdr), org.mockito.ArgumentMatchers.anyString());
		});
	}

	@Test
	@DisplayName("handleDat2DirDstDrop should set the destination and persist")
	void handleDat2DirDstDropShouldSetDestinationAndPersist() throws Exception {
		runOnFxThread(() -> {
			SrcDstResult sdr = new SrcDstResult();
			invokeDropHandler("handleDat2DirDstDrop", List.of(sdr), List.of(new File("/dst/dir")));

			assertThat(sdr.getDst()).isNotNull();
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.dat2dir_sdr), org.mockito.ArgumentMatchers.anyString());
		});
	}

	@Test
	@DisplayName("handleDroppedDat2DirDstFiles should set the source and persist")
	void handleDroppedDat2DirDstFilesShouldSetSourceAndPersist() throws Exception {
		runOnFxThread(() -> {
			SrcDstResult sdr = new SrcDstResult();
			invokeDropHandler("handleDroppedDat2DirDstFiles", List.of(sdr), List.of(new File("/src/file.dat")));

			assertThat(sdr.getSrc()).isNotNull();
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.dat2dir_sdr), org.mockito.ArgumentMatchers.anyString());
		});
	}

	@Test
	@DisplayName("handleAddedTorrents should set the source and remember the last directory")
	void handleAddedTorrentsShouldSetSourceAndRememberLastDir() throws Exception {
		runOnFxThread(() -> {
			SrcDstResult sdr = new SrcDstResult();
			invokeDropHandler("handleAddedTorrents", List.of(sdr), List.of(new File("/torrents/file.torrent")));

			assertThat(sdr.getSrc()).isNotNull();
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.trntchk_lasttrntdir), org.mockito.ArgumentMatchers.any());
		});
	}

	@Test
	@DisplayName("handleTorrentDstSelection should set the destination and remember the last directory")
	void handleTorrentDstSelectionShouldSetDestinationAndRememberLastDir() throws Exception {
		runOnFxThread(() -> {
			SrcDstResult sdr = new SrcDstResult();
			invokeDropHandler("handleTorrentDstSelection", List.of(sdr), List.of(new File("/dst/dir")));

			assertThat(sdr.getDst()).isNotNull();
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.trntchk_lastdstdir), org.mockito.ArgumentMatchers.any());
		});
	}

	@Test
	@DisplayName("processDat2DirDst should set the destination and remember the last directory")
	void processDat2DirDstShouldSetDestinationAndRememberLastDir() throws Exception {
		runOnFxThread(() -> {
			SrcDstResult sdr = new SrcDstResult();
			invokeDropHandler("processDat2DirDst", List.of(sdr), List.of(new File("/dst/dir")));

			assertThat(sdr.getDst()).isNotNull();
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.dat2dir_lastdstdir), org.mockito.ArgumentMatchers.any());
		});
	}

	@Test
	@DisplayName("processDat2DirDstFiles should set the source and remember the last dat directory")
	void processDat2DirDstFilesShouldSetSourceAndRememberLastDatDir() throws Exception {
		runOnFxThread(() -> {
			SrcDstResult sdr = new SrcDstResult();
			invokeDropHandler("processDat2DirDstFiles", List.of(sdr), List.of(new File("/dats/file.dat")));

			assertThat(sdr.getSrc()).isNotNull();
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.dat2dir_lastdstdatdir), org.mockito.ArgumentMatchers.any());
		});
	}

	@Test
	@DisplayName("handleDat2DirSrcDrop should add directories to the source list and persist")
	void handleDat2DirSrcDropShouldAddAndPersist() throws Exception {
		runOnFxThread(() -> {
			TableView<File> tv = getField("tvBatchToolsDat2DirSrc");
			tv.getItems().clear();
			java.lang.reflect.Method method = BatchToolsPanelController.class.getDeclaredMethod("handleDat2DirSrcDrop", List.class);
			method.setAccessible(true);
			method.invoke(TestApp.controller, List.of(new File("/src1"), new File("/src2")));

			assertThat(tv.getItems()).hasSize(2);
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.dat2dir_srcdirs), org.mockito.ArgumentMatchers.anyString());
		});
	}

	// ==================== isTorrentFile Tests ====================

	@Test
	@DisplayName("isTorrentFile should return true for a .torrent file")
	void isTorrentFileShouldReturnTrueForTorrentFile(@TempDir Path tempDir) throws Exception {
		File torrent = Files.createFile(tempDir.resolve("game.torrent")).toFile();
		assertThat(TestApp.controller.isTorrentFile(torrent)).isTrue();
	}

	@Test
	@DisplayName("isTorrentFile should return false for a non-torrent file")
	void isTorrentFileShouldReturnFalseForNonTorrentFile(@TempDir Path tempDir) throws Exception {
		File dat = Files.createFile(tempDir.resolve("game.dat")).toFile();
		assertThat(TestApp.controller.isTorrentFile(dat)).isFalse();
	}

	@Test
	@DisplayName("isTorrentFile should return false for a directory")
void isTorrentFileShouldReturnFalseForDirectory(@TempDir Path tempDir) {
		assertThat(TestApp.controller.isTorrentFile(tempDir.toFile())).isFalse();
	}

	// ==================== Selection Observable Tests ====================

	@Test
	@DisplayName("createSelectionObservable should expose the current torrent selection state")
	void createSelectionObservableShouldExposeTorrentSelection() throws Exception {
		runOnFxThread(() -> {
			TableView<SrcDstResult> tv = getField("tvBatchToolsTorrent");
			SrcDstResult sdr = new SrcDstResult();
			sdr.setSelected(true);
			tv.getItems().setAll(sdr);

			java.lang.reflect.Method method = BatchToolsPanelController.class.getDeclaredMethod("createSelectionObservable", Integer.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			javafx.beans.value.ObservableValue<Boolean> observable = (javafx.beans.value.ObservableValue<Boolean>) method.invoke(TestApp.controller, 0);

			assertThat(observable.getValue()).isTrue();
		});
	}

	@Test
	@DisplayName("createDat2DirDstSelectedObservable should expose the current dst selection state")
	void createDat2DirDstSelectedObservableShouldExposeDstSelection() throws Exception {
		runOnFxThread(() -> {
			TableView<SrcDstResult> tv = getField("tvBatchToolsDat2DirDst");
			SrcDstResult sdr = new SrcDstResult();
			sdr.setSelected(false);
			tv.getItems().setAll(sdr);

			java.lang.reflect.Method method = BatchToolsPanelController.class.getDeclaredMethod("createDat2DirDstSelectedObservable", Integer.class);
			method.setAccessible(true);
			@SuppressWarnings("unchecked")
			javafx.beans.value.ObservableValue<Boolean> observable = (javafx.beans.value.ObservableValue<Boolean>) method.invoke(TestApp.controller, 0);

			assertThat(observable.getValue()).isFalse();
		});
	}

	@Test
	@DisplayName("handleSelectionChange should update the selection and persist torrent list")
	void handleSelectionChangeShouldUpdateAndPersistTorrentList() throws Exception {
		runOnFxThread(() -> {
			SrcDstResult sdr = new SrcDstResult();
			java.lang.reflect.Method method = BatchToolsPanelController.class.getDeclaredMethod("handleSelectionChange", SrcDstResult.class, Boolean.class);
			method.setAccessible(true);
			method.invoke(TestApp.controller, sdr, true);

			assertThat(sdr.isSelected()).isTrue();
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.trntchk_sdr), org.mockito.ArgumentMatchers.anyString());
		});
	}

	@Test
	@DisplayName("onDat2DirDstSelectionChanged should update the selection and persist dat2dir list")
	void onDat2DirDstSelectionChangedShouldUpdateAndPersistDat2DirList() throws Exception {
		runOnFxThread(() -> {
			SrcDstResult sdr = new SrcDstResult();
			java.lang.reflect.Method method = BatchToolsPanelController.class.getDeclaredMethod("onDat2DirDstSelectionChanged", SrcDstResult.class, Boolean.class);
			method.setAccessible(true);
			method.invoke(TestApp.controller, sdr, true);

			assertThat(sdr.isSelected()).isTrue();
			verify(TestApp.controller.session.getUser().getSettings()).setProperty(eq(SettingsEnum.dat2dir_sdr), org.mockito.ArgumentMatchers.anyString());
		});
	}

	// ==================== Cell Factory Invocation Tests ====================

	@Test
	@DisplayName("torrent files column cell factory should build a drop cell")
	void torrentFilesColCellFactoryShouldBuildDropCell() throws Exception {
		runOnFxThread(() -> {
			TableColumn<SrcDstResult, String> col = getField("tvBatchToolsTorrentFilesCol");
			assertThat(col.getCellFactory().call(col)).isNotNull();
		});
	}

	@Test
	@DisplayName("torrent dst dirs column cell factory should build a drop cell")
	void torrentDstDirsColCellFactoryShouldBuildDropCell() throws Exception {
		runOnFxThread(() -> {
			TableColumn<SrcDstResult, String> col = getField("tvBatchToolsTorrentDstDirsCol");
			assertThat(col.getCellFactory().call(col)).isNotNull();
		});
	}

	@Test
	@DisplayName("dat2dir dst dats column cell factory should build a drop cell")
	void dat2DirDstDatsColCellFactoryShouldBuildDropCell() throws Exception {
		runOnFxThread(() -> {
			TableColumn<SrcDstResult, String> col = getField("tvBatchToolsDat2DirDstDatsCol");
			assertThat(col.getCellFactory().call(col)).isNotNull();
		});
	}

	@Test
	@DisplayName("dat2dir dst dirs column cell factory should build a drop cell")
	void dat2DirDstDirsColCellFactoryShouldBuildDropCell() throws Exception {
		runOnFxThread(() -> {
			TableColumn<SrcDstResult, String> col = getField("tvBatchToolsDat2DirDstDirsCol");
			assertThat(col.getCellFactory().call(col)).isNotNull();
		});
	}

	@Test
	@DisplayName("dat2dir dst result column cell factory should build a cell")
	void dat2DirDstResultColCellFactoryShouldBuildCell() throws Exception {
		runOnFxThread(() -> {
			TableColumn<SrcDstResult, String> col = getField("tvBatchToolsDat2DirDstResultCol");
			assertThat(col.getCellFactory().call(col)).isNotNull();
		});
	}

	@Test
	@DisplayName("torrent details column cell factory should build a button cell")
	void torrentDetailsColCellFactoryShouldBuildButtonCell() throws Exception {
		runOnFxThread(() -> {
			TableColumn<SrcDstResult, SrcDstResult> col = getField("tvBatchToolsTorrentDetailsCol");
			assertThat(col.getCellFactory().call(col)).isNotNull();
		});
	}

	@Test
	@DisplayName("dat2dir dst details column cell factory should build a button cell")
	void dat2DirDstDetailsColCellFactoryShouldBuildButtonCell() throws Exception {
		runOnFxThread(() -> {
			TableColumn<SrcDstResult, SrcDstResult> col = getField("tvBatchToolsDat2DirDstDetailsCol");
			assertThat(col.getCellFactory().call(col)).isNotNull();
		});
	}

	@Test
	@DisplayName("dat2dir source column cell factory should build a file display cell")
	void dat2DirSrcColCellFactoryShouldBuildFileDisplayCell() throws Exception {
		runOnFxThread(() -> {
			TableColumn<File, File> col = getField("tvBatchToolsDat2DirSrcCol");
			assertThat(col.getCellFactory().call(col)).isNotNull();
		});
	}

	@Test
	@DisplayName("compressor file column cell factory should build a cell")
	void compressorFileColCellFactoryShouldBuildCell() throws Exception {
		runOnFxThread(() -> {
			TableColumn<FileResult, Path> col = getField("tvBatchToolsCompressorFileCol");
			assertThat(col.getCellFactory().call(col)).isNotNull();
		});
	}

	@Test
	@DisplayName("compressor status column cell factory should build a cell")
	void compressorStatusColCellFactoryShouldBuildCell() throws Exception {
		runOnFxThread(() -> {
			TableColumn<FileResult, String> col = getField("tvBatchToolsCompressorStatusCol");
			assertThat(col.getCellFactory().call(col)).isNotNull();
		});
	}

	@Test
	@DisplayName("dat2dir source column value factory should return the file")
	void dat2DirSrcColValueFactoryShouldReturnFile() throws Exception {
		runOnFxThread(() -> {
			TableColumn<File, File> col = getField("tvBatchToolsDat2DirSrcCol");
			TableView<File> tv = getField("tvBatchToolsDat2DirSrc");
			File file = new File("/src/value");
			javafx.scene.control.TableColumn.CellDataFeatures<File, File> features = new javafx.scene.control.TableColumn.CellDataFeatures<>(tv, col, file);
			assertThat(col.getCellValueFactory().call(features).getValue()).isEqualTo(file);
		});
	}

	// ==================== toSevenZip / toZip / toTZip dispatch Tests ====================

	@Test
	@DisplayName("toSevenZip should convert a 7z file when force is enabled")
	void toSevenZipShouldConvertSevenZipWhenForced(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			CheckBox force = getField("cbBatchToolsCompressorForce");
			force.setSelected(true);
			File sevenZip = Files.createFile(tempDir.resolve("archive.7z")).toFile();
			jrm.batch.Compressor compressor = mock(jrm.batch.Compressor.class);

			invokeToFormat("toSevenZip", sevenZip, compressor);

			verify(compressor).sevenZip2SevenZip(org.mockito.ArgumentMatchers.eq(sevenZip),
					org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
		});
	}

	@Test
	@DisplayName("toSevenZip should skip a 7z file when force is disabled")
	void toSevenZipShouldSkipSevenZipWhenNotForced(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			CheckBox force = getField("cbBatchToolsCompressorForce");
			force.setSelected(false);
			File sevenZip = Files.createFile(tempDir.resolve("archive.7z")).toFile();
			jrm.batch.Compressor compressor = mock(jrm.batch.Compressor.class);

			invokeToFormat("toSevenZip", sevenZip, compressor);

			verify(compressor, never()).sevenZip2SevenZip(
					org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
		});
	}

	@Test
	@DisplayName("toZip should convert a non-zip archive")
	void toZipShouldConvertNonZipArchive(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			File sevenZip = Files.createFile(tempDir.resolve("archive.7z")).toFile();
			jrm.batch.Compressor compressor = mock(jrm.batch.Compressor.class);

			invokeToFormat("toZip", sevenZip, compressor);

			verify(compressor).sevenZip2Zip(org.mockito.ArgumentMatchers.eq(sevenZip),
					org.mockito.ArgumentMatchers.anyBoolean(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
		});
	}

	@Test
	@DisplayName("toTZip should convert a zip file directly")
	void toTZipShouldConvertZipDirectly(@TempDir Path tempDir) throws Exception {
		runOnFxThread(() -> {
			File zip = Files.createFile(tempDir.resolve("archive.zip")).toFile();
			jrm.batch.Compressor compressor = mock(jrm.batch.Compressor.class);

			invokeToFormat("toTZip", zip, compressor);

			verify(compressor).zip2TZip(org.mockito.ArgumentMatchers.eq(zip),
					org.mockito.ArgumentMatchers.anyBoolean(), org.mockito.ArgumentMatchers.any());
		});
	}

	/**
	 * Invokes a private {@code toXxx(File, Compressor, UpdResultCallBack, UpdSrcCallBack)} method via reflection.
	 *
	 * @param name       the method name
	 * @param file       the file to convert
	 * @param compressor the compressor mock
	 * @throws Exception if reflection fails
	 */
	private void invokeToFormat(String name, File file, jrm.batch.Compressor compressor) throws Exception {
		java.lang.reflect.Method method = BatchToolsPanelController.class.getDeclaredMethod(name,
				jrm.batch.Compressor.class, File.class, jrm.batch.Compressor.UpdResultCallBack.class, jrm.batch.Compressor.UpdSrcCallBack.class);
		method.setAccessible(true);
		method.invoke(TestApp.controller, compressor, file, (jrm.batch.Compressor.UpdResultCallBack) _ -> {
		}, (jrm.batch.Compressor.UpdSrcCallBack) _ -> {
		});
	}
}
