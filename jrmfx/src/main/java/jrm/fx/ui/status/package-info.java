/**
 * Provides status text formatting for the JavaFX front-end.
 * <p>
 * {@link jrm.fx.ui.status.NeutralToNodeFormatter} is a utility that parses the engine's neutral XML status markup
 * (labels with color/bold/italic attributes and inline progress bars) and converts it into a list of JavaFX
 * {@link javafx.scene.Node} instances. It is reused by the report views and the progress dialog so that status
 * messages produced by {@code jrmcore} can be rendered consistently without coupling the engine to JavaFX.
 * </p>
 * <ul>
 * <li>{@link jrm.fx.ui.status.NeutralToNodeFormatter}: Converts neutral status markup into JavaFX nodes.</li>
 * </ul>
 *
 * @author optyfr
 */
package jrm.fx.ui.status;
