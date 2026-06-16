/**
 * Provides the security context, user authentication, session state management, and relative path sandboxing utilities for
 * JRomManager.
 * <p>
 * This package implements a secure execution context that differentiates between local single-user desktop mode and multi-user
 * web/server environments:
 * </p>
 * <ul>
 * <li>{@link jrm.security.Session} maintains the active state of an execution context, including localized resource bundles,
 * reports, active profiles, and active scanning tasks.</li>
 * <li>{@link jrm.security.Sessions} acts as the registry for tracking active server sessions.</li>
 * <li>{@link jrm.security.User} represents the authenticated subject with assigned roles and global settings.</li>
 * <li>{@link jrm.security.PathAbstractor} acts as the security sandbox manager, translating relative placeholder paths into secure
 * absolute locations, and validating against path-traversal attacks.</li>
 * </ul>
 *
 * @author Expert Java Code Documentation Developer
 * 
 * @since 1.0
 */
package jrm.security;
