package jrm.security;

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
		if (strpath.startsWith("%work"))
			return true;
		if (strpath.startsWith("%shared"))
			return session.getUser().isAdmin();
		return session.getUser().isAdmin();
	}

	public Path getRelativePath(Path path)
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
