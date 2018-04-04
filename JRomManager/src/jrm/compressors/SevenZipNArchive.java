package jrm.compressors;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
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
	}
	
	public SevenZipNArchive(File archive, boolean readonly) throws IOException, SevenZipNativeInitializationException
	{
		if(!SevenZip.isInitializedSuccessfully())
			SevenZip.initSevenZipFromPlatformJAR();
		if (!archive.exists())
		{
			try (IOutCreateArchive7z iout = SevenZip.openOutArchive7z(); RandomAccessFile raf = new RandomAccessFile(archive, "rw"))
			{
				iout.setSolid(false);
				iout.setLevel(5);
				iout.setThreadCount(1);
				iout.createArchive(new RandomAccessFileOutStream(raf), 0, new IOutCreateCallback<IOutItem7z>()
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
					public void setOperationResult(boolean operationResultOk) throws SevenZipException
					{
					}

					@Override
					public IOutItem7z getItemInformation(int index, OutItemFactory<IOutItem7z> outItemFactory) throws SevenZipException
					{
						return null;
					}

					@Override
					public ISequentialInStream getStream(int index) throws SevenZipException
					{
						return null;
					}
				});
			}
		}
		closeables.add(iinstream = new RandomAccessFileInStream(new RandomAccessFile(archive, "r")));
		closeables.add(iinarchive = SevenZip.openInArchive(null, iinstream));
		this.readonly = readonly;
		if (null==(this.archive=archives.get(archive.getAbsolutePath())))
			archives.put(archive.getAbsolutePath(), this.archive = archive);
	}
	
	@Override
	public void close() throws IOException
	{
		if (tempDir != null)
		{
			Path tmpfile = Files.createTempFile(archive.getParentFile().toPath(), "JRM", ".7z");
			tmpfile.toFile().delete();
			try(RandomAccessFile raf = new RandomAccessFile(tmpfile.toFile(), "rw"))
			{
				int itemsCount = iinarchive.getNumberOfItems();
				itemsCount-=to_delete.size();
				itemsCount+=to_add.size();
				itemsCount+=to_duplicate.size();
				RandomAccessFileOutStream outStream  = new RandomAccessFileOutStream(raf);
				IOutUpdateArchive7z iout = iinarchive.getConnectedOutArchive7z();
				iout.setSolid(false);
				iout.setLevel(5);
				iout.setThreadCount(1);
				HashMap<Integer,String> idx_to_delete = new HashMap<>();
				HashMap<Integer,String> idx_to_rename = new HashMap<>();
				ArrayList<Object[]> idx_to_duplicate = new ArrayList<>();
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

				iout.updateItems(outStream, itemsCount, new IOutCreateCallback<IOutItem7z>()
				{
					int delete_cnt = 0;
					
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
					}
					
					@Override
					public ISequentialInStream getStream(int index) throws SevenZipException
					{
						if(index + delete_cnt - iinarchive.getNumberOfItems() < to_add.size())
						{
							String file = to_add.get(index + delete_cnt - iinarchive.getNumberOfItems());
							try
							{
								return new RandomAccessFileInStream(new RandomAccessFile(new File(getTempDir(),file), "r"));
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
						return null;
					}
					
					@Override
					public IOutItem7z getItemInformation(int index, OutItemFactory<IOutItem7z> outItemFactory) throws SevenZipException
					{
						if(idx_to_delete.containsKey(index + delete_cnt))
							delete_cnt++;
						if(index + delete_cnt < iinarchive.getNumberOfItems())
						{
							if(idx_to_rename.containsKey(index + delete_cnt))
							{
								IOutItem7z item = outItemFactory.createOutItemAndCloneProperties(index + delete_cnt);
								item.setPropertyPath(idx_to_rename.get(index + delete_cnt));
								return item;
							}
							return outItemFactory.createOutItem(index + delete_cnt);
						}
						if(index + delete_cnt - iinarchive.getNumberOfItems() < to_add.size())
						{
							IOutItem7z item = outItemFactory.createOutItem();
							item.setPropertyPath(to_add.get(index + delete_cnt - iinarchive.getNumberOfItems()));
							return item;
						}
						if(index + delete_cnt - iinarchive.getNumberOfItems() - to_add.size() < to_duplicate.size())
						{
							Object[] objects = idx_to_duplicate.get(index + delete_cnt - iinarchive.getNumberOfItems() - to_add.size());
							IOutItem7z item = outItemFactory.createOutItemAndCloneProperties((Integer)objects[0]);
							item.setPropertyPath((String)objects[1]);
							return item;
						}
						return null;
					}
				});
			}
			FileUtils.deleteDirectory(tempDir);
		}
		for (Closeable c : closeables)
			c.close();
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
		extract(getTempDir(), entry);
		File result = new File(getTempDir(), entry);
		if (result.exists())
			return result;
		return null;
	}
	
	@Override
	public InputStream extract_stdout(String entry) throws IOException
	{
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
		FileUtils.copyInputStreamToFile(src, new File(getTempDir(), entry));
		to_add.add(entry);
		return 0;
	}

	@Override
	public int delete(String entry) throws IOException
	{
		if(readonly)
			return -1;
		to_delete.add(entry);
		return 0;
	}

	@Override
	public int rename(String entry, String newname) throws IOException
	{
		if(readonly)
			return -1;
		to_rename.put(entry, newname);
		return 0;
	}

	@Override
	public int duplicate(String entry, String newname) throws IOException
	{
		if(readonly)
			return -1;
		to_duplicate.put(entry, newname);
		return 0;
	}

}
