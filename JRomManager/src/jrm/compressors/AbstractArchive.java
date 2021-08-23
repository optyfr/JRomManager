package jrm.compressors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.commons.io.FileUtils;

import jrm.misc.Log;

abstract class AbstractArchive implements Archive
{
	protected File tempDir = null;
	protected File archive;

	/**
	 * @param cmdAdd
	 * @param tmpfile
	 * @throws IOException
	 */
	protected void close(final List<String> cmdAdd, final Path tmpfile) throws IOException
	{
		final var process = new ProcessBuilder(cmdAdd).directory(tempDir).redirectErrorStream(true).start();
		int err = -1;
		try
		{
			err = process.waitFor();
		}
		catch(InterruptedException e)
		{
			Log.err(e.getMessage(),e);
			Thread.currentThread().interrupt();
		}
		FileUtils.deleteDirectory(tempDir);
		if(err != 0)
		{
			Files.deleteIfExists(tmpfile);
			throw new IOException("Process returned " + err); //$NON-NLS-1$
		}
		else
		{
			synchronized(archive)
			{
				Files.move(tmpfile, archive.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
	}
}
