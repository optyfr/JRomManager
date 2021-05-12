package jrm.server.shared;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermissions;

public class TempFileInputStream extends FileInputStream
{
	private final File file;
	private final long length;
	
	public TempFileInputStream(File file) throws FileNotFoundException
	{
		super(file);
		this.file = file;
		this.length = file.length();
	}
	
	@Override
	public void close() throws IOException
	{
		super.close();
		Files.deleteIfExists(file.toPath());
	}

	public static InputStream newInstance() throws IOException
	{
		final var attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---"));
		return new TempFileInputStream(Files.createTempFile("JRMSRV", null, attr).toFile());
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
		final var attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---"));
		final var tmpfile = Files.createTempFile("JRMSRV", null, attr);
		try (final var out = new BufferedOutputStream(Files.newOutputStream(tmpfile)))
		{
			if (len < 0)
				for (int b = in.read(); b != -1; b = in.read())
					out.write(b);
			else
				for (long i = 0; i < len; i++)
					out.write(in.read());
			if (close)
				in.close();
		}
		return new TempFileInputStream(tmpfile.toFile());
	}

	/**
	 * @return the len
	 */
	public long getLength()
	{
		return length;
	}

}
