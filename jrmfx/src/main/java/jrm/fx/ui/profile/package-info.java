/**
 * Provides the profile viewer window for inspecting loaded JRomManager profiles.
 * <p>
 * {@link jrm.fx.ui.profile.ProfileViewer} is a non-modal {@link javafx.stage.Stage} that displays the currently loaded
 * profile's machines and software lists, with filtering and statistics. Its FXML controller
 * {@link jrm.fx.ui.profile.ProfileViewerController} builds the tables, context menus and filter controls, and notifies
 * the viewer (via a reset counter and timer) when the displayed data must be refreshed. The viewer is opened from the
 * main profile panel and cooperates with the {@link jrm.fx.ui.profile.manager} and {@link jrm.fx.ui.profile.report}
 * sub-packages, as well as the keyword filter dialog in {@code jrm.fx.ui.profile.filter}.
 * </p>
 * <ul>
 * <li>{@link jrm.fx.ui.profile.ProfileViewer}: Stage hosting the profile viewer window.</li>
 * <li>{@link jrm.fx.ui.profile.ProfileViewerController}: FXML controller populating and filtering the profile tables.</li>
 * </ul>
 *
 * @author optyfr
 */
package jrm.fx.ui.profile;
