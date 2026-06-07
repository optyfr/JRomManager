package jrm.profile.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

/**
 * Defines a general contract for parsing configuration or initialization (INI)
 * files under specific sections, extracting key-value pairs separated by an
 * equals (=) symbol.
 * 
 * @author optyfr
 * @since 1.0
 */
interface IniProcessor {
    /**
     * Returns the specific INI section header (including brackets, e.g.,
     * {@code [Category]}) that this processor is responsible for parsing.
     * 
     * @return the target section header string
     */
    String getSection();

    /**
     * Callback functional interface executed when a valid key-value pair is parsed
     * from the target section.
     */
    @FunctionalInterface
    interface ProcessFileCallback {
        /**
         * Applies processing on the parsed key-value pair.
         * 
         * @param kv a two-element array where {@code kv[0]} is the trimmed key and
         *           {@code kv[1]} is the trimmed value
         */
        void apply(String[] kv);
    }

    /**
     * Parses the specified INI file, seeking the target section returned by
     * {@link #getSection()}. All lines within that section are split by the equals
     * sign {@code '='}, and the resulting key-value pairs are passed to the
     * provided callback interface.
     * 
     * @param file the INI {@link File} to be read
     * @param cb   the {@link ProcessFileCallback} to execute for each parsed entry
     * @throws IOException if an error occurs while opening or reading the file
     */
    default void processFile(File file, ProcessFileCallback cb) throws IOException {
        try (final var reader = new BufferedReader(new FileReader(file));) {
            String line;
            var inSection = false;
            while (null != (line = reader.readLine())) {
                if (line.equalsIgnoreCase(getSection())) // $NON-NLS-1$
                    inSection = true;
                else if (line.startsWith("[") && inSection) //$NON-NLS-1$
                    break;
                else if (inSection) {
                    final String[] kv = StringUtils.split(line, '=');
                    if (kv.length == 2) {
                        cb.apply(kv);
                    }
                }
            }
        }
    }

}
