/*
 * Copyright (C) 2018 optyfr
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package jrm.misc;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import lombok.Getter;
import lombok.NonNull;

/**
 * Standard centralized logging facility for the application. Wraps
 * {@link java.util.logging.Logger} to provide consistent formatting,
 * console/file routing, and simplified debugging levels (INFO, WARNING, SEVERE,
 * FINE, FINEST, CONFIG).
 * 
 * @author optyfr
 */
public class Log {
    /**
     * Custom log record formatter that outputs logs using a formatted timestamp,
     * logging level, the message, and any associated stack traces.
     */
    public static class Formatter extends java.util.logging.Formatter {
        
        /** Constructs a new Formatter instance with default settings. This constructor does not perform any specific initialization and can be used to create a basic log formatter for formatting log records in a consistent manner. */
        public Formatter() { /* default constructor */ }
        
        /**
         * Formats the given log record into a single-line string with a trailing
         * newline. Supports formatting embedded exception stack traces.
         * 
         * @param theRecord the log record to format
         * @return the formatted log message
         */
        @Override
        public String format(LogRecord theRecord) {
            final var currDate = new Date();
            currDate.setTime(theRecord.getMillis());
            String message = formatMessage(theRecord);
            var throwableMsg = "";
            if (theRecord.getThrown() != null) {
                final var sw = new StringWriter();
                final var pw = new PrintWriter(sw);
                pw.println();
                theRecord.getThrown().printStackTrace(pw);
                pw.close();
                throwableMsg = sw.toString();
            }
            return String.format("[%1$tF %1$tT] [%2$s] %3$s%4$s%n", currDate, theRecord.getLevel().getName(), message, throwableMsg);
        }
    }

    /**
     * Singleton instance of the log formatter.
     */
    public static final Formatter formatter = new Formatter();

    /**
     * Flag indicating whether the logging subsystem has been initialized.
     * 
     * @return {@code true} if initialized, {@code false} otherwise
     */
    private static @Getter boolean init = false;

    /**
     * Private constructor to prevent direct instantiation. Registers a fallback
     * console handler on the global logger.
     */
    private Log() {
        Logger.getGlobal().addHandler(new ConsoleHandler());
    }

    /**
     * Initializes the logger with a file handler and optional console output.
     * 
     * @param file  the path to the output log file
     * @param debug if {@code true}, console log outputs are enabled at FINE level
     * @param limit maximum file size limit (currently overridden to 100MB
     *              internally)
     * @param count maximum number of log files to keep in rotation (currently
     *              overridden to 5 internally)
     */
    public static void init(final String file, final boolean debug, final int limit, final int count) // NOSONAR
    {
        try {
            final var filehandler = new FileHandler(file, 100 * 1024 * 1024, 5, false);
            filehandler.setFormatter(Log.formatter);
            Logger.getGlobal().setUseParentHandlers(false);
            Logger.getGlobal().addHandler(filehandler);
            if (debug) {
                final var consolehandler = new ConsoleHandler();
                consolehandler.setLevel(Level.FINE);
                consolehandler.setFormatter(Log.formatter);
                Logger.getGlobal().addHandler(consolehandler);
                Logger.getGlobal().setLevel(Level.FINE);
            }
            init = true;
        } catch (SecurityException | IOException e) {
            System.console().format("%s%n", e.getMessage());
        }
    }

    /**
     * Sets the active logging level for all registered handlers and the root
     * logger.
     * 
     * @param level the new logging level
     */
    public static void setLevel(Level level) {
        for (Handler h : Logger.getGlobal().getHandlers())
            h.setLevel(level);
        Logger.getGlobal().setLevel(level);
    }

    /**
     * Retrieves the active level of the root global logger.
     * 
     * @return the current logging level, defaults to {@code INFO} if not defined
     */
    public static @NonNull Level getLevel() {
        return Optional.ofNullable(Logger.getGlobal().getLevel()).orElse(Level.INFO);
    }

    /**
     * Log an informational message.
     * 
     * @param msg the object or message string to log
     */
    public static void info(final Object msg) {
        if (msg == null)
            return;
        if (msg instanceof String str)
            Logger.getGlobal().info(str);
        else
            Logger.getGlobal().info(msg::toString);
    }

    /**
     * Log an informational message retrieved from a supplier.
     * 
     * @param msgSupplier the supplier providing the message to log
     */
    public static void info(Supplier<String> msgSupplier) {
        Logger.getGlobal().info(msgSupplier);
    }

    /**
     * Log a warning message.
     * 
     * @param msg the object or message string to log
     */
    public static void warn(final Object msg) {
        if (msg == null)
            return;
        if (msg instanceof String str)
            Logger.getGlobal().warning(str);
        else
            Logger.getGlobal().warning(msg::toString);
    }

    /**
     * Log a warning message retrieved from a supplier.
     * 
     * @param msgSupplier the supplier providing the message to log
     */
    public static void warn(Supplier<String> msgSupplier) {
        Logger.getGlobal().warning(msgSupplier);
    }

    /**
     * Log a severe/error message.
     * 
     * @param msg the object or message string to log
     */
    public static void err(final Object msg) {
        if (msg == null)
            return;
        if (msg instanceof String str)
            Logger.getGlobal().severe(str);
        else
            Logger.getGlobal().severe(msg::toString);
    }

    /**
     * Log a severe/error message retrieved from a supplier.
     * 
     * @param msgSupplier the supplier providing the message to log
     */
    public static void err(Supplier<String> msgSupplier) {
        Logger.getGlobal().severe(msgSupplier);
    }

    /**
     * Log an error message alongside its throwing exception cause.
     * 
     * @param msg the message to log
     * @param e   the underlying throwable exception cause
     */
    public static void err(final String msg, final Throwable e) {
        Logger.getGlobal().log(Level.SEVERE, msg, e);
    }

    /**
     * Log an error message retrieved from a supplier alongside its throwing
     * exception cause.
     * 
     * @param msgSupplier the supplier providing the message to log
     * @param e           the underlying throwable exception cause
     */
    public static void err(final Supplier<String> msgSupplier, final Throwable e) {
        Logger.getGlobal().log(Level.SEVERE, e, msgSupplier);
    }

    /**
     * Log a fine debugging message.
     * 
     * @param msg the object or message string to log
     */
    public static void debug(final Object msg) {
        if (msg == null)
            return;
        if (msg instanceof String str)
            Logger.getGlobal().fine(str);
        else
            Logger.getGlobal().fine(msg::toString);
    }

    /**
     * Log a fine debugging message retrieved from a supplier.
     * 
     * @param msgSupplier the supplier providing the message to log
     */
    public static void debug(Supplier<String> msgSupplier) {
        Logger.getGlobal().fine(msgSupplier);
    }

    /**
     * Log a trace level finest debugging message.
     * 
     * @param msg the object or message string to log
     */
    public static void trace(final Object msg) {
        if (msg == null)
            return;
        if (msg instanceof String str)
            Logger.getGlobal().finest(str);
        else
            Logger.getGlobal().finest(msg::toString);
    }

    /**
     * Log a trace level finest debugging message retrieved from a supplier.
     * 
     * @param msgSupplier the supplier providing the message to log
     */
    public static void trace(Supplier<String> msgSupplier) {
        Logger.getGlobal().finest(msgSupplier);
    }

    /**
     * Log a configuration level message.
     * 
     * @param msg the object or message string to log
     */
    public static void config(final Object msg) {
        if (msg == null)
            return;
        if (msg instanceof String str)
            Logger.getGlobal().config(str);
        else
            Logger.getGlobal().config(msg::toString);
    }

    /**
     * Log a configuration level message retrieved from a supplier.
     * 
     * @param msgSupplier the supplier providing the message to log
     */
    public static void config(Supplier<String> msgSupplier) {
        Logger.getGlobal().config(msgSupplier);
    }

    /**
     * Log an exception throwing stack trace event.
     * 
     * @param sourceClass  the class from which the exception is thrown
     * @param sourceMethod the method from which the exception is thrown
     * @param thrown       the throwing exception instance
     */
    public static void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        Logger.getGlobal().throwing(sourceClass, sourceMethod, thrown);
    }
}
