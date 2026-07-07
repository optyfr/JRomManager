/**
 * Provides the JavaFX desktop user interface for JRomManager.
 * <p>
 * This package is the entry point of the standalone JavaFX front-end. It hosts the {@link javafx.application.Application}
 * subclass {@link jrm.fx.ui.MainFrame} that builds the primary stage, together with its FXML controller
 * {@link jrm.fx.ui.MainFrameController} which wires up the tabbed panels (profile management, scanner, Dir2Dat, batch
 * tools and settings). Panel behavior is implemented by dedicated controllers extending the shared
 * {@link jrm.fx.ui.BaseController} base class, while cross-cutting concerns such as scene styling
 * ({@link jrm.fx.ui.JRMScene}), modal loading ({@link jrm.fx.ui.Loading}), profile loading
 * ({@link jrm.fx.ui.ProfileLoader}) and progress task execution ({@link jrm.fx.ui.ProgressTaskRunner}) are factored out
 * into supporting types. The package integrates with the {@code jrmcore} engine (profiles, scans, batch operations) and
 * reuses shared UI helpers from the {@link jrm.fx.ui.controls}, {@link jrm.fx.ui.misc}, {@link jrm.fx.ui.progress} and
 * {@link jrm.fx.ui.status} sub-packages.
 * </p>
 * <ul>
 * <li>{@link jrm.fx.ui.MainFrame}: JavaFX application entry point owning the primary stage and global singletons.</li>
 * <li>{@link jrm.fx.ui.MainFrameController}: FXML controller for the main tabbed window.</li>
 * <li>{@link jrm.fx.ui.BaseController}: Abstract base for FXML controllers providing file/directory chooser helpers.</li>
 * <li>{@link jrm.fx.ui.JRMScene}: {@link javafx.scene.Scene} subclass managing application style sheets.</li>
 * <li>{@link jrm.fx.ui.ScannerPanelController}: Controller for the ROM scanning panel and its settings.</li>
 * <li>{@link jrm.fx.ui.ProfilePanelController}: Controller for profile selection and management.</li>
 * <li>{@link jrm.fx.ui.BatchToolsPanelController}: Controller for batch compression and torrent checking tools.</li>
 * <li>{@link jrm.fx.ui.Dir2DatController}: Controller for the Dir2Dat panel.</li>
 * </ul>
 *
 * @author optyfr
 */
package jrm.fx.ui;
