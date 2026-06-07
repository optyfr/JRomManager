package jrm.locale;

import java.beans.Beans;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Utility resource bundle accessor that manages localization and translations
 * for all textual content across the application. It dynamically selects the
 * bundle based on the active or system Locale, or a design-time context.
 * 
 * @author optyfr
 */
public class Messages {
    // Constructor

    /**
     * Private constructor to prevent instantiation of utility localization class.
     */
    private Messages() {
        // do not instantiate
    }
    // Bundle access

    /**
     * The fully qualified name of the resource bundle containing localizations.
     */
    private static final String BUNDLE_NAME = "jrm.resources.Messages"; //$NON-NLS-1$

    /**
     * The default cached resource bundle instance for runtime use.
     */
    private static final ResourceBundle RESOURCE_BUNDLE = Messages.loadBundle();

    /**
     * Dynamically loads the default ResourceBundle based on the platform system
     * Locale.
     *
     * @return the loaded default ResourceBundle instance
     */
    private static ResourceBundle loadBundle() {
        return ResourceBundle.getBundle(Messages.BUNDLE_NAME);
    }

    /**
     * Loads a ResourceBundle matching the specified locale.
     *
     * @param locale the target locale for which the bundle should be loaded
     * @return the loaded ResourceBundle for the specified locale
     */
    public static ResourceBundle loadBundle(Locale locale) {
        return ResourceBundle.getBundle(Messages.BUNDLE_NAME, locale);
    }

    /**
     * Gets the active ResourceBundle, checking if it's currently running in
     * design-time mode.
     *
     * @return the design-time loaded bundle or the static runtime RESOURCE_BUNDLE
     *         instance
     */
    public static ResourceBundle getBundle() {
        return Beans.isDesignTime() ? Messages.loadBundle() : Messages.RESOURCE_BUNDLE;
    }
    // Strings access

    /**
     * Translates/retrieves the localized string value associated with the specified
     * key. If the key is not found, wraps the key name in exclamation marks.
     *
     * @param key the message key to look up in the active ResourceBundle
     * @return the localized string value, or {@code !key!} if missing
     */
    public static String getString(final String key) {
        try {
            return getBundle().getString(key);
        } catch (final MissingResourceException e) {
            return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
