/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.compressors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import jrm.aui.progress.ProgressNarchiveCallBack;
import jrm.compressors.sevenzipjbinding.Archive7ZOpenVolumeCallback;
import jrm.compressors.sevenzipjbinding.ArchiveRAROpenVolumeCallback;
import jrm.compressors.sevenzipjbinding.CloseCreateCallback;
import jrm.compressors.sevenzipjbinding.ExtractorCallback;
import jrm.compressors.sevenzipjbinding.NArchiveBase;
import jrm.misc.GlobalSettings;
import jrm.misc.IOUtils;
import jrm.misc.SettingsEnum;
import jrm.security.Session;
import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IOutCreateArchive;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutFeatureSetLevel;
import net.sf.sevenzipjbinding.IOutFeatureSetMultithreading;
import net.sf.sevenzipjbinding.IOutFeatureSetSolid;
import net.sf.sevenzipjbinding.IOutItemAllFormats;
import net.sf.sevenzipjbinding.IOutUpdateArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.impl.VolumedArchiveInStream;

/**
 * The multiple formats abstract class using SevenZipJBinding as back-end<br>
 * Please note that SevenZipJBinding never modifies "in place" archive :
 * a new temporary archive is always created, and non modified entries may be
 * copied without further re-compression (except if solid archive).<br>
 * This behavior happens even in case of renaming!<br>
 * If the archive does not already exists, then we are in creation mode,
 * and no temporary file will be created.<br>
 * Currently only support 7Z and ZIP. 
 * @author optyfr
 */
abstract class NArchive extends NArchiveBase
{
	private Session session;
	private File archive;
	private final boolean readonly;
	private ProgressNarchiveCallBack cb = null;
	

	private static final Map<String, File> archives = new HashMap<>();

	private ArchiveFormat format = ArchiveFormat.SEVEN_ZIP;
	private String ext = "7z"; //$NON-NLS-1$

	/**
	 * Constructor that default to readwrite
	 * @param archive {@link File} to archive
	 * @throws IOException
	 * @throws SevenZipNativeInitializationException in case of problem to find and initialize sevenzipjbinding native libraries
	 */
	protected NArchive(final Session session, final File archive) throws IOException, SevenZipNativeInitializationException
	{
		this(session, archive, false, null);
	}

	/**
	 * Constructor that default to readwrite
	 * @param archive {@link File} to archive
	 * @param cb {@link ProgressNarchiveCallBack} to show progress
	 * @throws IOException
	 * @throws SevenZipNativeInitializationException in case of problem to find and initialize sevenzipjbinding native libraries
	 */
	protected NArchive(final Session session, final File archive, final ProgressNarchiveCallBack cb) throws IOException, SevenZipNativeInitializationException
	{
		this(session, archive, false, cb);
	}

	/**
	 * Constructor with optional readonly mode
	 * @param archive {@link File} to archive
	 * @param readonly if true, will set archive in readonly safe mode
	 * @param cb {@link ProgressNarchiveCallBack} to show progress
	 * @throws IOException
	 * @throws SevenZipNativeInitializationException in case of problem to find and initialize sevenzipjbinding native libraries
	 */
	protected NArchive(final Session session, final File archive, final boolean readonly, final ProgressNarchiveCallBack cb) throws IOException, SevenZipNativeInitializationException
	{
		this.session = session;
		this.cb = cb;
		if(!SevenZip.isInitializedSuccessfully())
			SevenZip.initSevenZipFromPlatformJAR(session.getUser().getSettings().getTmpPath(true).toFile());
		ext = FilenameUtils.getExtension(archive.getName());
		switch(ext.toLowerCase())
		{
			case "zip": //$NON-NLS-1$
				format = ArchiveFormat.ZIP;
				break;
			case "rar":
				format = ArchiveFormat.RAR;
				break;
			case "7z": //$NON-NLS-1$
			default:
				format = ArchiveFormat.SEVEN_ZIP;
				break;
		}
		if(archive.exists())
		{
			if(format==ArchiveFormat.RAR)	// RAR and RAR multipart
			{
				final var archiveOpenVolumeCallback = new ArchiveRAROpenVolumeCallback(this);
				getCloseables().add(setIInArchive(SevenZip.openInArchive(format, setIInStream(archiveOpenVolumeCallback.getStream(archive.getAbsolutePath())).getIInStream(), archiveOpenVolumeCallback)).getIInArchive());
			}
			else if(format==ArchiveFormat.SEVEN_ZIP && archive.getName().endsWith(".7z.001"))	// SevenZip multipart
			{
				getCloseables().add(setIInArchive(SevenZip.openInArchive(format, new VolumedArchiveInStream(archive.getAbsolutePath(), new Archive7ZOpenVolumeCallback(this)))).getIInArchive());
			}
			else	// auto detect
			{
				getCloseables().add(setIInStream(new RandomAccessFileInStream(new RandomAccessFile(archive, "r"))).getIInStream()); //$NON-NLS-1$
				getCloseables().add(setIInArchive(SevenZip.openInArchive(null, getIInStream())).getIInArchive());
				format = getIInArchive().getArchiveFormat();
			}
		}
		this.readonly = readonly;
		if(null == (this.archive = NArchive.archives.get(archive.getAbsolutePath())))
		{
			this.archive = archive;
			NArchive.archives.put(archive.getAbsolutePath(), this.archive);
		}
	}

	/**
	 * This is where all operations really take place! Almost all is inside {@link IOutCreateCallback} callback,
	 * then we are using {@link IOutUpdateArchive} or {@link IOutCreateArchive} in case of creation mode (where archive does not already exist)  
	 */
	@Override
	public void close() throws IOException
	{
		if(getToAdd().isEmpty() && getToRename().isEmpty() && getToDelete().isEmpty() && getToCopy().isEmpty())
		{
			super.close();
			clearTempDir();
			return;
		}
			
		final var rafs = new HashMap<Integer, RandomAccessFile>();
		final var tmpfiles = new HashMap<Integer, File>();

		try
		{
			final var callback = new CloseCreateCallback(this, tmpfiles, rafs) {
				@Override
				public void setTotal(final long total) throws SevenZipException
				{
					if (cb != null)
						cb.setTotal(total);
				}

				@Override
				public void setCompleted(final long complete) throws SevenZipException
				{
					if (cb != null)
						cb.setCompleted(complete);
				}
			};

			if(archive.exists() && getIInArchive() != null)
			{
				final var tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", "." + ext); //$NON-NLS-1$ //$NON-NLS-2$
				Files.deleteIfExists(tmpfile);
				try(final var raf = new RandomAccessFile(tmpfile.toFile(), "rw")) //$NON-NLS-1$
				{
					final IOutUpdateArchive<IOutItemAllFormats> iout = getIInArchive().getConnectedOutArchive();
					setOptions(iout);

					final int itemsCount = getIInArchive().getNumberOfItems() - getToDelete().size() + getToAdd().size() + getToCopy().size();
					iout.updateItems(new RandomAccessFileOutStream(raf), itemsCount, callback);
				}
				super.close();
				if (Files.exists(tmpfile) && Files.size(tmpfile) > 0 && Files.deleteIfExists(archive.toPath()) && !tmpfile.toFile().renameTo(archive))
					Files.delete(tmpfile);
			}
			else
			{
				try(final var iout = SevenZip.openOutArchive(format); RandomAccessFile raf = new RandomAccessFile(archive, "rw")) //$NON-NLS-1$
				{
					setOptions(iout);

					final var itemsCount = getToAdd().size() + getToCopy().size();

					iout.createArchive(new RandomAccessFileOutStream(raf), itemsCount, callback);
				}
				super.close();
			}
		}
		finally
		{
			for(final var raf : rafs.values())
				raf.close();
			for(final var tmpfile : tmpfiles.values())
				Files.delete(tmpfile.toPath());
		}
		clearTempDir();
	}

	/**
	 * Mapper between SevenZipJBinding options and {@link GlobalSettings}
	 * @param iout the archive feature to map (see code to know what is supported)
	 * @throws SevenZipException
	 */
	private void setOptions(final Object iout) throws SevenZipException
	{
		switch(format)
		{
			case SEVEN_ZIP:
				if(iout instanceof IOutFeatureSetSolid)
					((IOutFeatureSetSolid) iout).setSolid(session.getUser().getSettings().getProperty(SettingsEnum.sevenzip_solid, true)); //$NON-NLS-1$
				if(iout instanceof IOutFeatureSetLevel)
					((IOutFeatureSetLevel) iout).setLevel(SevenZipOptions.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.sevenzip_level, SevenZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$
				if(iout instanceof IOutFeatureSetMultithreading)
					((IOutFeatureSetMultithreading) iout).setThreadCount(session.getUser().getSettings().getProperty(SettingsEnum.sevenzip_threads, -1)); //$NON-NLS-1$
				break;
			case ZIP:
				if(iout instanceof IOutFeatureSetLevel)
					((IOutFeatureSetLevel) iout).setLevel(ZipOptions.valueOf(session.getUser().getSettings().getProperty(SettingsEnum.zip_level, ZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$
				if(iout instanceof IOutFeatureSetMultithreading)
					((IOutFeatureSetMultithreading) iout).setThreadCount(session.getUser().getSettings().getProperty(SettingsEnum.zip_threads, -1)); //$NON-NLS-1$
				break;
			default:
				break;
		}

	}

	/**
	 * Internal method to extract one entry into an arbitrary base directory
	 * @param baseDir the base directory where we should extract file
	 * @param entry the entry name of the file (with path)
	 * @return 0 in case of success, -1 otherwise
	 * @throws IOException
	 */
	private int extract(final File baseDir, final String entry) throws IOException
	{
		if(entry != null)
		{
			final var simpleInArchive = getIInArchive().getSimpleInterface();
			for(final var item : simpleInArchive.getArchiveItems())
			{
				if(item.getPath().equals(entry))
				{
					final var file = new File(baseDir, entry);
					FileUtils.forceMkdirParent(file);
					try(final var out = new RandomAccessFile(file, "rw")) //$NON-NLS-1$
					{
						if(item.extractSlow(new RandomAccessFileOutStream(out)) == ExtractOperationResult.OK)
							return 0;
					}
				}
			}
			return -1;
		}
		final var tmpfiles = new HashMap<Integer, File>();
		final var rafs = new HashMap<Integer, RandomAccessFile>();
		final var idx = new int[getIInArchive().getNumberOfItems()];
		for (var i = 0; i < idx.length; i++)
		{
			idx[i] = i;
			if(!(boolean)getIInArchive().getProperty(i, PropID.IS_FOLDER))
			{
				final var file = IOUtils.createTempFile("JRM", null).toFile();
				tmpfiles.put(i, file); //$NON-NLS-1$
				rafs.put(i, new RandomAccessFile(file, "rw")); //$NON-NLS-1$
			}
			else
			{
				final var dir = new File(baseDir, (String) getIInArchive().getProperty(i, PropID.PATH));
				FileUtils.forceMkdir(dir);
			}
		}
		
		getIInArchive().extract(idx, false, new ExtractorCallbackWithProgress(this, baseDir, tmpfiles, rafs));
		return 0;
	}

	private final class ExtractorCallbackWithProgress extends ExtractorCallback
	{
		private ExtractorCallbackWithProgress(NArchiveBase nArchive, File baseDir, Map<Integer, File> tmpfiles, Map<Integer, RandomAccessFile> rafs)
		{
			super(nArchive, baseDir, tmpfiles, rafs);
		}

		@Override
		public void setTotal(long total) throws SevenZipException
		{
			if(cb != null)
				cb.setTotal(total);
		}

		@Override
		public void setCompleted(long complete) throws SevenZipException
		{
			if(cb != null)
				cb.setCompleted(complete);
		}
	}

	@Override
	public int extract() throws IOException
	{
		return extract(getTempDir(), null);
	}
	
	@Override
	public File extract(final String entry) throws IOException
	{
		extract(getTempDir(), entry);
		final var result = new File(getTempDir(), entry);
		if(result.exists())
			return result;
		return null;
	}

	@Override
	public InputStream extractStdOut(final String entry) throws IOException
	{
		extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(), entry));
	}

	@Override
	public int add(final File baseDir, final String entry) throws IOException
	{
		if(readonly)
			return -1;
		if(baseDir.isFile())
			FileUtils.copyFile(baseDir, new File(getTempDir(), entry));
		else if(!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		getToAdd().add(entry);
		return 0;
	}

	@Override
	public int addStdIn(final InputStream src, final String entry) throws IOException
	{
		if(readonly)
			return -1;
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		getToAdd().add(entry);
		return 0;
	}

	@Override
	public int delete(final String entry) throws IOException
	{
		if(readonly)
			return -1;
		getToDelete().add(normalize(entry));
		return 0;
	}

	@Override
	public int rename(final String entry, final String newname) throws IOException
	{
		if(readonly)
			return -1;
		getToRename().put(normalize(entry), normalize(newname));
		return 0;
	}

	@Override
	public int duplicate(final String entry, final String newname) throws IOException
	{
		if(readonly)
			return -1;
		getToCopy().put(normalize(newname), normalize(entry));
		return 0;
	}

	/**
	 * Normalize char separator according platform default separator
	 * @param entry the entry to normalize
	 * @return the normalized entry
	 */
	private String normalize(final String entry)
	{
		if(File.separatorChar == '/')
			return entry.replace('\\', '/');
		return entry.replace('/', '\\');
	}
}
