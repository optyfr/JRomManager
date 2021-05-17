package jrm.misc;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class IOUtils
{
	private static final boolean POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix"); //$NON-NLS-1$
	private static final FileAttribute<Set<PosixFilePermission>> POSIX_ATTR = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-x---")); //$NON-NLS-1$

	private IOUtils()
	{
	}

	public static Path createTempFile(Path dir, String prefix, String suffix) throws IOException
	{
		if(POSIX)
			return Files.createTempFile(dir, prefix, suffix, POSIX_ATTR); //$NON-NLS-1$
		return Files.createTempFile(dir, prefix, suffix);
	}
	
	public static Path createTempFile(String prefix, String suffix) throws IOException
	{
		if(POSIX)
			return Files.createTempFile(prefix, suffix, POSIX_ATTR); //$NON-NLS-1$
		return Files.createTempFile(prefix, suffix);
	}
	
	public static Path createTempDirectory(String prefix) throws IOException
	{
		if(POSIX)
			return Files.createTempDirectory(prefix, POSIX_ATTR); //$NON-NLS-1$
		return Files.createTempDirectory(prefix);
	}
	
	public static Path createDirectories(Path target) throws IOException
	{
		if(POSIX)
			return Files.createDirectories(target, POSIX_ATTR); //$NON-NLS-1$
		return Files.createDirectories(target);	
	}
	
	/**
	 * @return the posix
	 */
	public static boolean isPosix()
	{
		return POSIX;
	}
	
}
