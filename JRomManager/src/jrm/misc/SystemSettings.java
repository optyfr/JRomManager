package jrm.misc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface SystemSettings
{
	
	public default String getDBClass()
	{
		return "jrm.fullserver.db.H2";
	}
	
	public default Path getBasePath()
	{
		final String prop = System.getProperty("jrommanager.dir");
		final Path work = (prop != null ? Paths.get(prop) : Paths.get(System.getProperty("user.dir"))).toAbsolutePath().normalize(); //$NON-NLS-1$ //$NON-NLS-2$
		if (!Files.exists(work))
		{
			try
			{
				Files.createDirectories(work);
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(),e);
			}
		}
		return work;
	}
	
	/**
	 * Return the current work path, the one where we save working dirs (xml, cache, backup, ...)<br>
	 * @return the current working path
	 */
	public default Path getWorkPath()
	{
		return getBasePath();
	}

}
