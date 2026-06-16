package jrm.fullserver.db;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import jrm.misc.Log;
import jrm.misc.SystemSettings;
import lombok.Getter;

/**
 * DB is an abstract class that defines the interface for database operations in the system. It provides methods for connecting to a
 * database, determining if a database should be dropped based on access file changes, and dropping a database. The class uses the
 * SystemSettings object to configure the database connection and behavior.
 * <p>
 * The connectToDB method establishes a connection to the specified database, with options to drop the database before connecting,
 * ensure safe connections, and check if the database exists. The shouldDropDB method checks if the database needs to be rebuilt
 * based on changes to access files. The dropDB method allows for dropping a specified database.
 * <p>
 * Subclasses of DB must implement these abstract methods to provide specific database implementations (e.g., MySQL, PostgreSQL,
 * etc.).
 * 
 * @author jrm
 * 
 * @version 1.0
 * 
 * @since 2024-06
 */
public abstract class DB {
    /**
     * The SystemSettings object containing configuration for the database.
     * 
     * @return the SystemSettings object
     */
    protected @Getter SystemSettings settings;

    /**
     * Constructs a new DB object with the specified SystemSettings.
     * 
     * @param settings The SystemSettings object containing configuration for the database.
     */
    protected DB(SystemSettings settings) {
        this.settings = settings;
    }

    /**
     * Gets an instance of the DB class based on the configuration in the SystemSettings. It uses reflection to instantiate the
     * appropriate subclass of DB as specified by the DBClass setting in SystemSettings.
     * 
     * @param settings The SystemSettings object containing configuration for the database.
     * 
     * @return an instance of a subclass of DB, or null if an error occurs during instantiation.
     */
    public static DB getInstance(SystemSettings settings) {
        try {
            return getInstance(settings.getDBClass(), settings);
        } catch (ClassNotFoundException e) {
            Log.err(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets an instance of the DB class based on the specified class name. It uses reflection to instantiate the appropriate
     * subclass of DB.
     * 
     * @param cls The fully qualified class name of the DB subclass to instantiate.
     * @param settings The SystemSettings object containing configuration for the database.
     * 
     * @return an instance of a subclass of DB, or null if an error occurs during instantiation.
     * 
     * @throws ClassNotFoundException if the specified class cannot be found.
     */
    public static DB getInstance(String cls, SystemSettings settings) throws ClassNotFoundException {
        return getInstance(Class.forName(cls).asSubclass(DB.class), settings);
    }

    /**
     * Gets an instance of the DB class based on the specified class. It uses reflection to instantiate the appropriate subclass of
     * DB.
     * 
     * @param <T> The type of the DB subclass to instantiate.
     * @param cls The Class object representing the DB subclass to instantiate.
     * @param settings The SystemSettings object containing configuration for the database.
     * 
     * @return an instance of a subclass of DB, or null if an error occurs during instantiation.
     */
    public static <T extends DB> T getInstance(Class<T> cls, SystemSettings settings) {
        try {
            return cls.getConstructor(SystemSettings.class).newInstance(settings);
        } catch (Exception e) {
            Log.err(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Connect to the database with the specified name. If the drop parameter is true, the database will be deleted before
     * connecting. The safe parameter indicates whether to ensure a safe connection, and the ifexists parameter checks if the
     * database exists before connecting.
     * <p>
     * The method returns a Connection object that can be used to interact with the database. It may throw IOException if there is
     * an error during the connection process, or SQLException if there is an error with the SQL operations.
     * <p>
     * The implementation of this method will depend on the specific database being used and the requirements of the application.
     * Subclasses of DB must provide their own implementation of this method to establish a connection to the appropriate database.
     * <p>
     * Note: The drop parameter should be used with caution, as it will delete the existing database and all its data before
     * connecting. Ensure that this is the intended behavior before setting drop to true.
     * 
     * @param name the name of the database to connect to
     * @param drop if true, the database will be deleted before connecting
     * @param safe if true, ensure a safe connection to the database
     * @param ifexists if true, check if the database exists before connecting
     * 
     * @return a Connection object for interacting with the database
     * 
     * @throws IOException if an error occurs during the connection process
     * @throws SQLException if an error occurs with the SQL operations
     */
    public abstract Connection connectToDB(final String name, final boolean drop, final boolean safe, boolean ifexists) throws IOException, SQLException;

    /**
     * Connect to the database with the specified name. This method is a convenience overload of the connectToDB method that
     * defaults the ifexists parameter to false. If the drop parameter is true, the database will be deleted before connecting, and
     * the safe parameter indicates whether to ensure a safe connection.
     * <p>
     * The method returns a Connection object that can be used to interact with the database. It may throw IOException if there is
     * an error during the connection process, or SQLException if there is an error with the SQL operations.
     * <p>
     * Note: The drop parameter should be used with caution, as it will delete the existing database and all its data before
     * connecting. Ensure that this is the intended behavior before setting drop to true.
     * 
     * @param name the name of the database to connect to
     * @param drop if true, the database will be deleted before connecting
     * @param safe if true, ensure a safe connection to the database
     * 
     * @return a Connection object for interacting with the database
     * 
     * @throws IOException if an error occurs during the connection process
     * @throws SQLException if an error occurs with the SQL operations
     */
    public Connection connectToDB(final String name, final boolean drop, final boolean safe) throws IOException, SQLException {
        return connectToDB(name, drop, safe, false);
    }

    /**
     * Determines whether the database should be dropped based on changes to the CPS and Capture access files. This method checks if
     * the CPS access file has changed since the last check, and if so, it returns true to indicate that the database should be
     * dropped and rebuilt. If the Capture access file is provided and has changed, it also indicates that the database should be
     * dropped. If neither file has changed, or if the CPS access file does not exist but the database does, it returns false to
     * indicate that the database should not be dropped.
     * <p>
     * The implementation of this method will depend on how the access files are monitored and how the database tracks changes to
     * these files. Subclasses of DB must provide their own implementation of this method to determine when the database should be
     * dropped based on access file changes.
     * <p>
     * Note: The method may throw IOException if there is an error while testing the access files, such as if the files cannot be
     * read or accessed. Ensure that proper error handling is implemented when using this method to avoid issues with file access.
     * 
     * @param cpsPath the CPS access file (can't be null, but can be non-existent)
     * @param capturePath the Capture access file (may be null or non-existent)
     * 
     * @return a boolean true if yes, false if no. if cpsPath does not exists but database exists, then it should be false
     * 
     * @throws IOException on error while testing
     */
    public abstract boolean shouldDropDB(final Path cpsPath, final Path capturePath) throws IOException;

    /**
     * Drops the database with the specified name. This method is responsible for deleting the database and all its data. The
     * implementation of this method will depend on the specific database being used and the requirements of the application.
     * Subclasses of DB must provide their own implementation of this method to drop the appropriate database.
     * <p>
     * Note: This operation is irreversible and will result in the loss of all data in the database. Ensure that this is the
     * intended behavior before calling this method.
     * 
     * @param name the name of the database to drop
     * 
     * @throws IOException if an error occurs during the drop process
     */
    public abstract void dropDB(String name) throws IOException;

}