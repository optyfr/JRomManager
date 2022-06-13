package jrm.compressors.sevenzipjbinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import jrm.misc.IOUtils;
import jrm.misc.Log;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IOutCreateCallback;
import net.sf.sevenzipjbinding.IOutItemAllFormats;
import net.sf.sevenzipjbinding.ISequentialInStream;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.OutItemFactory;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.RandomAccessFileOutStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

public abstract class CloseCreateCallback implements IOutCreateCallback<IOutItemAllFormats>
{
	private final class ExtractCallback implements IArchiveExtractCallback
	{
		private final HashMap<Integer, RandomAccessFile> rafs2;

		private ExtractCallback(HashMap<Integer, RandomAccessFile> rafs2)
		{
			this.rafs2 = rafs2;
		}

		@Override
		public void setTotal(final long total) throws SevenZipException
		{
			// do nothing
		}

		@Override
		public void setCompleted(final long complete) throws SevenZipException
		{
			// do nothing
		}

		@Override
		public void setOperationResult(final ExtractOperationResult extractOperationResult) throws SevenZipException
		{
			// do nothing
		}

		@Override
		public void prepareOperation(final ExtractAskMode extractAskMode) throws SevenZipException
		{
			// do nothing
		}

		@Override
		public ISequentialOutStream getStream(final int idx, final ExtractAskMode extractAskMode) throws SevenZipException
		{
			if(ExtractAskMode.EXTRACT == extractAskMode)
				return new RandomAccessFileOutStream(rafs2.get(idx));
			return null;
		}
	}

	private final NArchiveBase nArchive;
	private final HashMap<Integer, File> tmpfiles;
	private final HashMap<Integer, RandomAccessFile> rafs;
	private HashMap<Integer, String> idxToDelete = new HashMap<>();
	private HashMap<Integer, String> idxToRename = new HashMap<>();
	private ArrayList<Object[]> idxToDuplicate = new ArrayList<>();
	private int oldIdx = 0;
	private int oldTot = 0;

	protected CloseCreateCallback(NArchiveBase nArchive, HashMap<Integer, File> tmpfiles, HashMap<Integer, RandomAccessFile> rafs) throws SevenZipException
	{
		this.nArchive = nArchive;
		this.tmpfiles = tmpfiles;
		this.rafs = rafs;
		if(this.nArchive.getIInArchive() == null)
			return;
		oldTot = this.nArchive.getIInArchive().getNumberOfItems();
		for(int i = 0; i < oldTot; i++)
		{
			final String path = this.nArchive.getIInArchive().getProperty(i, PropID.PATH).toString();
			if(this.nArchive.getToDelete().contains(path))
				idxToDelete.put(i, path);
			if(this.nArchive.getToRename().containsKey(path))
				idxToRename.put(i, this.nArchive.getToRename().get(path));
			for(final Entry<String, String> to_p : this.nArchive.getToCopy().entrySet())
				if(path.equals(to_p.getValue()))
					idxToDuplicate.add(new Object[] { i, to_p.getKey(), null });
		}
		if (this.nArchive.getToDelete().size() != idxToDelete.size())
			Log.err(() -> "to_delete:" + this.nArchive.getToDelete().size() + "!=" + idxToDelete.size()); //$NON-NLS-1$ //$NON-NLS-2$
		if (this.nArchive.getToRename().size() != idxToRename.size())
			Log.err(() -> "to_rename:" + this.nArchive.getToRename().size() + "!=" + idxToRename.size()); //$NON-NLS-1$ //$NON-NLS-2$
		if (this.nArchive.getToCopy().size() != idxToDuplicate.size())
			Log.err(() -> "to_duplicate:" + this.nArchive.getToCopy().size() + "!=" + idxToDuplicate.size()); //$NON-NLS-1$ //$NON-NLS-2$
	}


	@Override
	public void setOperationResult(final boolean operationResultOk) throws SevenZipException
	{
		// do nothing
	}

	@SuppressWarnings("exports")
	@Override
	public ISequentialInStream getStream(final int index) throws SevenZipException
	{
		if(index + idxToDelete.size() - oldTot < this.nArchive.getToAdd().size())
		{
			try
			{
				rafs.put(index, new RandomAccessFile(new File(this.nArchive.getTempDir(), this.nArchive.getToAdd().get(index + idxToDelete.size() - oldTot)), "r")); //$NON-NLS-1$
				return new RandomAccessFileInStream(rafs.get(index));
			}
			catch(final IOException e)
			{
				Log.err(e.getMessage(),e);
			}
		}
		if(index + idxToDelete.size() - oldTot - this.nArchive.getToAdd().size() >= this.nArchive.getToCopy().size())
			return null;
		try
		{
			if(!rafs.containsKey(index))
			{
				rebuildRafsMap();
			}
			rafs.get(index).seek(0);
			return new RandomAccessFileInStream(rafs.get(index));
		}
		catch(final IOException e)
		{
			Log.err(e.getMessage(),e);
		}
		return null;
	}


	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws SevenZipException
	 */
	private void rebuildRafsMap() throws IOException
	{
		final HashMap<Integer, File> tmpFilesByOldIndex = new HashMap<>();
		final HashMap<Integer, RandomAccessFile> rafs2 = new HashMap<>();
		for(final Object[] o : idxToDuplicate)
		{
			if(!tmpFilesByOldIndex.containsKey(o[0]))
				tmpFilesByOldIndex.put((Integer) o[0], IOUtils.createTempFile("JRM", null).toFile()); //$NON-NLS-1$
			tmpfiles.put((Integer) o[2], tmpFilesByOldIndex.get(o[0]));

		}
		for(final Entry<Integer, File> entry : tmpFilesByOldIndex.entrySet())
			rafs2.put(entry.getKey(), new RandomAccessFile(entry.getValue(), "rw")); //$NON-NLS-1$

		final int[] indices = idxToDuplicate.stream().flatMapToInt(objs -> IntStream.of((Integer) objs[0])).toArray();

		this.nArchive.getIInArchive().extract(indices, false, new ExtractCallback(rafs2));
		for(final RandomAccessFile raf2 : rafs2.values())
			raf2.close();
		
		for(final Entry<Integer, File> entry : tmpfiles.entrySet())
			rafs.put(entry.getKey(), new RandomAccessFile(entry.getValue(), "r")); //$NON-NLS-1$
	}

	@SuppressWarnings("exports")
	@Override
	public IOutItemAllFormats getItemInformation(final int index, final OutItemFactory<IOutItemAllFormats> outItemFactory) throws SevenZipException
	{
		try
		{
			while(idxToDelete.containsKey(oldIdx))
				oldIdx++;
			if(idxToRename.containsKey(oldIdx))
			{
				final IOutItemAllFormats item = outItemFactory.createOutItemAndCloneProperties(oldIdx);
				item.setPropertyPath(idxToRename.get(oldIdx));
				return item;
			}
			if(oldIdx < oldTot)
				return outItemFactory.createOutItem(oldIdx);
			else
			{
				if(oldIdx - oldTot < this.nArchive.getToAdd().size())
				{
					final String file = this.nArchive.getToAdd().get(oldIdx - oldTot);
					final IOutItemAllFormats item = outItemFactory.createOutItem();
					item.setPropertyPath(file);
					try
					{
						item.setDataSize(new File(this.nArchive.getTempDir(), file).length());
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
					final Object[] objects = idxToDuplicate.get(oldIdx - oldTot - this.nArchive.getToAdd().size());
					final ISimpleInArchiveItem refItem = this.nArchive.getIInArchive().getSimpleInterface().getArchiveItem((Integer) objects[0]);
					objects[2] = index;
					final IOutItemAllFormats item = outItemFactory.createOutItem();
					item.setPropertyPath((String) objects[1]);
					item.setDataSize(refItem.getSize());
					item.setUpdateIsNewData(true);
					item.setUpdateIsNewProperties(true);
					return item;
				}
			}
		}
		finally
		{
			oldIdx++;
		}
	}
}