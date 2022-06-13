package jrm.security;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jrm.misc.Log;
import lombok.val;

public class PathAbstractor
{
	private static final String SHARED = "%shared";
	private static final String WORK = "%work";
	private static final String PRESETS = "%presets";
	private static final String FORGED_PATH = "Forged path";
	private Session session;

	public PathAbstractor(Session session)
	{
		this.session = session;
	}

	public boolean isWriteable(String strpath)
	{
		return isWriteable(session, strpath);
	}
	
	public static boolean isWriteable(Session session, String strpath)
	{
		if (strpath.startsWith(WORK))
			return true;
		if (strpath.startsWith(SHARED))
			return session.getUser().isAdmin();
		return session.getUser().isAdmin();
	}

	public File getRelativePath(File file)
	{
		return getRelativePath(session, file.toPath()).toFile();
	}
	
	public Path getRelativePath(Path path)
	{
		return getRelativePath(session, path);
	}
	
	public static Path getRelativePath(Session session, Path path)
	{
		try
		{
			val pdir = session.getUser().getSettings().getWorkPath().resolve("presets");
			if (path.startsWith(pdir))
				return Paths.get(PRESETS, pdir.relativize(path).toString());
			else
			{
				val wdir = session.getUser().getSettings().getWorkPath();
				if (path.startsWith(wdir))
					return Paths.get(WORK, wdir.relativize(path).toString());
				else
				{
					val sdir = session.getUser().getSettings().getBasePath().resolve("users").resolve("shared");
					if (path.startsWith(sdir))
						return Paths.get(SHARED, sdir.relativize(path).toString());
				}
			}
		}
		catch (Exception e)
		{
			Log.err(e.getMessage(), e);
		}
		return path;
	}

	public Path getAbsolutePath(final String strpath) throws SecurityException
	{
		return getAbsolutePath(session, strpath);
	}

	public static Path getAbsolutePath(Session session, final String strpath) throws SecurityException
	{
		final Path path;
		if (strpath.startsWith(PRESETS))
		{
			val basepath = session.getUser().getSettings().getWorkPath().resolve("presets");
			try
			{
				Files.createDirectories(basepath);
			}
			catch (IOException e)
			{
				Log.err(e.getMessage(), e);
			}
			path = Paths.get(strpath.replace(PRESETS, basepath.toString())).toAbsolutePath().normalize();
			if (!path.startsWith(basepath))
				throw new SecurityException(FORGED_PATH);
		}
		else if (strpath.startsWith(WORK))
		{
			val basepath = session.getUser().getSettings().getWorkPath();
			path = Paths.get(strpath.replace(WORK, basepath.toString())).toAbsolutePath().normalize();
			if (!path.startsWith(basepath))
				throw new SecurityException(FORGED_PATH);
		}
		else if (strpath.startsWith(SHARED))
		{
			val basepath = session.getUser().getSettings().getBasePath().resolve("users").resolve("shared");
			path = Paths.get(strpath.replace(SHARED, basepath.toString())).toAbsolutePath().normalize();
			if (!path.startsWith(basepath))
				throw new SecurityException(FORGED_PATH);
		}
		else
			path = Paths.get(strpath);
		return path;
	}

}
