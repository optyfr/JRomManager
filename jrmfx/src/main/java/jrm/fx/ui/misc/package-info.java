/**
 * Provides miscellaneous JavaFX helpers for the JRomManager front-end.
 * <p>
 * This package groups cross-cutting utilities that do not belong to a specific panel: drag-and-drop wiring
 * ({@link jrm.fx.ui.misc.DragNDrop}), JavaFX property wrappers for batch source/destination entries
 * ({@link jrm.fx.ui.misc.SrcDstResult}) and file results ({@link jrm.fx.ui.misc.FileResult}), and window geometry
 * persistence through {@link jrm.fx.ui.misc.Settings} and {@link jrm.fx.ui.misc.WindowState}. These types are reused by
 * the main controllers and cell factories to keep behavior consistent across the UI.
 * </p>
 * <ul>
 * <li>{@link jrm.fx.ui.misc.DragNDrop}: Attaches filtered file drag-and-drop handling to JavaFX controls.</li>
 * <li>{@link jrm.fx.ui.misc.SrcDstResult}: JavaFX-property-backed source/destination result for batch operations.</li>
 * <li>{@link jrm.fx.ui.misc.FileResult}: JavaFX property holder pairing a file with a result string.</li>
 * <li>{@link jrm.fx.ui.misc.Settings}: Serializes and restores {@link javafx.stage.Stage} bounds to/from JSON.</li>
 * <li>{@link jrm.fx.ui.misc.WindowState}: Value object capturing a window's position, size and state flags.</li>
 * </ul>
 *
 * @author optyfr
 */
package jrm.fx.ui.misc;
