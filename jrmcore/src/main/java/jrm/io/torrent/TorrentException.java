package jrm.io.torrent;

/**
 * Custom exception class indicating errors or failures encountered during torrent file parsing, validating, or processing.
 * 
 * @author optyfr
 */
public class TorrentException extends Exception {
    /**
     * Unique identifier for class serialization.
     */
    private static final long serialVersionUID = 6735232930219484803L;

    /**
     * Constructs a new TorrentException with the specified detail message.
     *
     * @param message the detail error message
     */
    public TorrentException(String message) {
        super(message);
    }
}
