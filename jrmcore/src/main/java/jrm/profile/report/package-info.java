/**
 * Provides classes and structures for retro-gaming ROM manager report
 * generation and filtering.
 * <p>
 * This package implements a hierarchical reporting architecture composed of the
 * following core elements:
 * <ul>
 * <li>{@link jrm.profile.report.Report}: The root report node containing a list
 * of subjects and managing the overall statistics and filtering settings.</li>
 * <li>{@link jrm.profile.report.Subject}: An intermediate container
 * representing individual system romsets or physical file containers (e.g., zip
 * files, directories).</li>
 * <li>{@link jrm.profile.report.Note}: Individual leaf notes describing
 * specific structural statuses, such as missing files, bad hashes, unneeded
 * entities, or successfully matched ROMs.</li>
 * </ul>
 * <p>
 * It integrates support for customizable UI handlers, parallel sorting, and
 * dynamic visibility filtering based on options such as showing valid elements
 * or hiding completely missing ones.
 *
 * @author optyfr
 * @since 1.0
 */
package jrm.profile.report;
