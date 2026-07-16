package jrm.cli;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A utility class for retrieving localized command-line interface messages from a resource bundle.
 */
public class CLIMessages {
    /**
     * The base name of the resource bundle containing CLI messages.
     */
    private static final String BUNDLE_NAME = "jrm.cli.resources.CLIMessages";

    /**
     * The resource bundle instance used to retrieve localized messages.
     */
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /**
     * Private constructor to prevent instantiation of the CLIMessages class.
     */
    private CLIMessages() {
    }

    /**
     * Retrieves the localized message corresponding to the specified key from the resource bundle.
     *
     * @param key The key for the desired message.
     * @return The localized message associated with the key, or a placeholder if the key is not found.
     */
    public static String getString(String key) {
        try {
            return RESOURCE_BUNDLE.getString(key);
        } catch (MissingResourceException _) {
            return '!' + key + '!';
        }
    }
}
