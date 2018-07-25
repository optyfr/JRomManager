package jrm.compressors;

import java.io.File;
import java.io.IOException;

import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

/**
 * Zip native archive class, sould not be directly used
 * @author optyfr
 */
class ZipNArchive extends NArchive
{

	public ZipNArchive(final File archive) throws IOException, SevenZipNativeInitializationException
	{
		super(archive);
	}

	public ZipNArchive(final File archive, final boolean readonly) throws IOException, SevenZipNativeInitializationException
	{
		super(archive, readonly);
	}

}
