/**
 * Provides the scan report windows for JRomManager's JavaFX front-end.
 * <p>
 * This package offers two report presentations: the full {@link jrm.fx.ui.profile.report.ReportFrame}, a non-modal
 * {@link javafx.stage.Stage} that acts as a {@link jrm.aui.progress.StatusHandler} and live-updates as the scan
 * progresses, and the lighter {@link jrm.fx.ui.profile.report.ReportLite} for displaying a fixed
 * {@link jrm.profile.report.Report}. Both delegate the actual tree-based rendering of report notes to the shared
 * {@link jrm.fx.ui.profile.report.ReportViewController}, and persist their window geometry through
 * {@link jrm.fx.ui.misc.Settings}.
 * </p>
 * <ul>
 * <li>{@link jrm.fx.ui.profile.report.ReportFrame}: Full report stage with live status updates.</li>
 * <li>{@link jrm.fx.ui.profile.report.ReportFrameController}: FXML controller for the full report frame.</li>
 * <li>{@link jrm.fx.ui.profile.report.ReportLite}: Lightweight report stage for a fixed report.</li>
 * <li>{@link jrm.fx.ui.profile.report.ReportLiteController}: FXML controller for the lite report window.</li>
 * <li>{@link jrm.fx.ui.profile.report.ReportViewController}: Shared controller rendering the report tree and filters.</li>
 * </ul>
 *
 * @author optyfr
 */
package jrm.fx.ui.profile.report;
