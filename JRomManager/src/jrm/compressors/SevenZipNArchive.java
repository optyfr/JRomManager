package jrm.compressors;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.IOutCreateArchive7z;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItem7z;
import net.sf.sevenzipjbinding.IOutUpdateArchive7z;
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
				for (String to_d : to_delete)
				{
					for (int i = 0; i < iinarchive.getNumberOfItems(); i++)
					{
						if (iinarchive.getProperty(i, PropID.PATH).equals(to_d))
						{
							idx_to_delete.put(i, to_d);
							break;
						}
					}
				}

				iout.updateItems(outStream, itemsCount, new IOutCreateCallback<IOutItem7z>()
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
					public ISequentialInStream getStream(int index) throws SevenZipException
					{
						return null;
					}
					
					@Override
					public IOutItem7z getItemInformation(int index, OutItemFactory<IOutItem7z> outItemFactory) throws SevenZipException
					{
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
		{
			tempDir = Files.createTempDirectory("JRM").toFile();
			SevenZip.openOutArchive7z();
			if (!archive.exists())
			{
				IOutCreateArchive7z iout = SevenZip.openOutArchive7z();
				RandomAccessFileOutStream raf = new RandomAccessFileOutStream(new RandomAccessFile(archive, "rw"));
				iout.createArchive(raf, 0, null);
				iout.close();
				raf.close();
			}
			if(archive.exists())
			{
				if(iinarchive==null)
				{
					closeables.add(iinstream = new RandomAccessFileInStream(new RandomAccessFile(archive, "r")));
					closeables.add(iinarchive = SevenZip.openInArchive(null, iinstream));
					
				}
			}
		}
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
