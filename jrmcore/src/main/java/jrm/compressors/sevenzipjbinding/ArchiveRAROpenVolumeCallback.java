package jrm.compressors.sevenzipjbinding;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import net.sf.sevenzipjbinding.IArchiveOpenCallback;
import net.sf.sevenzipjbinding.IArchiveOpenVolumeCallback;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class ArchiveRAROpenVolumeCallback implements IArchiveOpenVolumeCallback, IArchiveOpenCallback
{
	private final Closeables closeables;

	public ArchiveRAROpenVolumeCallback(Closeables closeables)
	{
		this.closeables = closeables;
	}

	private Map<String, RandomAccessFile> openedRandomAccessFileList = new HashMap<>();
	private String name;

	@SuppressWarnings("exports")
	public Object getProperty(PropID propID) throws SevenZipException
	{
		if(PropID.NAME.equals(propID))
			return name;
		return null;
	}

	@SuppressWarnings("exports")
	public IInStream getStream(String filename) throws SevenZipException
	{
		try
		{
			var randomAccessFile = openedRandomAccessFileList.get(filename);
			if (randomAccessFile != null)
				randomAccessFile.seek(0);
			else
			{
				randomAccessFile = new RandomAccessFile(filename, "r");
				closeables.addCloseables(randomAccessFile);
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
			throw new RARException(e);
		}
	}

	@SuppressWarnings("serial")
	private class RARException extends RuntimeException
	{
		public RARException(Throwable e)
		{
			super(e);
		}
	}
	
	public void setCompleted(Long files, Long bytes) throws SevenZipException
	{
		// do nothing
	}

	public void setTotal(Long files, Long bytes) throws SevenZipException
	{
		// do nothing
	}
}