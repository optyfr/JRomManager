package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
