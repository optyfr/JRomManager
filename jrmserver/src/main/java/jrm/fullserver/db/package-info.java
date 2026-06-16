/**
 * Provides database access classes for the jrm full server.
 * <p>
 * This package contains classes that handle database connections, queries, and operations for the jrm full server application. It
 * includes implementations for different database types (e.g., H2, MySQL, PostgreSQL) and utility classes for managing database
 * interactions. The classes in this package are responsible for establishing connections to the database, executing SQL queries,
 * and handling database-related exceptions. They also provide methods for checking if a database needs to be dropped and for
 * dropping databases when necessary.
 * <p>
 * The DB class is an abstract class that defines the interface for database operations, while specific implementations (e.g., H2)
 * provide concrete implementations of these operations. The classes in this package are designed to be used by other parts of the
 * jrm full server application to perform database-related tasks in a consistent and efficient manner.
 * 
 * @author jrm
 * 
 * @version 1.0
 */
package jrm.fullserver.db;
