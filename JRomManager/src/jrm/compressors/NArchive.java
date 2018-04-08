package jrm.compressors;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
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
import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.IOutCreateArchive;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutFeatureSetLevel;
import net.sf.sevenzipjbinding.IOutFeatureSetMultithreading;
import net.sf.sevenzipjbinding.IOutFeatureSetSolid;
import net.sf.sevenzipjbinding.IOutItemAllFormats;
import net.sf.sevenzipjbinding.IOutUpdateArchive;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

abstract class NArchive implements Archive
{
	private File archive;
	private File tempDir = null;
	private boolean readonly;
	private IInArchive iinarchive = null;
	private IInStream iinstream = null;

	private static HashMap<String, File> archives = new HashMap<>();
	private List<Closeable> closeables = new ArrayList<>();

	private List<String> to_add = new ArrayList<>();
	private HashSet<String> to_delete = new HashSet<>();
	private HashMap<String, String> to_rename = new HashMap<>();
	private HashMap<String, String> to_duplicate = new HashMap<>();

	private ArchiveFormat format = ArchiveFormat.SEVEN_ZIP;
	private String ext = "7z";

	public NArchive(File archive) throws IOException, SevenZipNativeInitializationException
	{
		this(archive, false);
		// System.out.println("SevenZipNArchive " + archive);
	}

	public NArchive(File archive, boolean readonly) throws IOException, SevenZipNativeInitializationException
	{
		if(!SevenZip.isInitializedSuccessfully())
			SevenZip.initSevenZipFromPlatformJAR();
		ext = FilenameUtils.getExtension(archive.getName());
		if(archive.exists())
		{
			closeables.add(iinstream = new RandomAccessFileInStream(new RandomAccessFile(archive, "r")));
			closeables.add(iinarchive = SevenZip.openInArchive(null, iinstream));
			format = iinarchive.getArchiveFormat();
		}
		else
		{
			switch(ext.toLowerCase())
			{
				case "zip":
					format = ArchiveFormat.ZIP;
					break;
				case "7z":
				default:
					format = ArchiveFormat.SEVEN_ZIP;
					break;
			}
		}
		this.readonly = readonly;
		if(null == (this.archive = archives.get(archive.getAbsolutePath())))
			archives.put(archive.getAbsolutePath(), this.archive = archive);
	}

	@Override
	public void close() throws IOException
	{
		if(!to_add.isEmpty() || !to_rename.isEmpty() || !to_delete.isEmpty() || !to_duplicate.isEmpty())
		{
			HashMap<Integer, RandomAccessFile> rafs = new HashMap<>();
			HashMap<Integer, File> tmpfiles = new HashMap<>();

			IOutCreateCallback<IOutItemAllFormats> callback = new IOutCreateCallback<IOutItemAllFormats>()
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
							String path = iinarchive.getProperty(i, PropID.PATH).toString();
							for(String to_d : to_delete)
							{
								if(path.equals(to_d))
								{
									idx_to_delete.put(i, to_d);
									break;
								}
							}
							for(Entry<String, String> to_r : to_rename.entrySet())
							{
								if(path.equals(to_r.getKey()))
								{
									idx_to_rename.put(i, to_r.getValue());
									break;
								}
							}
							for(Entry<String, String> to_p : to_duplicate.entrySet())
							{
								if(path.equals(to_p.getValue()))
								{
									idx_to_duplicate.add(new Object[] { i, to_p.getKey(), null });
								}
							}
						}
						if(to_delete.size() != idx_to_delete.size())
							System.err.println("to_delete:" + to_delete.size() + "!=" + idx_to_delete.size());
						if(to_rename.size() != idx_to_rename.size())
							System.err.println("to_rename:" + to_rename.size() + "!=" + idx_to_rename.size());
						if(to_duplicate.size() != idx_to_duplicate.size())
							System.err.println("to_duplicate:" + to_duplicate.size() + "!=" + idx_to_duplicate.size());
					}
				}

				@Override
				public void setTotal(long total) throws SevenZipException
				{
				}

				@Override
				public void setCompleted(long complete) throws SevenZipException
				{
				}

				@Override
				public void setOperationResult(boolean operationResultOk) throws SevenZipException
				{
					/*
					 * System.out.println("setOperationResult "+curr_index); try { if (curr_index >= 0) { if (rafs.containsKey(-1)) rafs.remove(curr_index).close(); if (tmpfiles.containsKey(curr_index)) tmpfiles.remove(curr_index).delete();
					 * curr_index = -1; } } catch (IOException e) { e.printStackTrace(); }
					 */
				}

				@Override
				public ISequentialInStream getStream(int index) throws SevenZipException
				{
					// System.out.println("getStream "+index);
					// curr_index = index;
					if(index + idx_to_delete.size() - old_tot < to_add.size())
					{
						try
						{
							// System.out.println("create raf for "+new File(getTempDir(),to_add.get(index + idx_to_delete.size() - old_tot)));
							rafs.put(index, new RandomAccessFile(new File(getTempDir(), to_add.get(index + idx_to_delete.size() - old_tot)), "r"));
							return new RandomAccessFileInStream(rafs.get(index));
						}
						catch(FileNotFoundException e)
						{
							e.printStackTrace();
						}
						catch(IOException e)
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
								HashMap<Integer, File> tmpfiles_by_oldindex = new HashMap<>();
								HashMap<Integer, RandomAccessFile> rafs2 = new HashMap<>();
								for(Object[] o : idx_to_duplicate)
								{
									if(!tmpfiles_by_oldindex.containsKey(o[0]))
										tmpfiles_by_oldindex.put((Integer) o[0], Files.createTempFile("JRM", null).toFile());
									tmpfiles.put((Integer) o[2], tmpfiles_by_oldindex.get(o[0]));

								}
								for(Entry<Integer, File> entry : tmpfiles_by_oldindex.entrySet())
									rafs2.put(entry.getKey(), new RandomAccessFile(entry.getValue(), "rw"));

								int[] indices = idx_to_duplicate.stream().flatMapToInt(objs -> IntStream.of((Integer) objs[0])).toArray();
								// idx_to_duplicate.forEach(objs->System.out.println("will extract for "+objs[1]));
								iinarchive.extract(indices, false, new IArchiveExtractCallback()
								{

									@Override
									public void setTotal(long total) throws SevenZipException
									{
									}

									@Override
									public void setCompleted(long complete) throws SevenZipException
									{
									}

									@Override
									public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException
									{
									}

									@Override
									public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException
									{
									}

									@Override
									public ISequentialOutStream getStream(int idx, ExtractAskMode extractAskMode) throws SevenZipException
									{
										if(ExtractAskMode.EXTRACT == extractAskMode)
											return new RandomAccessFileOutStream(rafs2.get(idx));
										return null;
									}
								});
								for(RandomAccessFile raf2 : rafs2.values())
									raf2.close();
								for(Entry<Integer, File> entry : tmpfiles.entrySet())
									rafs.put(entry.getKey(), new RandomAccessFile(entry.getValue(), "r"));
							}
							rafs.get(index).seek(0);
							return new RandomAccessFileInStream(rafs.get(index));
						}
						catch(IOException e)
						{
							e.printStackTrace();
						}
					}
					return null;
				}

				@Override
				public IOutItemAllFormats getItemInformation(int index, OutItemFactory<IOutItemAllFormats> outItemFactory) throws SevenZipException
				{
					try
					{
						while(idx_to_delete.containsKey(old_idx))
							old_idx++;
						if(idx_to_rename.containsKey(old_idx))
						{
							IOutItemAllFormats item = outItemFactory.createOutItemAndCloneProperties(old_idx);
							item.setPropertyPath(idx_to_rename.get(old_idx));
							return item;
						}
						if(old_idx < old_tot)
							return outItemFactory.createOutItem(old_idx);
						else
						{
							if(old_idx - old_tot < to_add.size())
							{
								String file = to_add.get(old_idx - old_tot);
								IOutItemAllFormats item = outItemFactory.createOutItem();
								item.setPropertyPath(file);
								try
								{
									item.setDataSize(new File(getTempDir(), file).length());
								}
								catch(IOException e)
								{
									e.printStackTrace();
								}
								item.setUpdateIsNewData(true);
								item.setUpdateIsNewProperties(true);
								return item;
							}
							else
							{
								Object[] objects = idx_to_duplicate.get(old_idx - old_tot - to_add.size());
								ISimpleInArchiveItem ref_item = iinarchive.getSimpleInterface().getArchiveItem((Integer) objects[0]);
								objects[2] = index;
								IOutItemAllFormats item = outItemFactory.createOutItem();
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
				File tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", "." + ext).toFile();
				tmpfile.delete();
				try(RandomAccessFile raf = new RandomAccessFile(tmpfile, "rw"))
				{
					IOutUpdateArchive<IOutItemAllFormats> iout = iinarchive.getConnectedOutArchive();
					SetOptions(iout);

					int itemsCount = iinarchive.getNumberOfItems() - to_delete.size() + to_add.size() + to_duplicate.size();
					// System.err.println(itemsCount);
					iout.updateItems(new RandomAccessFileOutStream(raf), itemsCount, callback);
				}
				for(Closeable c : closeables)
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
				try(IOutCreateArchive<IOutItemAllFormats> iout = SevenZip.openOutArchive(format); RandomAccessFile raf = new RandomAccessFile(archive, "rw"))
				{
					SetOptions(iout);

					int itemsCount = to_add.size() + to_duplicate.size();

					iout.createArchive(new RandomAccessFileOutStream(raf), itemsCount, callback);
				}
				for(Closeable c : closeables)
					c.close();
				closeables.clear();
			}
			for(RandomAccessFile raf : rafs.values())
				raf.close();
			for(File tmpfile : tmpfiles.values())
				tmpfile.delete();
		}
		else
		{
			for(Closeable c : closeables)
				c.close();
			closeables.clear();
		}
		try
		{
			if(tempDir != null)
				FileUtils.deleteDirectory(tempDir);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private void SetOptions(Object iout) throws SevenZipException
	{
		switch(format)
		{
			case SEVEN_ZIP:
				if(iout instanceof IOutFeatureSetSolid)
					((IOutFeatureSetSolid) iout).setSolid(Settings.getProperty("7z_solid", true));
				if(iout instanceof IOutFeatureSetLevel)
					((IOutFeatureSetLevel) iout).setLevel(SevenZipOptions.valueOf(Settings.getProperty("7z_level", SevenZipOptions.NORMAL.toString())).getLevel());
				if(iout instanceof IOutFeatureSetMultithreading)
					((IOutFeatureSetMultithreading) iout).setThreadCount(Settings.getProperty("7z_threads", -1));
				break;
			case ZIP:
				if(iout instanceof IOutFeatureSetLevel)
					((IOutFeatureSetLevel) iout).setLevel(ZipOptions.valueOf(Settings.getProperty("zip_level", ZipOptions.NORMAL.toString())).getLevel());
				if(iout instanceof IOutFeatureSetMultithreading)
					((IOutFeatureSetMultithreading) iout).setThreadCount(Settings.getProperty("zip_threads", -1));
			default:
				break;
		}

	}

	@Override
	public File getTempDir() throws IOException
	{
		if(tempDir == null)
			tempDir = Files.createTempDirectory("JRM").toFile();
		return tempDir;
	}

	private int extract(File baseDir, String entry) throws IOException
	{
		ISimpleInArchive simpleInArchive = iinarchive.getSimpleInterface();
		for(ISimpleInArchiveItem item : simpleInArchive.getArchiveItems())
		{
			if(item.getPath().equals(entry))
			{
				try(RandomAccessFile out = new RandomAccessFile(new File(baseDir, entry), "rw"))
				{
					if(item.extractSlow(new RandomAccessFileOutStream(out)) == ExtractOperationResult.OK)
						return 0;
				}
			}
		}
		return -1;
	}

	@Override
	public File extract(String entry) throws IOException
	{
		// System.out.println("extract "+entry+" to "+new File(getTempDir(), entry));
		extract(getTempDir(), entry);
		File result = new File(getTempDir(), entry);
		if(result.exists())
			return result;
		return null;
	}

	@Override
	public InputStream extract_stdout(String entry) throws IOException
	{
		// System.out.println("extract "+entry+" to "+new File(getTempDir(), entry)+" then send to stdout");
		extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(), entry));
	}

	@Override
	public int add(String entry) throws IOException
	{
		return add(getTempDir(), entry);
	}

	@Override
	public int add(File baseDir, String entry) throws IOException
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
	public int add_stdin(InputStream src, String entry) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("add stdin to "+new File(getTempDir(), entry));
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		to_add.add(entry);
		return 0;
	}

	@Override
	public int delete(String entry) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("delete "+normalize(entry));
		to_delete.add(normalize(entry));
		return 0;
	}

	@Override
	public int rename(String entry, String newname) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("rename "+normalize(entry)+" to "+normalize(newname));
		to_rename.put(normalize(entry), normalize(newname));
		return 0;
	}

	@Override
	public int duplicate(String entry, String newname) throws IOException
	{
		if(readonly)
			return -1;
		// System.out.println("duplicate "+normalize(entry)+" to "+normalize(newname));
		to_duplicate.put(normalize(newname), normalize(entry));
		return 0;
	}

	private String normalize(String entry)
	{
		if(File.separatorChar == '/')
			return entry.replace('\\', '/');
		return entry.replace('/', '\\');
	}

}
