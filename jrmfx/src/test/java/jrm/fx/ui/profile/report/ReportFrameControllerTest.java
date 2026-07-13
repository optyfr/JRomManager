package jrm.fx.ui.profile.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.lang.reflect.Method;

import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxApplication;
import io.gitlab.fxlabs.testfx.junit.jupiter.TestFxRecordedStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ReportFrameController}.
 * <p>
 * Verifies initialization and FXML field injection for the full report frame
 * that embeds a {@link ReportViewController} and a status bar.
 *
 * @since 3.0.5
 */
@TestFxApplication(ReportFrameControllerTest.TestApp.class)
@DisplayName("ReportFrameController Tests")
class ReportFrameControllerTest {

	/**
	 * Test application that creates a {@link ReportFrameController}
	 * with FXML fields injected via reflection.
	 */
	public static class TestApp extends Application implements TestFxRecordedStage {
		private Stage primaryStage;
		static ReportFrameController controller;

		@Override
		public Stage recordedStage() {
			return primaryStage;
		}

		@Override
		public void start(Stage primaryStage) throws Exception {
			this.primaryStage = primaryStage;

			controller = new ReportFrameController();

			// Inject FXML fields via reflection
			ReportViewController mockViewController = new ReportViewController();
			HBox status = new HBox();

			injectField(controller, "viewController", mockViewController);
			injectField(controller, "status", status);

			controller.initialize(null, null);

			VBox root = new VBox();
			Scene scene = new Scene(root, 800, 600);
			primaryStage.setScene(scene);
			primaryStage.show();
		}

		private static void injectField(Object target, String fieldName, Object value) {
			try {
				var field = ReportFrameController.class.getDeclaredField(fieldName);
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
		public static ReportFrameController getController() {
			return controller;
		}
	}

	@Test
	@DisplayName("Should initialize controller")
	void shouldInitializeController() {
		ReportFrameController controller = TestApp.getController();
		assertThat(controller).isNotNull();
	}

	@Test
	@DisplayName("Should have embedded ReportViewController")
	void shouldHaveEmbeddedReportViewController() {
		ReportFrameController controller = TestApp.getController();
		ReportViewController viewController = getField(controller, "viewController");
		assertThat(viewController).as("embedded view controller should be set").isNotNull();
	}

	@Test
	@DisplayName("Should have status bar container")
	void shouldHaveStatusBarContainer() {
		ReportFrameController controller = TestApp.getController();
		HBox status = getField(controller, "status");
		assertThat(status).as("status bar container should be set").isNotNull();
	}

	@Test
	@DisplayName("Should handle initialize without error")
	void shouldHandleInitializeWithoutError() {
		ReportFrameController controller = TestApp.getController();
		// initialize() was already called in TestApp.start(), calling again should not throw
		assertThatCode(() -> controller.initialize(null, null))
				.as("initialize should not throw when called again").doesNotThrowAnyException();
	}

	@Test
	@DisplayName("Should have initialize method from Initializable")
	void shouldHaveInitializeMethodFromInitializable() throws Exception {
		Method initMethod = ReportFrameController.class.getMethod("initialize",
				java.net.URL.class, java.util.ResourceBundle.class);
		assertThat(initMethod).as("initialize method should exist").isNotNull();
	}

	@SuppressWarnings("unchecked")
	private static <T> T getField(Object target, String fieldName) {
		try {
			var field = ReportFrameController.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(target);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Failed to get field " + fieldName, e);
		}
	}
}
