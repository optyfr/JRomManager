package jrm.misc;

/**
 * Interface representing an enum type that defines a default value. Used for setting fallback properties or configurations.
 * 
 * @author optyfr
 */
public interface EnumWithDefault {
    /**
     * Retrieves the default value associated with this enum constant.
     * 
     * @return the default object value, can be null
     */
    public Object getDefault();

}
