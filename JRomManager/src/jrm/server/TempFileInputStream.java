package jrm.server;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

	public static InputStream newInstance() throws IOException
	{
		return new TempFileInputStream(File.createTempFile("JRMSRV", null));
	}

	public static InputStream newInstance(InputStream in) throws IOException
	{
		return newInstance(in, -1L, false);
	}

	public static InputStream newInstance(InputStream in, long len) throws IOException
	{
		return newInstance(in, len, false);
	}

	public static InputStream newInstance(InputStream in, long len, boolean close) throws IOException
	{
		File tmpfile = File.createTempFile("JRMSRV", null);
		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmpfile));)
		{
			if (len < 0)
				for (int b = in.read(); b != -1; b = in.read())
					out.write(b);
			else
				for (long i = 0; i < len; i++)
					out.write(in.read());
			if (close)
				in.close();
			out.close();
		}
		return new TempFileInputStream(tmpfile);
	}

}
