package jrm.compressors.sevenzipjbinding;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import net.sf.sevenzipjbinding.IArchiveOpenVolumeCallback;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class Archive7ZOpenVolumeCallback implements IArchiveOpenVolumeCallback
{
	private final Closeables closeables;

	public Archive7ZOpenVolumeCallback(Closeables closeables)
	{
		this.closeables = closeables;
	}

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
				closeables.addCloseables(randomAccessFile = new RandomAccessFile(filename, "r"));
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