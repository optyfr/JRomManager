package jrm.batch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import jrm.aui.progress.ProgressHandler;
import jrm.profile.report.Report;
import jrm.security.PathAbstractor;
import jrm.security.Session;

public class DirUpdaterResults implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public class DirUpdaterResult implements Serializable
	{
		private static final long serialVersionUID = 1L;

		public File dat;
		public Report.Stats stats;
	}

	public File dat;
	public List<DirUpdaterResult> results = new ArrayList<>();
	
	public void add(File dat, Report.Stats stats)
	{
		DirUpdaterResult result = new DirUpdaterResult();
		result.dat = dat;
		result.stats = stats;
		results.add(result);
	}
	
	private static File getFile(final Session session, final File file)
	{
		final CRC32 crc = new CRC32();
		crc.update(PathAbstractor.getAbsolutePath(session, file.toString()).toString().getBytes());
		final File reports = session.getUser().getSettings().getWorkPath().resolve("work").toFile(); //$NON-NLS-1$
		reports.mkdirs();
		return new File(reports, String.format("%08x", crc.getValue()) + ".results"); //$NON-NLS-1$
	}

	public void save(final Session session)
	{
		try (final ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getFile(session, dat)))))
		{
			oos.writeObject(this);
		}
		catch (final Throwable e)
		{
		}
	}
	
	public static DirUpdaterResults load(final Session session, final File file, ProgressHandler progress)
	{
		final var rfile = getFile(session, file);
		try (final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(progress.getInputStream(new FileInputStream(rfile),(int)rfile.length()))))
		{
			return (DirUpdaterResults)ois.readObject();
		}
		catch (final Throwable e)
		{
			System.err.println(e.getMessage());
		}
		return null;
	}

	
	public static DirUpdaterResults load(final Session session, final File file)
	{
		try (final ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(getFile(session, file)))))
		{
			return (DirUpdaterResults)ois.readObject();
		}
		catch (final Throwable e)
		{
			System.err.println(e.getMessage());
		}
		return null;
	}
}
