package jrm.profile.manager;

import java.io.File;

/**
 * Represents a logical directory in the ROM manager, wrapping a {@link File} object
 * and providing named display capability and safe creation.
 * 
 * @author optyfr
 */
public class Dir
{
	/**
	 * The physical directory file.
	 */
	private final File file;

	/**
	 * The custom or file-derived display name of this directory.
	 */
	private final String name;

	/**
	 * Constructs a new directory instance. If the directory on the physical filesystem
	 * does not exist, it is recursively created.
	 * 
	 * @param file the physical folder location
	 */
	public Dir(final File file)
	{
		this.file = file;
		name = file.getName();
		if(!this.file.exists())
			this.file.mkdirs();
	}

	/**
	 * Constructs a new directory instance with a custom display name.
	 * The physical directory is not automatically created.
	 * 
	 * @param file the physical folder location
	 * @param name the display name for the directory
	 */
	public Dir(final File file, final String name)
	{
		this.file = file;
		this.name = name;
	}

	/**
	 * Retrieves the physical folder wrapped by this directory representation.
	 * 
	 * @return the underlying {@link File} directory
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * Returns the display name of the directory.
	 * 
	 * @return the directory's custom or native name
	 */
	@Override
	public String toString()
	{
		return name;
	}
	
	/**
	 * Compares this directory with another object for equality.
	 * Two directories are considered equal if they point to the exact same physical file path.
	 * 
	 * @param obj the reference object with which to compare
	 * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise
	 */
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Dir d)
			return file.equals(d.file);
		return false;
	}
	
	/**
	 * Computes the hash code of this directory based on the underlying file path.
	 * 
	 * @return a hash code value for this object
	 */
	@Override
	public int hashCode()
	{
		return file.hashCode();
	}
	
	/**
	 * Renames the physical directory to a new target path on disk.
	 * 
	 * @param newFile the new physical target folder
	 * @return a new {@link Dir} instance representing the new path if the operation succeeded,
	 *         or {@code this} instance if renaming failed
	 */
	public Dir renameTo(File newFile)
	{
		if(this.file.renameTo(newFile))
			return new Dir(newFile);
		return this;
	}
}
