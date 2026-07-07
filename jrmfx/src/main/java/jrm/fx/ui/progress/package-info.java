/**
 * Provides the progress dialog for long-running JRomManager tasks in the JavaFX front-end.
 * <p>
 * {@link jrm.fx.ui.progress.Progress} is a modal {@link javafx.stage.Stage} hosting up to three progress bars with
 * time-left estimates, driven by {@link jrm.fx.ui.progress.ProgressController}. Concrete background work is modeled by
 * {@link jrm.fx.ui.progress.ProgressTask}, an abstract {@link javafx.concurrent.Task} that also implements
 * {@link jrm.aui.progress.ProgressHandler} so engine code can report progress without depending on JavaFX directly.
 * The dialog is launched by the main controllers via {@link jrm.fx.ui.ProgressTaskRunner}.
 * </p>
 * <ul>
 * <li>{@link jrm.fx.ui.progress.Progress}: Modal progress dialog stage.</li>
 * <li>{@link jrm.fx.ui.progress.ProgressController}: FXML controller updating the progress bars and labels.</li>
 * <li>{@link jrm.fx.ui.progress.ProgressTask}: Abstract background task exposing progress-handler callbacks.</li>
 * </ul>
 *
 * @author optyfr
 */
package jrm.fx.ui.progress;
