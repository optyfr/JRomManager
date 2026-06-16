/**
 * Provides classes and structures for applying fixes and repairs to romsets, computer systems, and arcade machine configurations.
 * <p>
 * The main components are:
 * </p>
 * <ul>
 * <li>{@link jrm.profile.fix.Fix} - Orchestrates parallel task processing pipelines to execute all queued repairs.</li>
 * <li>{@link jrm.profile.fix.actions.ContainerAction} - Represents base operations targeted on game/ROM containers (e.g., zip, 7z,
 * directory structures).</li>
 * <li>{@link jrm.profile.fix.actions.EntryAction} - Represents fine-grained operations executed within container files (e.g., add,
 * delete, rename, duplicate entries).</li>
 * </ul>
 *
 * @since 1.0
 * 
 * @author optyfr
 */
package jrm.profile.fix;
