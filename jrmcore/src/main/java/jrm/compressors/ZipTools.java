package jrm.compressors;

/**
 * Utility class for handling ZIP entry names. It provides methods to convert file paths to ZIP entry format and vice versa. The
 * methods ensure that the entry names are formatted correctly for use in ZIP archives, replacing backslashes with forward slashes
 * and handling leading slashes appropriately.
 */
public class ZipTools {
    /**
     * Private constructor to prevent instantiation of the utility class. This class is not meant to be instantiated, as it only
     * contains static methods for handling ZIP entry names.
     */
    private ZipTools() {
    }

    /**
     * Converts a file path to a ZIP entry name. This method replaces backslashes with forward slashes and removes any leading slash
     * from the entry name. The resulting string is suitable for use as an entry name in a ZIP archive.
     * 
     * @param name the file path to be converted to a ZIP entry name
     * 
     * @return the converted ZIP entry name, with backslashes replaced by forward slashes and no leading slash
     */
    public static String toZipEntry(String name) {
        name = name.replace('\\', '/');
        if (name.startsWith("/"))
            name = name.substring(1);
        return name;
    }

    /**
     * Converts a file path to a ZIP entry name with a leading slash. This method replaces backslashes with forward slashes and
     * ensures that the resulting entry name starts with a leading slash. The resulting string is suitable for use as an entry name
     * in a ZIP archive where a leading slash is desired.
     * 
     * @param name the file path to be converted to a ZIP entry name with a leading slash
     * 
     * @return the converted ZIP entry name, with backslashes replaced by forward slashes and a leading slash
     */
    public static String toEntry(String name) {
        name = name.replace('\\', '/');
        if (!name.startsWith("/"))
            name = '/' + name;
        return name;
    }
}
