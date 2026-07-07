/**
 * Provides JavaFX support types for the profile manager view.
 * <p>
 * This package supplies the tree and table building blocks used by the profile management panel:
 * {@link jrm.fx.ui.profile.manager.DirItem} represents a directory as an expandable
 * {@link javafx.scene.control.TreeItem} for browsing profile folders, while
 * {@link jrm.fx.ui.profile.manager.HaveNTotalCellFactory} renders "have/total" statistics with color coding that
 * reflects collection completeness. Both types rely on the {@code jrmcore} profile manager model
 * ({@link jrm.profile.manager.Dir} and {@link jrm.profile.manager.ProfileNFOStats}).
 * </p>
 * <ul>
 * <li>{@link jrm.fx.ui.profile.manager.DirItem}: {@link javafx.scene.control.TreeItem} modeling a profile directory tree.</li>
 * <li>{@link jrm.fx.ui.profile.manager.HaveNTotalCellFactory}: Table cell rendering have/total counts with status colors.</li>
 * </ul>
 *
 * @author optyfr
 */
package jrm.fx.ui.profile.manager;
