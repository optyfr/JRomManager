package jrm.security;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.val;

public class PathAbstractor
{
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
		if (strpath.startsWith("%work"))
			return true;
		if (strpath.startsWith("%shared"))
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
			val wdir = session.getUser().getSettings().getWorkPath();
			if (path.startsWith(wdir))
				return Paths.get("%work", wdir.relativize(path).toString());
			val sdir = session.getUser().getSettings().getBasePath().resolve("users").resolve("shared");
			if (path.startsWith(sdir))
				return Paths.get("%shared", sdir.relativize(path).toString());
		}
		catch (Exception e)
		{
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
		if (strpath.startsWith("%work"))
		{
			val basepath = session.getUser().getSettings().getWorkPath();
			path = Paths.get(strpath.replace("%work", basepath.toString())).toAbsolutePath().normalize();
			if (!path.startsWith(basepath))
				throw new SecurityException("Forged path");
		}
		else if (strpath.startsWith("%shared"))
		{
			val basepath = session.getUser().getSettings().getBasePath().resolve("users").resolve("shared");
			path = Paths.get(strpath.replace("%shared", basepath.toString())).toAbsolutePath().normalize();
			if (!path.startsWith(basepath))
				throw new SecurityException("Forged path");
		}
		else
			path = Paths.get(strpath);
		return path;
	}

}
