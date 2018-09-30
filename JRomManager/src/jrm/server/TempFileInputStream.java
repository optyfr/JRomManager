package jrm.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TempFileInputStream extends FileInputStream
{
	private File file;
	
	public TempFileInputStream(File file) throws FileNotFoundException
	{
		super(file);
		this.file = file;
	}
	
	@Override
	public void close() throws IOException
	{
		super.close();
		file.delete();
	}


}
