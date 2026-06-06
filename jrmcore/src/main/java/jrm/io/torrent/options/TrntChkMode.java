package jrm.io.torrent.options;

/**
 * Enumeration of available verification modes for matching local files against
 * the metadata specified inside torrent files.
 * 
 * @author optyfr
 */
public enum TrntChkMode
{
	/**
	 * Verify matches using file names/relative directory paths.
	 */
	FILENAME,

	/**
	 * Verify matches using file sizes in bytes.
	 */
	FILESIZE,

	/**
	 * Verify matches using cryptographic SHA-1 piece hashes.
	 */
	SHA1
}
