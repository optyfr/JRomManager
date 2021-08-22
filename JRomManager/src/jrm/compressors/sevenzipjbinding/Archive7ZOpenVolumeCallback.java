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

	private Map<String, RandomAccessFile> openedRandomAccessFileList = new HashMap<>();

	@SuppressWarnings("exports")
	public Object getProperty(PropID propID) throws SevenZipException
	{
		return null;
	}

	@SuppressWarnings("exports")
	public IInStream getStream(String filename) throws SevenZipException
	{
		try
		{
			RandomAccessFile randomAccessFile = openedRandomAccessFileList.get(filename);
			if (randomAccessFile != null)
				randomAccessFile.seek(0);
			else
			{
				randomAccessFile = new RandomAccessFile(filename, "r");
				closeables.addCloseables(randomAccessFile);
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
			throw new Archive7ZVolCBException(e);
		}
	}
	
	@SuppressWarnings("serial")
	class Archive7ZVolCBException extends RuntimeException
	{
		public Archive7ZVolCBException(Throwable e)
		{
			super(e);
		}
	}
}