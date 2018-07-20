package jrm.compressors;

import java.io.File;
import java.io.IOException;

import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

/**
 * SevenZip native archive class, should not be used directly
 * @author optyfr
 * @see {@link NArchive} and {@link SevenZipArchive}
 */
class SevenZipNArchive extends NArchive
{

	public SevenZipNArchive(final File archive) throws IOException, SevenZipNativeInitializationException
	{
		super(archive);
	}

	public SevenZipNArchive(final File archive, final boolean readonly) throws IOException, SevenZipNativeInitializationException
	{
		super(archive, readonly);
	}

}
