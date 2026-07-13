package jrm.fx.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jrm.batch.DirUpdaterResults.DirUpdaterResult;
import jrm.security.Session;
import jrm.security.Sessions;
import jrm.security.User;
import jrm.misc.GlobalSettings;

/**
 * Tests for {@link BatchDirUpd8rResultsController}.
 * <p>
 * Verifies initialization of table columns and setup methods.
 *
 * @since 3.0.5
 */
@TestFxApplication(BatchDirUpd8rResultsControllerTest.TestApp.class)
@DisplayName("BatchDirUpd8rResultsController Tests")
class BatchDirUpd8rResultsControllerTest {

	/**
	 * Functional interface that accepts a runnable which may throw checked exceptions.
	 */
	@FunctionalInterface
	interface ThrowingRunnable {
		void run() throws Exception;
	}

	/**
	 * Minimal application that creates a {@link BatchDirUpd8rResultsController}
	 * with all FXML fields injected via reflection.
	 */
	public static class TestApp extends Application implements TestFxRecordedStage {
		private Stage primaryStage;
		static BatchDirUpd8rResultsController controller;

		@Override
		public Stage recordedStage() {
			return primaryStage;
		}

		@Override
		public void start(Stage primaryStage) throws Exception {
			this.primaryStage = primaryStage;

			// Set up static mock for Sessions
			GlobalSettings mockSettings = mock(GlobalSettings.class);
			User mockUser = mock(User.class);
			when(mockUser.getSettings()).thenReturn(mockSettings);
			Session mockSession = mock(Session.class);
			when(mockSession.getUser()).thenReturn(mockUser);
			SharedMockSession.getOrCreate().when(Sessions::getSingleSession).thenReturn(mockSession);

			// Create controller
			controller = new BatchDirUpd8rResultsController();

			// Create UI components
			TableView<DirUpdaterResult> resultList = new TableView<>();
			TableColumn<DirUpdaterResult, String> datCol = new TableColumn<>();
			TableColumn<DirUpdaterResult, Integer> haveCol = new TableColumn<>();
			TableColumn<DirUpdaterResult, Integer> createCol = new TableColumn<>();
			TableColumn<DirUpdaterResult, Integer> fixCol = new TableColumn<>();
			TableColumn<DirUpdaterResult, Integer> missCol = new TableColumn<>();
			TableColumn<DirUpdaterResult, Integer> totalCol = new TableColumn<>();
			TableColumn<DirUpdaterResult, DirUpdaterResult> reportCol = new TableColumn<>();
			Button ok = new Button("OK");

			// Inject fields via reflection
			injectField(controller, "resultList", resultList);
			injectField(controller, "datCol", datCol);
			injectField(controller, "haveCol", haveCol);
			injectField(controller, "createCol", createCol);
			injectField(controller, "fixCol", fixCol);
			injectField(controller, "missCol", missCol);
			injectField(controller, "totalCol", totalCol);
			injectField(controller, "reportCol", reportCol);
			injectField(controller, "ok", ok);

			// Initialize controller
			VBox root = new VBox(resultList, ok);
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
				} catch (Throwable t) {
					future.completeExceptionally(t);
				}
			});
			future.get();
		}
	}

	/**
	 * Helper to run code on JavaFX thread and wait for completion.
	 */
	private void runOnFxThread(ThrowingRunnable action) throws Exception {
		TestApp.runOnFxThread(action);
	}

	@Test
	@DisplayName("Should initialize controller without errors")
	void shouldInitializeController() throws Exception {
		runOnFxThread(() -> {
			assertThat(TestApp.controller).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have result list initialized")
	void shouldHaveResultListInitialized() throws Exception {
		runOnFxThread(() -> {
			assertThat(TestApp.controller.getResultList()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have null selection model on result list")
	void shouldHaveNullSelectionModel() throws Exception {
		runOnFxThread(() -> {
			assertThat(TestApp.controller.getResultList().getSelectionModel()).isNull();
		});
	}

	@Test
	@DisplayName("Should have OK button with action handler")
	void shouldHaveOkButtonWithAction() throws Exception {
		runOnFxThread(() -> {
			Button ok = (Button) getField(TestApp.controller, "ok");
			assertThat(ok.getOnAction()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have dat column with cell factory")
	void shouldHaveDatColumnWithCellFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, String> datCol = 
				(TableColumn<DirUpdaterResult, String>) getField(TestApp.controller, "datCol");
			assertThat(datCol.getCellFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have have column with cell factory")
	void shouldHaveHaveColumnWithCellFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, Integer> haveCol = 
				(TableColumn<DirUpdaterResult, Integer>) getField(TestApp.controller, "haveCol");
			assertThat(haveCol.getCellFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have create column with cell factory")
	void shouldHaveCreateColumnWithCellFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, Integer> createCol = 
				(TableColumn<DirUpdaterResult, Integer>) getField(TestApp.controller, "createCol");
			assertThat(createCol.getCellFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have fix column with cell factory")
	void shouldHaveFixColumnWithCellFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, Integer> fixCol = 
				(TableColumn<DirUpdaterResult, Integer>) getField(TestApp.controller, "fixCol");
			assertThat(fixCol.getCellFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have miss column with cell factory")
	void shouldHaveMissColumnWithCellFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, Integer> missCol = 
				(TableColumn<DirUpdaterResult, Integer>) getField(TestApp.controller, "missCol");
			assertThat(missCol.getCellFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have total column with cell factory")
	void shouldHaveTotalColumnWithCellFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, Integer> totalCol = 
				(TableColumn<DirUpdaterResult, Integer>) getField(TestApp.controller, "totalCol");
			assertThat(totalCol.getCellFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have report column with cell factory")
	void shouldHaveReportColumnWithCellFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, DirUpdaterResult> reportCol = 
				(TableColumn<DirUpdaterResult, DirUpdaterResult>) getField(TestApp.controller, "reportCol");
			assertThat(reportCol.getCellFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have dat column with cell value factory")
	void shouldHaveDatColumnWithCellValueFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, String> datCol = 
				(TableColumn<DirUpdaterResult, String>) getField(TestApp.controller, "datCol");
			assertThat(datCol.getCellValueFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have have column with cell value factory")
	void shouldHaveHaveColumnWithCellValueFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, Integer> haveCol = 
				(TableColumn<DirUpdaterResult, Integer>) getField(TestApp.controller, "haveCol");
			assertThat(haveCol.getCellValueFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have create column with cell value factory")
	void shouldHaveCreateColumnWithCellValueFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, Integer> createCol = 
				(TableColumn<DirUpdaterResult, Integer>) getField(TestApp.controller, "createCol");
			assertThat(createCol.getCellValueFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have fix column with cell value factory")
	void shouldHaveFixColumnWithCellValueFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, Integer> fixCol = 
				(TableColumn<DirUpdaterResult, Integer>) getField(TestApp.controller, "fixCol");
			assertThat(fixCol.getCellValueFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have miss column with cell value factory")
	void shouldHaveMissColumnWithCellValueFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, Integer> missCol = 
				(TableColumn<DirUpdaterResult, Integer>) getField(TestApp.controller, "missCol");
			assertThat(missCol.getCellValueFactory()).isNotNull();
		});
	}

	@Test
	@DisplayName("Should have total column with cell value factory")
	void shouldHaveTotalColumnWithCellValueFactory() throws Exception {
		runOnFxThread(() -> {
			@SuppressWarnings("unchecked")
			TableColumn<DirUpdaterResult, Integer> totalCol = 
				(TableColumn<DirUpdaterResult, Integer>) getField(TestApp.controller, "totalCol");
			assertThat(totalCol.getCellValueFactory()).isNotNull();
		});
	}

	/**
	 * Helper to get a field value via reflection.
	 */
	private static Object getField(Object target, String fieldName) {
		try {
			Field field = target.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(target);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get field: " + fieldName, e);
		}
	}
}
