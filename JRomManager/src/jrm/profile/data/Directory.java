package jrm.profile.data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Describe an directory that can eventually be linked to an {@link AnywareBase} set
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class Directory extends Container implements Serializable
{
	/**
	 * Construct a directory where set is known
	 * @param file the directory {@link File}
	 * @param m the corresponding {@link AnywareBase} set
	 */
	public Directory(final File file, final AnywareBase m)
	{
		super(Type.DIR, file, m);
	}

	/**
	 * Construct a directory with no related set
	 * @param file the directory {@link File}
	 * @param attr the directory attributes
	 */
	public Directory(final File file, final BasicFileAttributes attr)
	{
		super(Type.DIR, file, attr);
	}

}
