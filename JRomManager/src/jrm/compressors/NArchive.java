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

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import jrm.misc.GlobalSettings;
import jrm.misc.Log;
import jrm.security.Session;
import jrm.ui.progress.ProgressNarchiveCallBack;
import lombok.Getter;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.impl.VolumedArchiveInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

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
abstract class NArchive implements Archive
{
	private Session session;
	private File archive;
	private File tempDir = null;
	private final boolean readonly;
	private ProgressNarchiveCallBack cb = null;
	
	/*
	 * These fields are only used, if archive already exists, otherwise we are in creation mode
	 */
	private @Getter IInArchive iInArchive = null;
	private IInStream iInStream = null;

	private final static HashMap<String, File> archives = new HashMap<>();
	private final List<Closeable> closeables = new ArrayList<>();

	/*
	 * This is where all directives are stored until final archive modification upon archive closing
	 */
	private final List<String> to_add = new ArrayList<>();
	private final HashSet<String> to_delete = new HashSet<>();
	private final HashMap<String, String> to_rename = new HashMap<>();
	private final HashMap<String, String> to_duplicate = new HashMap<>();

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
			SevenZip.initSevenZipFromPlatformJAR(session.getUser().settings.getTmpPath(true).toFile());
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
				ArchiveRAROpenVolumeCallback archiveOpenVolumeCallback = new ArchiveRAROpenVolumeCallback();
				closeables.add(iInArchive = SevenZip.openInArchive(format, iInStream = archiveOpenVolumeCallback.getStream(archive.getAbsolutePath()), archiveOpenVolumeCallback));
			}
			else if(format==ArchiveFormat.SEVEN_ZIP && archive.getName().endsWith(".7z.001"))	// SevenZip multipart
			{
				closeables.add(iInArchive = SevenZip.openInArchive(format, new VolumedArchiveInStream(archive.getAbsolutePath(), new Archive7ZOpenVolumeCallback())));
			}
			else	// auto detect
			{
				closeables.add(iInStream = new RandomAccessFileInStream(new RandomAccessFile(archive, "r"))); //$NON-NLS-1$
				closeables.add(iInArchive = SevenZip.openInArchive(null, iInStream));
				format = iInArchive.getArchiveFormat();
			}
		}
		this.readonly = readonly;
		if(null == (this.archive = NArchive.archives.get(archive.getAbsolutePath())))
			NArchive.archives.put(archive.getAbsolutePath(), this.archive = archive);
	}

	private class Archive7ZOpenVolumeCallback implements IArchiveOpenVolumeCallback
	{
		private Map<String, RandomAccessFile> openedRandomAccessFileList = new HashMap<String, RandomAccessFile>();

		public Object getProperty(PropID propID) throws SevenZipException
		{
			return null;
		}

		public IInStream getStream(String filename) throws SevenZipException
		{
			try
			{
				RandomAccessFile randomAccessFile = openedRandomAccessFileList.get(filename);
				if (randomAccessFile != null)
					randomAccessFile.seek(0);
				else
				{
					closeables.add(randomAccessFile = new RandomAccessFile(filename, "r"));
					openedRandomAccessFileList.put(filename, randomAccessFile);					
				}
				return new RandomAccessFileInStream(randomAccessFile);
			}
			catch (FileNotFoundException fileNotFoundException)
			{
				return null; // We return always null in this case
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	private class ArchiveRAROpenVolumeCallback implements IArchiveOpenVolumeCallback, IArchiveOpenCallback
	{
		private Map<String, RandomAccessFile> openedRandomAccessFileList = new HashMap<String, RandomAccessFile>();
		private String name;

		@SuppressWarnings("incomplete-switch")
		public Object getProperty(PropID propID) throws SevenZipException
		{
			switch (propID)
			{
				case NAME:
					return name;
			}
			return null;
		}

		public IInStream getStream(String filename) throws SevenZipException
		{
			try
			{
				RandomAccessFile randomAccessFile = openedRandomAccessFileList.get(filename);
				if (randomAccessFile != null)
					randomAccessFile.seek(0);
				else
				{
					closeables.add(randomAccessFile = new RandomAccessFile(filename, "r"));
					openedRandomAccessFileList.put(filename, randomAccessFile);
				}
				name = filename;
				return new RandomAccessFileInStream(randomAccessFile);
			}
			catch (FileNotFoundException fileNotFoundException)
			{
				return null; // We return always null in this case
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		public void setCompleted(Long files, Long bytes) throws SevenZipException
		{
		}

		public void setTotal(Long files, Long bytes) throws SevenZipException
		{
		}
	}
	
	/**
	 * This is where all operations really take place! Almost all is inside {@link IOutCreateCallback} callback,
	 * then we are using {@link IOutUpdateArchive} or {@link IOutCreateArchive} in case of creation mode (where archive does not already exist)  
	 */
	@Override
	public void close() throws IOException
	{
		if(!to_add.isEmpty() || !to_rename.isEmpty() || !to_delete.isEmpty() || !to_duplicate.isEmpty())
		{
			final HashMap<Integer, RandomAccessFile> rafs = new HashMap<>();
			final HashMap<Integer, File> tmpfiles = new HashMap<>();

			final IOutCreateCallback<IOutItemAllFormats> callback = new IOutCreateCallback<IOutItemAllFormats>()
			{
				HashMap<Integer, String> idx_to_delete = new HashMap<>();
				HashMap<Integer, String> idx_to_rename = new HashMap<>();
				ArrayList<Object[]> idx_to_duplicate = new ArrayList<>();
				int old_idx = 0, old_tot = 0;
				// int curr_index = -1;

				// anonymous constructor
				{
					if(iInArchive != null)
					{
						old_tot = iInArchive.getNumberOfItems();
						for(int i = 0; i < old_tot; i++)
						{
							final String path = iInArchive.getProperty(i, PropID.PATH).toString();
							for(final String to_d : to_delete)
							{
								if(path.equals(to_d))
								{
									idx_to_delete.put(i, to_d);
									break;
								}
							}
							for(final Entry<String, String> to_r : to_rename.entrySet())
							{
								if(path.equals(to_r.getKey()))
								{
									idx_to_rename.put(i, to_r.getValue());
									break;
								}
							}
							for(final Entry<String, String> to_p : to_duplicate.entrySet())
							{
								if(path.equals(to_p.getValue()))
								{
									idx_to_duplicate.add(new Object[] { i, to_p.getKey(), null });
								}
							}
						}
						if(to_delete.size() != idx_to_delete.size())
							System.err.println("to_delete:" + to_delete.size() + "!=" + idx_to_delete.size()); //$NON-NLS-1$ //$NON-NLS-2$
						if(to_rename.size() != idx_to_rename.size())
							System.err.println("to_rename:" + to_rename.size() + "!=" + idx_to_rename.size()); //$NON-NLS-1$ //$NON-NLS-2$
						if(to_duplicate.size() != idx_to_duplicate.size())
							System.err.println("to_duplicate:" + to_duplicate.size() + "!=" + idx_to_duplicate.size()); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

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

				@Override
				public void setOperationResult(final boolean operationResultOk) throws SevenZipException
				{
					/*
					 * System.out.println("setOperationResult "+curr_index); try { if (curr_index >= 0) { if (rafs.containsKey(-1)) rafs.remove(curr_index).close(); if (tmpfiles.containsKey(curr_index)) tmpfiles.remove(curr_index).delete();
					 * curr_index = -1; } } catch (IOException e) { Log.err(e.getMessage(),e); }
					 */
				}

				@Override
				public ISequentialInStream getStream(final int index) throws SevenZipException
				{
					// System.out.println("getStream "+index);
					// curr_index = index;
					if(index + idx_to_delete.size() - old_tot < to_add.size())
					{
						try
						{
							// System.out.println("create raf for "+new File(getTempDir(),to_add.get(index + idx_to_delete.size() - old_tot)));
							rafs.put(index, new RandomAccessFile(new File(getTempDir(), to_add.get(index + idx_to_delete.size() - old_tot)), "r")); //$NON-NLS-1$
							return new RandomAccessFileInStream(rafs.get(index));
						}
						catch(final FileNotFoundException e)
						{
							Log.err(e.getMessage(),e);
						}
						catch(final IOException e)
						{
							Log.err(e.getMessage(),e);
						}
					}
					if(index + idx_to_delete.size() - old_tot - to_add.size() < to_duplicate.size())
					{
						try
						{
							if(!rafs.containsKey(index))
							{
								final HashMap<Integer, File> tmpfiles_by_oldindex = new HashMap<>();
								final HashMap<Integer, RandomAccessFile> rafs2 = new HashMap<>();
								for(final Object[] o : idx_to_duplicate)
								{
									if(!tmpfiles_by_oldindex.containsKey(o[0]))
										tmpfiles_by_oldindex.put((Integer) o[0], Files.createTempFile("JRM", null).toFile()); //$NON-NLS-1$
									tmpfiles.put((Integer) o[2], tmpfiles_by_oldindex.get(o[0]));

								}
								for(final Entry<Integer, File> entry : tmpfiles_by_oldindex.entrySet())
									rafs2.put(entry.getKey(), new RandomAccessFile(entry.getValue(), "rw")); //$NON-NLS-1$

								final int[] indices = idx_to_duplicate.stream().flatMapToInt(objs -> IntStream.of((Integer) objs[0])).toArray();
								// idx_to_duplicate.forEach(objs->System.out.println("will extract for "+objs[1]));
								iInArchive.extract(indices, false, new IArchiveExtractCallback()
								{

									@Override
									public void setTotal(final long total) throws SevenZipException
									{
									}

									@Override
									public void setCompleted(final long complete) throws SevenZipException
									{
									}

									@Override
									public void setOperationResult(final ExtractOperationResult extractOperationResult) throws SevenZipException
									{
									}

									@Override
									public void prepareOperation(final ExtractAskMode extractAskMode) throws SevenZipException
									{
									}

									@Override
									public ISequentialOutStream getStream(final int idx, final ExtractAskMode extractAskMode) throws SevenZipException
									{
										if(ExtractAskMode.EXTRACT == extractAskMode)
											return new RandomAccessFileOutStream(rafs2.get(idx));
										return null;
									}
								});
								for(final RandomAccessFile raf2 : rafs2.values())
									raf2.close();
								for(final Entry<Integer, File> entry : tmpfiles.entrySet())
									rafs.put(entry.getKey(), new RandomAccessFile(entry.getValue(), "r")); //$NON-NLS-1$
							}
							rafs.get(index).seek(0);
							return new RandomAccessFileInStream(rafs.get(index));
						}
						catch(final IOException e)
						{
							Log.err(e.getMessage(),e);
						}
					}
					return null;
				}

				@Override
				public IOutItemAllFormats getItemInformation(final int index, final OutItemFactory<IOutItemAllFormats> outItemFactory) throws SevenZipException
				{
					try
					{
						while(idx_to_delete.containsKey(old_idx))
							old_idx++;
						if(idx_to_rename.containsKey(old_idx))
						{
							final IOutItemAllFormats item = outItemFactory.createOutItemAndCloneProperties(old_idx);
							item.setPropertyPath(idx_to_rename.get(old_idx));
							return item;
						}
						if(old_idx < old_tot)
							return outItemFactory.createOutItem(old_idx);
						else
						{
							if(old_idx - old_tot < to_add.size())
							{
								final String file = to_add.get(old_idx - old_tot);
								final IOutItemAllFormats item = outItemFactory.createOutItem();
								item.setPropertyPath(file);
								try
								{
									item.setDataSize(new File(getTempDir(), file).length());
								}
								catch(final IOException e)
								{
									Log.err(e.getMessage(),e);
								}
								item.setUpdateIsNewData(true);
								item.setUpdateIsNewProperties(true);
								return item;
							}
							else
							{
								final Object[] objects = idx_to_duplicate.get(old_idx - old_tot - to_add.size());
								final ISimpleInArchiveItem ref_item = iInArchive.getSimpleInterface().getArchiveItem((Integer) objects[0]);
								objects[2] = index;
								final IOutItemAllFormats item = outItemFactory.createOutItem();
								item.setPropertyPath((String) objects[1]);
								item.setDataSize(ref_item.getSize());
								item.setUpdateIsNewData(true);
								item.setUpdateIsNewProperties(true);
								return item;
							}
						}
					}
					finally
					{
						old_idx++;
					}
				}
			};

			if(archive.exists() && iInArchive != null)
			{
				// System.out.println("modifying archive "+archive);
				final File tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", "." + ext).toFile(); //$NON-NLS-1$ //$NON-NLS-2$
				tmpfile.delete();
				try(RandomAccessFile raf = new RandomAccessFile(tmpfile, "rw")) //$NON-NLS-1$
				{
					final IOutUpdateArchive<IOutItemAllFormats> iout = iInArchive.getConnectedOutArchive();
					SetOptions(iout);

					final int itemsCount = iInArchive.getNumberOfItems() - to_delete.size() + to_add.size() + to_duplicate.size();
					// System.err.println(itemsCount);
					iout.updateItems(new RandomAccessFileOutStream(raf), itemsCount, callback);
				}
				for(final Closeable c : closeables)
					c.close();
				closeables.clear();
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
				try(IOutCreateArchive<IOutItemAllFormats> iout = SevenZip.openOutArchive(format); RandomAccessFile raf = new RandomAccessFile(archive, "rw")) //$NON-NLS-1$
				{
					SetOptions(iout);

					final int itemsCount = to_add.size() + to_duplicate.size();

					iout.createArchive(new RandomAccessFileOutStream(raf), itemsCount, callback);
				}
				for(final Closeable c : closeables)
					c.close();
				closeables.clear();
			}
			for(final RandomAccessFile raf : rafs.values())
				raf.close();
			for(final File tmpfile : tmpfiles.values())
				tmpfile.delete();
		}
		else
		{
			for(final Closeable c : closeables)
				c.close();
			closeables.clear();
		}
		try
		{
			if(tempDir != null)
				FileUtils.deleteDirectory(tempDir);
		}
		catch(final Exception e)
		{
			Log.err(e.getMessage(),e);
		}
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
					((IOutFeatureSetSolid) iout).setSolid(session.getUser().settings.getProperty("7z_solid", true)); //$NON-NLS-1$
				if(iout instanceof IOutFeatureSetLevel)
					((IOutFeatureSetLevel) iout).setLevel(SevenZipOptions.valueOf(session.getUser().settings.getProperty("7z_level", SevenZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$
				if(iout instanceof IOutFeatureSetMultithreading)
					((IOutFeatureSetMultithreading) iout).setThreadCount(session.getUser().settings.getProperty("7z_threads", -1)); //$NON-NLS-1$
				break;
			case ZIP:
				if(iout instanceof IOutFeatureSetLevel)
					((IOutFeatureSetLevel) iout).setLevel(ZipOptions.valueOf(session.getUser().settings.getProperty("zip_level", ZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$
				if(iout instanceof IOutFeatureSetMultithreading)
					((IOutFeatureSetMultithreading) iout).setThreadCount(session.getUser().settings.getProperty("zip_threads", -1)); //$NON-NLS-1$
			default:
				break;
		}

	}

	@Override
	public File getTempDir() throws IOException
	{
		if(tempDir == null)
			tempDir = Files.createTempDirectory("JRM").toFile(); //$NON-NLS-1$
		return tempDir;
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
			final ISimpleInArchive simpleInArchive = iInArchive.getSimpleInterface();
			for(final ISimpleInArchiveItem item : simpleInArchive.getArchiveItems())
			{
				if(item.getPath().equals(entry))
				{
					File file = new File(baseDir, entry);
					FileUtils.forceMkdirParent(file);
					try(RandomAccessFile out = new RandomAccessFile(file, "rw")) //$NON-NLS-1$
					{
						if(item.extractSlow(new RandomAccessFileOutStream(out)) == ExtractOperationResult.OK)
							return 0;
					}
				}
			}
		}
		else
		{
			final HashMap<Integer, File> tmpfiles = new HashMap<>();
			final HashMap<Integer, RandomAccessFile> rafs = new HashMap<>();
			final int[] in = new int[iInArchive.getNumberOfItems()];
			for (int i = 0; i < in.length; i++)
			{
				in[i] = i;
				if(!(Boolean)iInArchive.getProperty(i, PropID.IS_FOLDER))
				{
					final File file = Files.createTempFile("JRM", null).toFile();
					tmpfiles.put(i, file); //$NON-NLS-1$
					rafs.put(i, new RandomAccessFile(file, "rw")); //$NON-NLS-1$
				}
				else
				{
					File dir = new File(baseDir, (String) iInArchive.getProperty(i, PropID.PATH));
					FileUtils.forceMkdir(dir);
				}
			}
			
			iInArchive.extract(in, false, new IArchiveExtractCallback()
			{
				private boolean skipExtraction;
				private int index;
				
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
				
				@Override
				public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException
				{
				}
				
				@Override
				public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException
				{
					if (skipExtraction)
						return;
					if (extractOperationResult != ExtractOperationResult.OK)
						System.err.println("Extraction error");
					else
					{
						try
						{
							rafs.get(index).close();
							String path  = (String) iInArchive.getProperty(index, PropID.PATH);
							File tmpfile = tmpfiles.get(index);
							File dstfile = new File(baseDir,path);
							FileUtils.forceMkdirParent(dstfile);
							if(!dstfile.exists())
								FileUtils.moveFile(tmpfile, dstfile);
						}
						catch (IOException e)
						{
							Log.err(e.getMessage(),e);
						}
					}
				}
				
				@Override
				public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException
				{
					this.index = index;
					skipExtraction = (Boolean) iInArchive.getProperty(index, PropID.IS_FOLDER);
					if (skipExtraction || extractAskMode != ExtractAskMode.EXTRACT)
						return null;
					return new ISequentialOutStream()
					{
						@Override
						public int write(byte[] data) throws SevenZipException
						{
							try
							{
								rafs.get(index).write(data);
								return data.length;
							}
							catch (IOException e)
							{
								Log.err(e.getMessage(),e);
							}
							return 0;
						}
					};
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
		final File result = new File(getTempDir(), entry);
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
		to_add.add(entry);
		return 0;
	}

	@Override
	public int add_stdin(final InputStream src, final String entry) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("add stdin to "+new File(getTempDir(), entry));
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		to_add.add(entry);
		return 0;
	}

	@Override
	public int delete(final String entry) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("delete "+normalize(entry));
		to_delete.add(normalize(entry));
		return 0;
	}

	@Override
	public int rename(final String entry, final String newname) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("rename "+normalize(entry)+" to "+normalize(newname));
		to_rename.put(normalize(entry), normalize(newname));
		return 0;
	}

	@Override
	public int duplicate(final String entry, final String newname) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("duplicate "+normalize(entry)+" to "+normalize(newname));
		to_duplicate.put(normalize(newname), normalize(entry));
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
