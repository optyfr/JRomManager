package jrm.fx.ui;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

import javafx.stage.Stage;
import jrm.fx.ui.controls.Dialogs;
import jrm.fx.ui.progress.ProgressTask;
import jrm.misc.BreakException;
import jrm.misc.Log;

/**
 * Utility class for running ProgressTask operations with standardized error handling.
 */
final class ProgressTaskRunner {
	private ProgressTaskRunner() {
		// utility class
	}

	/**
	 * Run a progress task with standardized thread setup and error handling.
	 * 
	 * @param <T> the result type
	 * @param stage the parent stage for the progress dialog
	 * @param taskFactory factory that creates the ProgressTask
	 */
	static <T> void run(Stage stage, ProgressTaskFactory<T> taskFactory) {
		try {
			final var task = taskFactory.create(stage);
			final var thread = new Thread(task);
			thread.setDaemon(true);
			thread.start();
		} catch (IOException | URISyntaxException e) {
			Log.err(e.getMessage(), e);
			Dialogs.showError(e);
		}
	}

	/**
	 * Handle InterruptedException in succeeded() method.
	 */
	static void handleInterruptedException(ProgressTask<?> task, InterruptedException e) {
		task.close();
		Log.err(e.getMessage(), e);
		Thread.currentThread().interrupt();
	}

	/**
	 * Handle ExecutionException in succeeded() method.
	 */
	static void handleExecutionException(ProgressTask<?> task, Exception e) {
		task.close();
		Optional.ofNullable(e.getCause()).ifPresentOrElse(
			cause -> {
				Log.err(cause.getMessage(), cause);
				Dialogs.showError(cause);
			},
			() -> {
				Log.err(e.getMessage(), e);
				Dialogs.showError(e);
			}
		);
	}

	/**
	 * Handle exception in failed() method.
	 */
	static void handleFailedException(ProgressTask<?> task, Throwable e) {
		if (e instanceof BreakException) {
			Dialogs.showAlert("Cancelled");
		} else {
			task.close();
			Optional.ofNullable(e.getCause()).ifPresentOrElse(
				cause -> {
					Log.err(cause.getMessage(), cause);
					Dialogs.showError(cause);
				},
				() -> {
					Log.err(e.getMessage(), e);
					Dialogs.showError(e);
				}
			);
		}
	}

	/**
	 * Factory interface for creating ProgressTask instances.
	 */
	@FunctionalInterface
	interface ProgressTaskFactory<T> {
		ProgressTask<T> create(Stage stage) throws IOException, URISyntaxException;
	}
}
