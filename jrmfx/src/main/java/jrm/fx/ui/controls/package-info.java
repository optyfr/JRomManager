/**
 * Provides reusable JavaFX cell factories and dialog helpers shared across the JRomManager front-end.
 * <p>
 * The types in this package render table and list cells with consistent formatting (colored integers, dates, ellipsed
 * strings, profile names, versions, HTML web-views and rich-text nodes) and centralize user-facing dialogs through
 * {@link jrm.fx.ui.controls.Dialogs}. They are consumed by the panel controllers in {@link jrm.fx.ui} and its
 * sub-packages to keep cell-rendering and error-reporting behavior uniform.
 * </p>
 * <ul>
 * <li>{@link jrm.fx.ui.controls.ButtonCellFactory}: Table cell rendering a button with a custom action.</li>
 * <li>{@link jrm.fx.ui.controls.ColoredIntegerCellFactory}: Table cell rendering integers with custom color and alignment.</li>
 * <li>{@link jrm.fx.ui.controls.DateCellFactory}: Table cell formatting {@link java.time.Instant} timestamps.</li>
 * <li>{@link jrm.fx.ui.controls.DescriptorCellFactory}: List cell displaying {@link jrm.profile.scan.options.Descriptor} enum descriptions.</li>
 * <li>{@link jrm.fx.ui.controls.Dialogs}: Utility class for displaying error, warning and confirmation alerts.</li>
 * <li>{@link jrm.fx.ui.controls.DropCell}: Table cell accepting drag-and-drop file operations.</li>
 * <li>{@link jrm.fx.ui.controls.EllipsisStringCellFactory}: Table cell displaying strings with configurable text overrun.</li>
 * <li>{@link jrm.fx.ui.controls.NameCellFactory}: Editable text field table cell with tooltip support.</li>
 * <li>{@link jrm.fx.ui.controls.NodeCellFactory}: Table cell rendering neutral markup as JavaFX {@link javafx.scene.Node} graphs.</li>
 * <li>{@link jrm.fx.ui.controls.ProfileCellFactory}: Cell factory styling {@link jrm.profile.manager.ProfileNFO} rows by MAME status.</li>
 * <li>{@link jrm.fx.ui.controls.VersionCellFactory}: Table cell formatting version strings with fallback placeholders.</li>
 * <li>{@link jrm.fx.ui.controls.WebviewCellFactory}: Table cell rendering HTML content in an embedded {@link javafx.scene.web.WebView}.</li>
 * </ul>
 *
 * @author optyfr
 */
package jrm.fx.ui.controls;
