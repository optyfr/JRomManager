package jrm.misc;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

/**
 * Abstract implementation base for managing typed properties. Provides rich
 * conversion helpers for booleans, integers, enums, and standard strings,
 * allowing subclasses to focus on the raw storage mechanisms.
 * 
 * @author optyfr
 */
public abstract class SettingsImpl {
    /**
     * Returns the underlying {@link Properties} store.
     * 
     * @return the raw configuration properties
     */
    public abstract Properties getProperties();

    /**
     * Retrieves a property of type {@code T} using a default value from the
     * specified enum helper.
     * 
     * @param <T>      the type to return
     * @param property the enum option defining the default value and key name
     * @param cls      the target class representation of the type {@code T}
     * @return the resolved property value, or the default value if absent
     */
    public <T> T getProperty(final EnumWithDefault property, Class<T> cls) {
        if (property instanceof Enum<?> e) {
            if (cls == Boolean.class)
                return cls.cast(getProperty(e.toString(), (Boolean) property.getDefault()));
            if (cls == Integer.class)
                return cls.cast(getProperty(e.toString(), ((Number) property.getDefault()).intValue()));
            if (cls == String.class)
                return cls.cast(getProperty(e.toString(), Optional.ofNullable(property.getDefault()).map(Object::toString).orElse("")));
        }
        return null;
    }

    /**
     * Retrieves an enum property value. Resolves the value to the matching constant
     * of the specified enum class.
     * 
     * @param <T>      the enum type
     * @param property the configuration option representing the property key
     * @param cls      the enum class to look up
     * @return the resolved enum constant
     */
    public <T extends Enum<T>> T getEnumProperty(final EnumWithDefault property, Class<T> cls) {
        return Enum.valueOf(cls, getProperty(property));
    }

    /**
     * Sets an enum property value as its string representation.
     * 
     * @param <T>      the enum type
     * @param property the configuration key
     * @param value    the enum constant to set
     */
    public <T extends Enum<T>> void setEnumProperty(final Enum<?> property, T value) {
        setProperty(property, value.toString());
    }

    /**
     * Retrieves a string property value using the specified setting key.
     * 
     * @param property the option key definition
     * @return the string property value, or the default value if absent
     */
    public String getProperty(final EnumWithDefault property) {
        return getProperty(property, String.class);
    }

    /**
     * Retrieves a boolean property value, falling back to a default value if not
     * set.
     * 
     * @param property the property key name
     * @param def      the default fallback value
     * @return the resolved property value
     */
    protected abstract boolean getProperty(final String property, final boolean def);

    /**
     * Retrieves an integer property value, falling back to a default value if not
     * set.
     * 
     * @param property the property key name
     * @param def      the default fallback value
     * @return the resolved property value
     */
    protected abstract int getProperty(final String property, final int def);

    /**
     * Retrieves a string property value, falling back to a default value if not
     * set.
     * 
     * @param property the property key name
     * @param def      the default fallback value
     * @return the resolved property value
     */
    protected abstract String getProperty(final String property, final String def);

    /**
     * Loads configuration settings from the specified XML properties file.
     * 
     * @param file the source properties file
     */
    public abstract void loadSettings(File file);

    /**
     * Saves active configuration settings into the specified XML properties file.
     * 
     * @param file the target properties file
     */
    public abstract void saveSettings(File file);

    /**
     * Configures a boolean property value.
     * 
     * @param property the property key name
     * @param value    the value to set
     */
    protected abstract void setProperty(final String property, final boolean value);

    /**
     * Configures a boolean property value using an enum key representation.
     * 
     * @param property the key option
     * @param value    the value to set
     */
    public void setProperty(final Enum<?> property, final boolean value) {
        setProperty(property.toString(), value);
    }

    /**
     * Configures an integer property value.
     * 
     * @param property the property key name
     * @param value    the value to set
     */
    protected abstract void setProperty(final String property, final int value);

    /**
     * Configures an integer property value using an enum key representation.
     * 
     * @param property the key option
     * @param value    the value to set
     */
    public void setProperty(final Enum<?> property, final int value) {
        setProperty(property.toString(), value);
    }

    /**
     * Configures a string property value.
     * 
     * @param property the property key name
     * @param value    the value to set
     */
    protected abstract void setProperty(final String property, final String value);

    /**
     * Configures a string property value using an enum key representation.
     * 
     * @param property the key option
     * @param value    the value to set
     */
    public void setProperty(final Enum<?> property, final String value) {
        setProperty(property.toString(), value);
    }

    /**
     * Checks if a property with the specified key name exists in the configuration
     * store.
     * 
     * @param property the property key name to verify
     * @return {@code true} if the property is defined, {@code false} otherwise
     */
    protected abstract boolean hasProperty(final String property);

    /**
     * Checks if a property with the specified enum key exists in the configuration
     * store.
     * 
     * @param property the key option to verify
     * @return {@code true} if the property is defined, {@code false} otherwise
     */
    public boolean hasProperty(final Enum<?> property) {
        return hasProperty(property.toString());
    }
}
