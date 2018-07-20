package jrm.compressors;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import jrm.misc.Settings;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
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
 * @see {@link SevenZipNArchive}, {@link ZipNArchive}
 */
abstract class NArchive implements Archive
{
	private File archive;
	private File tempDir = null;
	private final boolean readonly;
	
	/*
	 * These fields are only used, if archive already exists, otherwise we are in creation mode
	 */
	private IInArchive iinarchive = null;
	private IInStream iinstream = null;

	private static HashMap<String, File> archives = new HashMap<>();
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
	public NArchive(final File archive) throws IOException, SevenZipNativeInitializationException
	{
		this(archive, false);
		// System.out.println("SevenZipNArchive " + archive);
	}

	/**
	 * Constructor with optional readonly mode
	 * @param archive {@link File} to archive
	 * @param readonly if true, will set archive in readonly safe mode
	 * @throws IOException
	 * @throws SevenZipNativeInitializationException in case of problem to find and initialize sevenzipjbinding native libraries
	 */
	public NArchive(final File archive, final boolean readonly) throws IOException, SevenZipNativeInitializationException
	{
		if(!SevenZip.isInitializedSuccessfully())
			SevenZip.initSevenZipFromPlatformJAR();
		ext = FilenameUtils.getExtension(archive.getName());
		if(archive.exists())
		{
			closeables.add(iinstream = new RandomAccessFileInStream(new RandomAccessFile(archive, "r"))); //$NON-NLS-1$
			closeables.add(iinarchive = SevenZip.openInArchive(null, iinstream));
			format = iinarchive.getArchiveFormat();
		}
		else
		{
			switch(ext.toLowerCase())
			{
				case "zip": //$NON-NLS-1$
					format = ArchiveFormat.ZIP;
					break;
				case "7z": //$NON-NLS-1$
				default:
					format = ArchiveFormat.SEVEN_ZIP;
					break;
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
					if(iinarchive != null)
					{
						old_tot = iinarchive.getNumberOfItems();
						for(int i = 0; i < old_tot; i++)
						{
							final String path = iinarchive.getProperty(i, PropID.PATH).toString();
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
				}

				@Override
				public void setCompleted(final long complete) throws SevenZipException
				{
				}

				@Override
				public void setOperationResult(final boolean operationResultOk) throws SevenZipException
				{
					/*
					 * System.out.println("setOperationResult "+curr_index); try { if (curr_index >= 0) { if (rafs.containsKey(-1)) rafs.remove(curr_index).close(); if (tmpfiles.containsKey(curr_index)) tmpfiles.remove(curr_index).delete();
					 * curr_index = -1; } } catch (IOException e) { e.printStackTrace(); }
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
							e.printStackTrace();
						}
						catch(final IOException e)
						{
							e.printStackTrace();
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
								iinarchive.extract(indices, false, new IArchiveExtractCallback()
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
							e.printStackTrace();
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
									e.printStackTrace();
								}
								item.setUpdateIsNewData(true);
								item.setUpdateIsNewProperties(true);
								return item;
							}
							else
							{
								final Object[] objects = idx_to_duplicate.get(old_idx - old_tot - to_add.size());
								final ISimpleInArchiveItem ref_item = iinarchive.getSimpleInterface().getArchiveItem((Integer) objects[0]);
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

			if(archive.exists() && iinarchive != null)
			{
				// System.out.println("modifying archive "+archive);
				final File tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", "." + ext).toFile(); //$NON-NLS-1$ //$NON-NLS-2$
				tmpfile.delete();
				try(RandomAccessFile raf = new RandomAccessFile(tmpfile, "rw")) //$NON-NLS-1$
				{
					final IOutUpdateArchive<IOutItemAllFormats> iout = iinarchive.getConnectedOutArchive();
					SetOptions(iout);

					final int itemsCount = iinarchive.getNumberOfItems() - to_delete.size() + to_add.size() + to_duplicate.size();
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
			e.printStackTrace();
		}
	}

	/**
	 * Mapper between SevenZipJBinding options and {@link Settings}
	 * @param iout the archive feature to map (see code to know what is supported)
	 * @throws SevenZipException
	 */
	private void SetOptions(final Object iout) throws SevenZipException
	{
		switch(format)
		{
			case SEVEN_ZIP:
				if(iout instanceof IOutFeatureSetSolid)
					((IOutFeatureSetSolid) iout).setSolid(Settings.getProperty("7z_solid", true)); //$NON-NLS-1$
				if(iout instanceof IOutFeatureSetLevel)
					((IOutFeatureSetLevel) iout).setLevel(SevenZipOptions.valueOf(Settings.getProperty("7z_level", SevenZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$
				if(iout instanceof IOutFeatureSetMultithreading)
					((IOutFeatureSetMultithreading) iout).setThreadCount(Settings.getProperty("7z_threads", -1)); //$NON-NLS-1$
				break;
			case ZIP:
				if(iout instanceof IOutFeatureSetLevel)
					((IOutFeatureSetLevel) iout).setLevel(ZipOptions.valueOf(Settings.getProperty("zip_level", ZipOptions.NORMAL.toString())).getLevel()); //$NON-NLS-1$
				if(iout instanceof IOutFeatureSetMultithreading)
					((IOutFeatureSetMultithreading) iout).setThreadCount(Settings.getProperty("zip_threads", -1)); //$NON-NLS-1$
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
		final ISimpleInArchive simpleInArchive = iinarchive.getSimpleInterface();
		for(final ISimpleInArchiveItem item : simpleInArchive.getArchiveItems())
		{
			if(item.getPath().equals(entry))
			{
				try(RandomAccessFile out = new RandomAccessFile(new File(baseDir, entry), "rw")) //$NON-NLS-1$
				{
					if(item.extractSlow(new RandomAccessFileOutStream(out)) == ExtractOperationResult.OK)
						return 0;
				}
			}
		}
		return -1;
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
		if(!baseDir.equals(getTempDir()))
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
