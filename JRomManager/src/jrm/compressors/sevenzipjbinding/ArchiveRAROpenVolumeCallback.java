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
	/**
	 * 
	 */
	private final Closeables closeables;

	/**
	 * @param nArchive
	 */
	public ArchiveRAROpenVolumeCallback(Closeables closeables)
	{
		this.closeables = closeables;
	}

	private Map<String, RandomAccessFile> openedRandomAccessFileList = new HashMap<String, RandomAccessFile>();
	private String name;

	public Object getProperty(PropID propID) throws SevenZipException
	{
		switch (propID)
		{
			case NAME:
				return name;
			default:
				return null;
		}
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