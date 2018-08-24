package jrm.misc;

public interface UnitRenderer
{
	public default String humanReadableByteCount(long bytes, boolean si)
	{
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B"; //$NON-NLS-1$
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre); //$NON-NLS-1$
	}
}
