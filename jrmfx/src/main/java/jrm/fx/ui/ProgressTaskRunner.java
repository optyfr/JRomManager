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
		// utility class; prevent instantiation
	}

	/**
	 * Runs a progress task with standardized thread setup and error handling.
	 *
	 * @param <T> the result type
	 * @param stage the parent stage for the progress dialog
	 * @param taskFactory factory that creates the ProgressTask
	 */
	static <T> void run(Stage stage, ProgressTaskFactory<T> taskFactory) {
		try {
			final var task = taskFactory.create(stage);
			Thread.startVirtualThread(task);
		} catch (IOException | URISyntaxException e) {
			Log.err(e.getMessage(), e);
			Dialogs.showError(e);
		}
	}

	/**
	 * Handles an {@link InterruptedException} that occurred in a {@code succeeded()} callback.
	 *
	 * @param task the progress task that threw the exception
	 * @param e the interrupted exception
	 */
	static void handleInterruptedException(ProgressTask<?> task, InterruptedException e) {
		task.close();
		Log.err(e.getMessage(), e);
		Thread.currentThread().interrupt();
	}

	/**
	 * Handles an {@link java.util.concurrent.ExecutionException} that occurred in a {@code succeeded()} callback.
	 *
	 * @param task the progress task that threw the exception
	 * @param e the execution exception
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
	 * Handles a {@link Throwable} that occurred in a {@code failed()} callback.
	 *
	 * @param task the progress task that threw the exception
	 * @param e the throwable
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
	 *
	 * @param <T> the result type
	 */
	@FunctionalInterface
	interface ProgressTaskFactory<T> {
		/**
		 * Creates a new progress task.
		 *
		 * @param stage the parent stage for the progress dialog
		 * @return the created progress task
		 * @throws IOException if an I/O error occurs
		 * @throws URISyntaxException if a URI used by the task is malformed
		 */
		ProgressTask<T> create(Stage stage) throws IOException, URISyntaxException;
	}
}
