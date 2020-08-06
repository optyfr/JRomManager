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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import jrm.aui.progress.ProgressNarchiveCallBack;
import jrm.compressors.sevenzipjbinding.Archive7ZOpenVolumeCallback;
import jrm.compressors.sevenzipjbinding.ArchiveRAROpenVolumeCallback;
import jrm.compressors.sevenzipjbinding.CloseCreateCallback;
import jrm.compressors.sevenzipjbinding.ExtractorCallback;
import jrm.compressors.sevenzipjbinding.NArchiveBase;
import jrm.misc.GlobalSettings;
import jrm.misc.SettingsEnum;
import jrm.security.Session;
import lombok.*;
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
	

	private final static HashMap<String, File> archives = new HashMap<>();

	private ArchiveFormat format = ArchiveFormat.SEVEN_ZIP;
	private String ext = "7z"; //$NON-NLS-1$

	/**
	 * Constructor that default to readwrite
	 * @param archive {@link File} to archive
	 * @throws IOException
	 * @throws SevenZipNativeInitializationException in case of problem to find and initialize sevenzipjbinding native libraries
	 */
	public NArchive(final Session session, final File archive) throws IOException, SevenZipNativeInitializationException
	{
		this(session, archive, false, null);
		// System.out.println("SevenZipNArchive " + archive);
	}

	/**
	 * Constructor that default to readwrite
	 * @param archive {@link File} to archive
	 * @param cb {@link ProgressNarchiveCallBack} to show progress
	 * @throws IOException
	 * @throws SevenZipNativeInitializationException in case of problem to find and initialize sevenzipjbinding native libraries
	 */
	public NArchive(final Session session, final File archive, final ProgressNarchiveCallBack cb) throws IOException, SevenZipNativeInitializationException
	{
		this(session, archive, false, cb);
		// System.out.println("SevenZipNArchive " + archive);
	}

	/**
	 * Constructor with optional readonly mode
	 * @param archive {@link File} to archive
	 * @param readonly if true, will set archive in readonly safe mode
	 * @param cb {@link ProgressNarchiveCallBack} to show progress
	 * @throws IOException
	 * @throws SevenZipNativeInitializationException in case of problem to find and initialize sevenzipjbinding native libraries
	 */
	public NArchive(final Session session, final File archive, final boolean readonly, final ProgressNarchiveCallBack cb) throws IOException, SevenZipNativeInitializationException
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
				ArchiveRAROpenVolumeCallback archiveOpenVolumeCallback = new ArchiveRAROpenVolumeCallback(this);
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
			NArchive.archives.put(archive.getAbsolutePath(), this.archive = archive);
	}

	/**
	 * This is where all operations really take place! Almost all is inside {@link IOutCreateCallback} callback,
	 * then we are using {@link IOutUpdateArchive} or {@link IOutCreateArchive} in case of creation mode (where archive does not already exist)  
	 */
	@Override
	public void close() throws IOException
	{
		if(!getToAdd().isEmpty() || !getToRename().isEmpty() || !getToDelete().isEmpty() || !getToCopy().isEmpty())
		{
			val rafs = new HashMap<Integer, RandomAccessFile>();
			val tmpfiles = new HashMap<Integer, File>();

			try
			{
				val callback = new CloseCreateCallback(this, tmpfiles, rafs) {
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
					// System.out.println("modifying archive "+archive);
					val tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", "." + ext).toFile(); //$NON-NLS-1$ //$NON-NLS-2$
					tmpfile.delete();
					try(RandomAccessFile raf = new RandomAccessFile(tmpfile, "rw")) //$NON-NLS-1$
					{
						final IOutUpdateArchive<IOutItemAllFormats> iout = getIInArchive().getConnectedOutArchive();
						SetOptions(iout);
	
						final int itemsCount = getIInArchive().getNumberOfItems() - getToDelete().size() + getToAdd().size() + getToCopy().size();
						// System.err.println(itemsCount);
						iout.updateItems(new RandomAccessFileOutStream(raf), itemsCount, callback);
					}
					super.close();
					// System.out.println("done with "+tmpfile);
					if(tmpfile.exists() && tmpfile.length() > 0)
					{
						// System.out.println("moving "+tmpfile+" to "+archive);
						archive.delete();
						if(!tmpfile.renameTo(archive))
							tmpfile.delete();
					}
				}
				else
				{
					// System.out.println("creating archive "+archive);
					try(val iout = SevenZip.openOutArchive(format); RandomAccessFile raf = new RandomAccessFile(archive, "rw")) //$NON-NLS-1$
					{
						SetOptions(iout);
	
						val itemsCount = getToAdd().size() + getToCopy().size();
	
						iout.createArchive(new RandomAccessFileOutStream(raf), itemsCount, callback);
					}
					super.close();
				}
			}
			finally
			{
				for(val raf : rafs.values())
					raf.close();
				for(val tmpfile : tmpfiles.values())
					tmpfile.delete();
			}
		}
		else
		{
			super.close();
		}
		clearTempDir();
	}

	/**
	 * Mapper between SevenZipJBinding options and {@link GlobalSettings}
	 * @param iout the archive feature to map (see code to know what is supported)
	 * @throws SevenZipException
	 */
	private void SetOptions(final Object iout) throws SevenZipException
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
			val simpleInArchive = getIInArchive().getSimpleInterface();
			for(val item : simpleInArchive.getArchiveItems())
			{
				if(item.getPath().equals(entry))
				{
					val file = new File(baseDir, entry);
					FileUtils.forceMkdirParent(file);
					try(val out = new RandomAccessFile(file, "rw")) //$NON-NLS-1$
					{
						if(item.extractSlow(new RandomAccessFileOutStream(out)) == ExtractOperationResult.OK)
							return 0;
					}
				}
			}
		}
		else
		{
			val tmpfiles = new HashMap<Integer, File>();
			val rafs = new HashMap<Integer, RandomAccessFile>();
			val idx = new int[getIInArchive().getNumberOfItems()];
			for (var i = 0; i < idx.length; i++)
			{
				idx[i] = i;
				if(!(Boolean)getIInArchive().getProperty(i, PropID.IS_FOLDER))
				{
					val file = Files.createTempFile("JRM", null).toFile();
					tmpfiles.put(i, file); //$NON-NLS-1$
					rafs.put(i, new RandomAccessFile(file, "rw")); //$NON-NLS-1$
				}
				else
				{
					val dir = new File(baseDir, (String) getIInArchive().getProperty(i, PropID.PATH));
					FileUtils.forceMkdir(dir);
				}
			}
			
			getIInArchive().extract(idx, false, new ExtractorCallback(this, baseDir, tmpfiles, rafs) {
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
			});
			return 0;
		}
		return -1;
	}

	@Override
	public int extract() throws IOException
	{
		return extract(getTempDir(), null);
	}
	
	@Override
	public File extract(final String entry) throws IOException
	{
		// System.out.println("extract "+entry+" to "+new File(getTempDir(), entry));
		extract(getTempDir(), entry);
		val result = new File(getTempDir(), entry);
		if(result.exists())
			return result;
		return null;
	}

	@Override
	public InputStream extract_stdout(final String entry) throws IOException
	{
		// System.out.println("extract "+entry+" to "+new File(getTempDir(), entry)+" then send to stdout");
		extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(), entry));
	}

	@Override
	public int add(final File baseDir, final String entry) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("add "+new File(baseDir, entry)+" to "+new File(getTempDir(), entry));
		if(baseDir.isFile())
			FileUtils.copyFile(baseDir, new File(getTempDir(), entry));
		else if(!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		getToAdd().add(entry);
		return 0;
	}

	@Override
	public int add_stdin(final InputStream src, final String entry) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("add stdin to "+new File(getTempDir(), entry));
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		getToAdd().add(entry);
		return 0;
	}

	@Override
	public int delete(final String entry) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("delete "+normalize(entry));
		getToDelete().add(normalize(entry));
		return 0;
	}

	@Override
	public int rename(final String entry, final String newname) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("rename "+normalize(entry)+" to "+normalize(newname));
		getToRename().put(normalize(entry), normalize(newname));
		return 0;
	}

	@Override
	public int duplicate(final String entry, final String newname) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("duplicate "+normalize(entry)+" to "+normalize(newname));
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
