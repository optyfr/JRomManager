package jrm.profile.data;

import java.io.File;
import java.io.Serializable;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Describe an archive file that can eventually be linked to an {@link AnywareBase} set
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class Archive extends Container implements Serializable
{
	/**
	 * Construct an archive where set is known
	 * @param file the archive {@link File}
	 * @param m the corresponding {@link AnywareBase} set
	 */
	public Archive(final File file, final AnywareBase m)
	{
		super(Container.getType(file), file, m);
	}

	/**
	 * Construct an archive file with no related set
	 * @param file the archive {@link File}
	 * @param attr the file attributes
	 */
	public Archive(final File file, final BasicFileAttributes attr)
	{
		super(Container.getType(file), file, attr);
	}

}
