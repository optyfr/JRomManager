package jrm.compressors;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Archive common interface
 * @author optyfr
 *
 */
public interface Archive extends Closeable, AutoCloseable
{
	/**
	 * get a suitable Temporary directory
	 * @return the Temporary directory as {@link File}
	 * @throws IOException
	 */
	public File getTempDir() throws IOException;

	/**
	 * Extract a file entry from Archive
	 * @param entry the file entry name including path as stored in archive
	 * @return the {@link File} extracted
	 * @throws IOException
	 */
	public File extract(String entry) throws IOException;

	/**
	 * Extract a file entry from Archive into an {@link InputStream}
	 * @param entry the file entry name including path as stored in archive
	 * @return the resulting {@link InputStream}
	 * @throws IOException
	 */
	public InputStream extract_stdout(String entry) throws IOException;

	/**
	 * Add an entry to the archive,
	 * The default assume that the {@code baseDir} is from  {@link #getTempDir()} then return {@link #add(File baseDir, String entry)}
	 * @param entry the entry to add, can contains sub directories, but always have to be a file
	 * @return 0 for success, anything else for error, -1 is reserved for read only archives
	 * @throws IOException
	 */
	public default int add(String entry) throws IOException
	{
		return add(getTempDir(), entry);
	}
	
	/**
	 * Add an entry to the archive, baseDir is the base directory of the entry and should not be included in the archive path
	 * @param baseDir the base directory
	 * @param entry the entry to add, can contains sub directories, but always have to be a file
	 * @return 0 for success, anything else for error, -1 is reserved for read only archives
	 * @throws IOException
	 */
	public int add(File baseDir, String entry) throws IOException;
	
	/**
	 * Add an entry to the archive, named by entry, src data come from an {@link InputStream}
	 * @param src the src data
	 * @param entry the entry name (with path)
	 * @return 0 for success, anything else for error, -1 is reserved for read only archives
	 * @throws IOException
	 */
	public int add_stdin(InputStream src, String entry) throws IOException;
	
	/**
	 * Delete an entry from archive
	 * @param entry the entry to delete
	 * @return 0 for success, anything else for error, -1 is reserved for read only archives
	 * @throws IOException in case of IO error
	 */
	public int delete(String entry) throws IOException;
	
	/**
	 * Rename an entry into an archive. Be warned that, 
	 * if using some external commands in backend (like zip), renaming may not exists! 
	 * So that, this function will need to extract, rename, add, then delete!
	 * @param entry the entry to rename
	 * @param newname the new name
	 * @return 0 for success, anything else for error, -1 is reserved for read only archives
	 * @throws IOException in case of IO error, can happen if new name already exists
	 */
	public int rename(String entry, String newname) throws IOException;
	
	/**
	 * Duplicate an entry. Generally, it is handled by extracting, then adding
	 * @param entry the entry to duplicate (with path)
	 * @param newname the new name (with path)
	 * @return 0 for success, anything else for error, -1 is reserved for read only archives
	 * @throws IOException in case of IO error, can happen if new name already exists
	 */
	public int duplicate(String entry, String newname) throws IOException;
}
