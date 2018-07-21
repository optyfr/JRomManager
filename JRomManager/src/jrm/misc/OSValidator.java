package jrm.misc;

/**
 * Utility class to determine the current OS
 */
public class OSValidator
{
	private static String OS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$

	public static boolean isWindows()
	{
		return (OSValidator.OS.indexOf("win") >= 0); //$NON-NLS-1$
	}

	public static boolean isMac()
	{
		return (OSValidator.OS.indexOf("mac") >= 0); //$NON-NLS-1$
	}

	public static boolean isUnix()
	{
		return (OSValidator.OS.indexOf("nix") >= 0 || OSValidator.OS.indexOf("nux") >= 0 || OSValidator.OS.indexOf("aix") > 0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public static boolean isSolaris()
	{
		return (OSValidator.OS.indexOf("sunos") >= 0); //$NON-NLS-1$
	}
}
