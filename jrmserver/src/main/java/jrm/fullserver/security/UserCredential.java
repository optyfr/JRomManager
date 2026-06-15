package jrm.fullserver.security;

import lombok.Data;

/**
 * UserCredential is a simple data class that represents the credentials of a user, including their login, password, and roles.
 * <p>
 * This class is used to store and manage user credentials in the application. It contains three fields: login, password, and roles.
 * The login field represents the username or identifier for the user, while the password field stores the user's password (which
 * should be securely hashed in practice). The roles field contains a string representation of the user's roles or permissions
 * within the application.
 */
@Data
public class UserCredential {
    
    /**
     * Default constructor for UserCredential.
     * Initializes a new instance of the UserCredential class with default values.
     */
    public UserCredential() {
        // Default constructor
    }
    
    /**
     * Constructs a new UserCredential with the specified login, password, and roles.
     *
     * @param login    the login or username of the user
     * @param password the password of the user (should be securely hashed in practice)
     * @param roles    the roles or permissions of the user within the application
     */
    public UserCredential(String login, String password, String roles) {
        this.login = login;
        this.password = password;
        this.roles = roles;
    }
    
    /**
     * The login or username of the user.
     * 
     * @param login the login or username of the user
     * 
     * @return the login or username of the user
     */
    private String login;
    /**
     * The password of the user (should be securely hashed in practice).
     * 
     * @param password the password of the user
     * 
     * @return the password of the user
     */
    private String password;
    /**
     * The roles or permissions of the user within the application.
     * 
     * @param roles the roles or permissions of the user
     * 
     * @return the roles or permissions of the user
     */
    private String roles;
}