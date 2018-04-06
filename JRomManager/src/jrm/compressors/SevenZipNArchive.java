package jrm.compressors;

import java.io.File;
import java.io.IOException;

import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;

class SevenZipNArchive extends NArchive
{

	public SevenZipNArchive(File archive) throws IOException, SevenZipNativeInitializationException
	{
		super(archive, false);
	//	System.out.println("SevenZipNArchive " + archive);
	}
	
	public SevenZipNArchive(File archive, boolean readonly) throws IOException, SevenZipNativeInitializationException
	{
		super(archive, readonly);
	}
	
}
