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
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.IOutCreateArchive7z;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItem7z;
import net.sf.sevenzipjbinding.IOutUpdateArchive7z;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public class SevenZipNArchive implements Archive
{
	private File archive;
	private File tempDir = null;
	private boolean readonly;
	private IInArchive iinarchive = null;
	private IInStream iinstream = null;
	
	private static HashMap<String,File> archives = new HashMap<>();
	private List<Closeable> closeables = new ArrayList<>();

	private List<String> to_add = new ArrayList<>();
	private List<String> to_delete = new ArrayList<>();
	private HashMap<String,String> to_rename = new HashMap<>();
	private HashMap<String,String> to_duplicate = new HashMap<>();

	public SevenZipNArchive(File archive) throws IOException, SevenZipNativeInitializationException
	{
		this(archive, false);
	//	System.out.println("SevenZipNArchive " + archive);
	}
	
	public SevenZipNArchive(File archive, boolean readonly) throws IOException, SevenZipNativeInitializationException
	{
		if(!SevenZip.isInitializedSuccessfully())
			SevenZip.initSevenZipFromPlatformJAR();
		if(archive.exists())
		{
			closeables.add(iinstream = new RandomAccessFileInStream(new RandomAccessFile(archive, "r")));
			closeables.add(iinarchive = SevenZip.openInArchive(null, iinstream));
		}
		this.readonly = readonly;
		if (null==(this.archive=archives.get(archive.getAbsolutePath())))
			archives.put(archive.getAbsolutePath(), this.archive = archive);
	}
	
	@Override
	public void close() throws IOException
	{
		if (!to_add.isEmpty() || !to_rename.isEmpty() || !to_delete.isEmpty() || !to_duplicate.isEmpty())
		{
			IOutCreateCallback<IOutItem7z> callback = new IOutCreateCallback<IOutItem7z>()
			{
				HashMap<Integer,String> idx_to_delete = new HashMap<>();
				HashMap<Integer,String> idx_to_rename = new HashMap<>();
				ArrayList<Object[]> idx_to_duplicate = new ArrayList<>();
				int delete_cnt = 0;
				int old_cnt = 0;
				HashMap<Integer,RandomAccessFile> rafs = new HashMap<>();
				HashMap<Integer,File> tmpfiles = new HashMap<>();
				int curr_index = -1;
				
				{
					if(iinarchive != null)
					{
						old_cnt = iinarchive.getNumberOfItems();
						for (int i = 0; i < iinarchive.getNumberOfItems(); i++)
						{
							for (String to_d : to_delete)
							{
								if (iinarchive.getProperty(i, PropID.PATH).equals(to_d))
								{
									idx_to_delete.put(i, to_d);
									break;
								}
							}
							for (Entry<String,String> to_r : to_rename.entrySet())
							{
								if (iinarchive.getProperty(i, PropID.PATH).equals(to_r.getKey()))
								{
								//	System.out.println("put "+i+" value "+to_r.getValue());
									idx_to_rename.put(i, to_r.getValue());
									break;
								}
							}
							for (Entry<String,String> to_p : to_duplicate.entrySet())
							{
								if (iinarchive.getProperty(i, PropID.PATH).equals(to_p.getKey()))
								{
									idx_to_duplicate.add(new Object[] {i,to_p.getValue()});
									break;
								}
							}
						}
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
					try
					{
						if (curr_index >= 0)
						{
							if (rafs.containsKey(curr_index))
								rafs.remove(curr_index).close();
							if (tmpfiles.containsKey(curr_index))
								tmpfiles.remove(curr_index).delete();
							curr_index = -1;
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
				
				@Override
				public ISequentialInStream getStream(int index) throws SevenZipException
				{
					curr_index = index;
					if(index + delete_cnt - old_cnt < to_add.size())
					{
						try
						{
							rafs.put(index, new RandomAccessFile(new File(getTempDir(),to_add.get(index + delete_cnt - old_cnt)), "r"));
							return new RandomAccessFileInStream(rafs.get(index));
						}
						catch (FileNotFoundException e)
						{
							e.printStackTrace();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					if(index + delete_cnt - old_cnt - to_add.size() < to_duplicate.size())
					{
						try
						{
							System.out.println("getStream:"+tmpfiles.get(index));
							rafs.put(index,new RandomAccessFile(tmpfiles.get(index), "r"));
							return new RandomAccessFileInStream(rafs.get(index));
						}
						catch (FileNotFoundException e)
						{
							e.printStackTrace();
						}
					}
					return null;
				}
				
				@Override
				public IOutItem7z getItemInformation(int index, OutItemFactory<IOutItem7z> outItemFactory) throws SevenZipException
				{
					if(idx_to_delete.containsKey(index + delete_cnt))
						delete_cnt++;
					if(index + delete_cnt < old_cnt)
					{
						if(idx_to_rename.containsKey(index + delete_cnt))
						{
						//	System.out.println("creating index "+index + delete_cnt+" and name it "+idx_to_rename.get(index + delete_cnt));
							IOutItem7z item = outItemFactory.createOutItemAndCloneProperties(index + delete_cnt);
							item.setPropertyPath(idx_to_rename.get(index + delete_cnt));
							return item;
						}
						return outItemFactory.createOutItem(index + delete_cnt);
					}
					if(index + delete_cnt - old_cnt < to_add.size())
					{
						String file = to_add.get(index + delete_cnt - old_cnt);
						IOutItem7z item = outItemFactory.createOutItem();
						item.setPropertyPath(file);
						try
						{
							item.setDataSize(new File(getTempDir(),file).length());
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						item.setUpdateIsNewData(true);
						item.setUpdateIsNewProperties(true);
						return item;
					}
					if(index + delete_cnt - old_cnt - to_add.size() < to_duplicate.size())
					{
						Object[] objects = idx_to_duplicate.get(index + delete_cnt - old_cnt - to_add.size());
						try
						{
							tmpfiles.put(index, Files.createTempFile("JRM", null).toFile());
							rafs.put(index, new RandomAccessFile(tmpfiles.get(index), "rw"));
							System.out.println("getItemInformation:"+tmpfiles.get(index));
							iinarchive.extractSlow((Integer)objects[0], new RandomAccessFileOutStream(rafs.get(index)));
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
						finally
						{
							try
							{
								if(rafs.get(index)!=null)
									rafs.get(index).close();
							}
							catch (IOException e)
							{
								e.printStackTrace();
							}
						}
						IOutItem7z item = outItemFactory.createOutItem();
						item.setPropertyPath((String)objects[1]);
						item.setDataSize(tmpfiles.get(index).length());
						item.setUpdateIsNewData(true);
						item.setUpdateIsNewProperties(true);
						return item;
					}
					return null;
				}
			};
			
			if(archive.exists() && iinarchive != null)
			{
			//	System.out.println("modifying archive "+archive);
				File tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", ".7z").toFile();
				tmpfile.delete();
				try(RandomAccessFile raf = new RandomAccessFile(tmpfile, "rw"))
				{
					IOutUpdateArchive7z iout = iinarchive.getConnectedOutArchive7z();
					iout.setSolid(false);
					iout.setLevel(5);
					iout.setThreadCount(1);

					int itemsCount = iinarchive.getNumberOfItems() - to_delete.size() + to_add.size() + to_duplicate.size();

					iout.updateItems(new RandomAccessFileOutStream(raf), itemsCount, callback);
				}
				for (Closeable c : closeables)
					c.close();
				closeables.clear();
			//	System.out.println("done with "+tmpfile);
				if(tmpfile.exists() && tmpfile.length()>0)
				{
				//	System.out.println("moving "+tmpfile+" to "+archive);
					archive.delete();
					if(!tmpfile.renameTo(archive))
						tmpfile.delete();
				}
			}
			else
			{
			//	System.out.println("creating archive "+archive);
				try (IOutCreateArchive7z iout = SevenZip.openOutArchive7z(); RandomAccessFile raf = new RandomAccessFile(archive, "rw"))
				{
					iout.setSolid(false);
					iout.setLevel(5);
					iout.setThreadCount(1);

					int itemsCount = to_add.size() + to_duplicate.size();

					iout.createArchive(new RandomAccessFileOutStream(raf), itemsCount, callback);
				}
				for (Closeable c : closeables)
					c.close();
				closeables.clear();
			}
		}
		else
		{
			for (Closeable c : closeables)
				c.close();
			closeables.clear();
		}
		try
		{
			if (tempDir != null)
				FileUtils.deleteDirectory(tempDir);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public File getTempDir() throws IOException
	{
		if (tempDir == null)
			tempDir = Files.createTempDirectory("JRM").toFile();
		return tempDir;
	}

	private int extract(File baseDir, String entry) throws IOException
	{
		 ISimpleInArchive simpleInArchive = iinarchive.getSimpleInterface();
		 for (ISimpleInArchiveItem item : simpleInArchive.getArchiveItems())
		 {
			 if(item.getPath().equals(entry))
			 {
				 try(RandomAccessFile out = new RandomAccessFile(new File(baseDir, entry), "rw"))
				 {
					 if(item.extractSlow(new RandomAccessFileOutStream(out))==ExtractOperationResult.OK)
						 return 0;
				 }
			 }
		 }
		 return -1;
	}

	@Override
	public File extract(String entry) throws IOException
	{
		System.out.println("extract "+entry+" to "+new File(getTempDir(), entry));
		extract(getTempDir(), entry);
		File result = new File(getTempDir(), entry);
		if (result.exists())
			return result;
		return null;
	}
	
	@Override
	public InputStream extract_stdout(String entry) throws IOException
	{
		System.out.println("extract "+entry+" to "+new File(getTempDir(), entry)+" then send to stdout");
		extract(getTempDir(), entry);
		return new FileInputStream(new File(getTempDir(),entry));
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
		System.out.println("add "+new File(baseDir, entry)+" to "+new File(getTempDir(), entry));
		if (!baseDir.equals(getTempDir()))
			FileUtils.copyFile(new File(baseDir, entry), new File(getTempDir(), entry));
		to_add.add(entry);
		return 0;
	}

	@Override
	public int add_stdin(InputStream src, String entry) throws IOException
	{
		if(readonly)
			return -1;
		System.out.println("add stdin to "+new File(getTempDir(), entry));
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		to_add.add(entry);
		return 0;
	}

	@Override
	public int delete(String entry) throws IOException
	{
		if(readonly)
			return -1;
		System.out.println("delete "+normalize(entry));
		to_delete.add(normalize(entry));
		return 0;
	}

	@Override
	public int rename(String entry, String newname) throws IOException
	{
		if(readonly)
			return -1;
		System.out.println("rename "+normalize(entry)+" to "+normalize(newname));
		to_rename.put(normalize(entry), normalize(newname));
		return 0;
	}

	@Override
	public int duplicate(String entry, String newname) throws IOException
	{
		if(readonly)
			return -1;
		System.out.println("duplicate "+normalize(entry)+" to "+normalize(newname));
		to_duplicate.put(normalize(entry), normalize(newname));
		return 0;
	}
	
	private String normalize(String entry)
	{
		if(File.separatorChar=='/')
			return entry.replace('\\', '/');
		return entry.replace('/', '\\');
	}

}
