package jrm.profile.report;

import java.io.File;
import java.util.Set;
import java.util.zip.CRC32;

import jrm.aui.profile.report.ReportTreeHandler;
import jrm.security.Session;

public interface ReportIntf<T>
{
	public T clone(final Set<FilterOptions> filterOptions);
	
	public void setHandler(ReportTreeHandler<T> handler);
	public ReportTreeHandler<T> getHandler();

	public File getFile();
	
	public long getFileModified();
	
	public default File getReportFile(final Session session)
	{
		return getReportFile(session, getFile());
	}

	public static File getReportFile(final Session session, final File file)
	{
		final CRC32 crc = new CRC32();
		crc.update(file.getAbsolutePath().getBytes());
		final File reports = session.getUser().getSettings().getWorkPath().resolve("reports").toFile(); //$NON-NLS-1$
		reports.mkdirs();
		return new File(reports, String.format("%08x", crc.getValue()) + ".report"); //$NON-NLS-1$
	}
}
