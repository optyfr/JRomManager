package jrm.security;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jrm.misc.GlobalSettings;
import lombok.Getter;

/**
 * Represents a user within the security context of the ROM manager application. A user is associated with an active
 * {@link Session}, has a name, a set of roles determining permissions, and user-specific {@link GlobalSettings}.
 * <p>
 * User instances are immutable with respect to their credentials and roles after instantiation. Their settings profile may be
 * modified through the returned {@link GlobalSettings}.
 * </p>
 *
 * @author Expert Java Code Documentation Developer
 * 
 * @since 1.0
 */
public class User {
    /**
     * The session associated with this user.
     * 
     * @return the active security session for this user
     */
    private final @Getter Session session;

    /**
     * The name/identifier of this user.
     * 
     * @return the username
     */
    private final @Getter String name;

    /**
     * The set of authorization roles assigned to this user, normalized to lower-case. This set is unmodifiable and used for
     * security lookup checks.
     */
    private final Set<String> roles;

    /**
     * The global application settings specific to this user.
     * 
     * @return the global settings
     */
    private final @Getter GlobalSettings settings;

    /**
     * Constructs a new {@code User} and associates it with the specified session. The constructor registers the newly created user
     * within the provided session, normalizes role strings to lowercase, and initializes user-specific settings.
     *
     * @param session the security session this user belongs to
     * @param name the username of the user
     * @param roles the array of roles assigned to the user; can be {@code null}
     * 
     * @throws NullPointerException if the session or name is {@code null}
     */
    public User(final Session session, final String name, final String[] roles) {
        this.session = session;
        this.session.user = this;
        this.name = name;
        this.roles = roles != null ? Stream.of(roles).map(String::toLowerCase).collect(Collectors.toSet()) : Collections.emptySet();
        this.settings = new GlobalSettings(this);
    }

    /**
     * Checks whether the user is assigned the specified role. The comparison is performed in a case-insensitive manner.
     *
     * @param role the name of the role to check
     * 
     * @return {@code true} if the user possesses the role, {@code false} otherwise
     * 
     * @throws NullPointerException if the specified role is {@code null}
     */
    public boolean hasRole(final String role) {
        return roles.contains(role.toLowerCase());
    }

    /**
     * Checks whether the user has administrative privileges. This is a convenience method equivalent to calling
     * {@code hasRole("admin")}.
     *
     * @return {@code true} if the user has the admin role, {@code false} otherwise
     */
    public boolean isAdmin() {
        return hasRole("admin");
    }
}
