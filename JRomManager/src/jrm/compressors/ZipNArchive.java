package jrm.compressors;

import java.io.File;
import java.io.IOException;

import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

class ZipNArchive extends NArchive
{

	public ZipNArchive(File archive) throws IOException, SevenZipNativeInitializationException
	{
		super(archive, false);
	}

	public ZipNArchive(File archive, boolean readonly) throws IOException, SevenZipNativeInitializationException
	{
		super(archive, readonly);
	}

}
