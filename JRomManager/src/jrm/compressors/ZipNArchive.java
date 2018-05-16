package jrm.compressors;

import java.io.File;
import java.io.IOException;

import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

class ZipNArchive extends NArchive
{

	public ZipNArchive(final File archive) throws IOException, SevenZipNativeInitializationException
	{
		super(archive, false);
	}

	public ZipNArchive(final File archive, final boolean readonly) throws IOException, SevenZipNativeInitializationException
	{
		super(archive, readonly);
	}

}
