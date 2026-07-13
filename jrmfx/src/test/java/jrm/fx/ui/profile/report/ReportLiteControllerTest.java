package jrm.fx.ui.profile.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.Method;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ReportLiteController}.
 * <p>
 * Verifies initialization, FXML field injection, and the close button behavior
 * for the lightweight report view that embeds a {@link ReportViewController}.
 *
 * @since 3.0.5
 */
@TestFxApplication(ReportLiteControllerTest.TestApp.class)
@DisplayName("ReportLiteController Tests")
class ReportLiteControllerTest {

	/**
	 * Test application that creates a {@link ReportLiteController}
	 * with FXML fields injected via reflection.
	 */
	public static class TestApp extends Application implements TestFxRecordedStage {
		private static Stage primaryStage;
		static ReportLiteController controller;
		static ReportViewController viewController;

		@Override
		public Stage recordedStage() {
			return primaryStage;
		}

		@Override
		public void start(Stage primaryStage) throws Exception {
			TestApp.primaryStage = primaryStage;

			controller = new ReportLiteController();

			// Create a mock ReportViewController with treeview
			viewController = new ReportViewController();
			TreeView<Object> treeview = new TreeView<>();
			injectField(viewController, ReportViewController.class, "treeview", treeview);

			injectField(controller, ReportLiteController.class, "viewController", viewController);

			controller.initialize(null, null);

			VBox root = new VBox();
			// Add treeview to root so it has a scene and window
			root.getChildren().add(treeview);
			Scene scene = new Scene(root, 800, 600);
			primaryStage.setScene(scene);
			primaryStage.show();
		}

		private static void injectField(Object target, Class<?> clazz, String fieldName, Object value) {
			try {
				var field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				field.set(target, value);
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException("Failed to inject field " + fieldName, e);
			}
		}

		/**
		 * Get controller.
		 *
		 * @return controller
		 */
		public static ReportLiteController getController() {
			return controller;
		}

		/**
		 * Get primary stage.
		 *
		 * @return stage
		 */
		public static Stage getPrimaryStage() {
			return primaryStage;
		}
	}

	@Test
	@DisplayName("Should initialize controller")
	void shouldInitializeController() {
		ReportLiteController controller = TestApp.getController();
		assertThat(controller).isNotNull();
	}

	@Test
	@DisplayName("Should have embedded ReportViewController")
	void shouldHaveEmbeddedReportViewController() {
		ReportLiteController controller = TestApp.getController();
		ReportViewController viewController = getField(controller, "viewController");
		assertThat(viewController).as("embedded view controller should be set").isNotNull();
	}

	@Test
	@DisplayName("Should handle initialize without error")
	void shouldHandleInitializeWithoutError() {
		ReportLiteController controller = TestApp.getController();
		// initialize() was already called in TestApp.start(), calling again should not throw
		assertThatCode(() -> controller.initialize(null, null)).doesNotThrowAnyException();
		assertThat(controller).as("controller should still be available after re-initialize").isNotNull();
	}

	@Test
	@DisplayName("Should have initialize method from Initializable")
	void shouldHaveInitializeMethodFromInitializable() throws Exception {
		Method initMethod = ReportLiteController.class.getMethod("initialize",
				java.net.URL.class, java.util.ResourceBundle.class);
		assertThat(initMethod).as("initialize method should exist").isNotNull();
	}

	@Test
	@DisplayName("Should have onClose method defined")
	void shouldHaveOnCloseMethodDefined() throws Exception {
		Method onCloseMethod = ReportLiteController.class.getDeclaredMethod("onClose", javafx.event.ActionEvent.class);
		assertThat(onCloseMethod).as("onClose(ActionEvent) method exists for FXML").isNotNull();
	}

	@Test
	@DisplayName("Should close stage when onClose is invoked")
	void shouldCloseStageWhenOnCloseIsInvoked() throws Exception {
		ReportLiteController controller = TestApp.getController();
		Stage stage = TestApp.getPrimaryStage();

		// Verify stage is showing before close
		assertThat(stage.isShowing()).as("stage should be showing before close").isTrue();

		// Invoke onClose via reflection
		Method onCloseMethod = ReportLiteController.class.getDeclaredMethod("onClose", javafx.event.ActionEvent.class);
		onCloseMethod.setAccessible(true);

		// Run on FX thread since it modifies the stage
		java.util.concurrent.CompletableFuture<Void> future = new java.util.concurrent.CompletableFuture<>();
		javafx.application.Platform.runLater(() -> {
			try {
				onCloseMethod.invoke(controller, (Object) null);
				future.complete(null);
			} catch (Exception e) {
				future.completeExceptionally(e);
			}
		});
		future.get();

		// Verify stage is no longer showing
		assertThat(stage.isShowing()).as("stage should be closed after onClose").isFalse();

		// Restore stage for other tests
		java.util.concurrent.CompletableFuture<Void> restoreFuture = new java.util.concurrent.CompletableFuture<>();
		javafx.application.Platform.runLater(() -> {
			stage.show();
			restoreFuture.complete(null);
		});
		restoreFuture.get(1000, java.util.concurrent.TimeUnit.MILLISECONDS);
	}

	@SuppressWarnings("unchecked")
	private static <T> T getField(Object target, String fieldName) {
		try {
			var field = ReportLiteController.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(target);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to get field " + fieldName, e);
		}
	}
}
