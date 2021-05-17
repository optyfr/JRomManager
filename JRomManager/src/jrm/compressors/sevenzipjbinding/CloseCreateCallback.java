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
	private final NArchiveBase nArchive;
	private final HashMap<Integer, File> tmpfiles;
	private final HashMap<Integer, RandomAccessFile> rafs;
	private HashMap<Integer, String> idx_to_delete = new HashMap<>();
	private HashMap<Integer, String> idx_to_rename = new HashMap<>();
	private ArrayList<Object[]> idx_to_duplicate = new ArrayList<>();
	private int old_idx = 0, old_tot = 0;
	// int curr_index = -1;

	public CloseCreateCallback(NArchiveBase nArchive, HashMap<Integer, File> tmpfiles, HashMap<Integer, RandomAccessFile> rafs) throws SevenZipException
	{
		this.nArchive = nArchive;
		this.tmpfiles = tmpfiles;
		this.rafs = rafs;
		if(this.nArchive.getIInArchive() != null)
		{
			old_tot = this.nArchive.getIInArchive().getNumberOfItems();
			for(int i = 0; i < old_tot; i++)
			{
				final String path = this.nArchive.getIInArchive().getProperty(i, PropID.PATH).toString();
				for(final String to_d : this.nArchive.getToDelete())
				{
					if(path.equals(to_d))
					{
						idx_to_delete.put(i, to_d);
						break;
					}
				}
				for(final Entry<String, String> to_r : this.nArchive.getToRename().entrySet())
				{
					if(path.equals(to_r.getKey()))
					{
						idx_to_rename.put(i, to_r.getValue());
						break;
					}
				}
				for(final Entry<String, String> to_p : this.nArchive.getToCopy().entrySet())
				{
					if(path.equals(to_p.getValue()))
					{
						idx_to_duplicate.add(new Object[] { i, to_p.getKey(), null });
					}
				}
			}
			if(this.nArchive.getToDelete().size() != idx_to_delete.size())
				System.err.println("to_delete:" + this.nArchive.getToDelete().size() + "!=" + idx_to_delete.size()); //$NON-NLS-1$ //$NON-NLS-2$
			if(this.nArchive.getToRename().size() != idx_to_rename.size())
				System.err.println("to_rename:" + this.nArchive.getToRename().size() + "!=" + idx_to_rename.size()); //$NON-NLS-1$ //$NON-NLS-2$
			if(this.nArchive.getToCopy().size() != idx_to_duplicate.size())
				System.err.println("to_duplicate:" + this.nArchive.getToCopy().size() + "!=" + idx_to_duplicate.size()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}


	@Override
	public void setOperationResult(final boolean operationResultOk) throws SevenZipException
	{
		/*
		 * System.out.println("setOperationResult "+curr_index); try { if (curr_index >= 0) { if (rafs.containsKey(-1)) rafs.remove(curr_index).close(); if (tmpfiles.containsKey(curr_index)) tmpfiles.remove(curr_index).delete();
		 * curr_index = -1; } } catch (IOException e) { Log.err(e.getMessage(),e); }
		 */
	}

	@Override
	public ISequentialInStream getStream(final int index) throws SevenZipException
	{
		// System.out.println("getStream "+index);
		// curr_index = index;
		if(index + idx_to_delete.size() - old_tot < this.nArchive.getToAdd().size())
		{
			try
			{
				// System.out.println("create raf for "+new File(getTempDir(),to_add.get(index + idx_to_delete.size() - old_tot)));
				rafs.put(index, new RandomAccessFile(new File(this.nArchive.getTempDir(), this.nArchive.getToAdd().get(index + idx_to_delete.size() - old_tot)), "r")); //$NON-NLS-1$
				return new RandomAccessFileInStream(rafs.get(index));
			}
			catch(final FileNotFoundException e)
			{
				Log.err(e.getMessage(),e);
			}
			catch(final IOException e)
			{
				Log.err(e.getMessage(),e);
			}
		}
		if(index + idx_to_delete.size() - old_tot - this.nArchive.getToAdd().size() < this.nArchive.getToCopy().size())
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
							tmpfiles_by_oldindex.put((Integer) o[0], IOUtils.createTempFile("JRM", null).toFile()); //$NON-NLS-1$
						tmpfiles.put((Integer) o[2], tmpfiles_by_oldindex.get(o[0]));

					}
					for(final Entry<Integer, File> entry : tmpfiles_by_oldindex.entrySet())
						rafs2.put(entry.getKey(), new RandomAccessFile(entry.getValue(), "rw")); //$NON-NLS-1$

					final int[] indices = idx_to_duplicate.stream().flatMapToInt(objs -> IntStream.of((Integer) objs[0])).toArray();
					// idx_to_duplicate.forEach(objs->System.out.println("will extract for "+objs[1]));
					this.nArchive.getIInArchive().extract(indices, false, new IArchiveExtractCallback()
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
				Log.err(e.getMessage(),e);
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
				if(old_idx - old_tot < this.nArchive.getToAdd().size())
				{
					final String file = this.nArchive.getToAdd().get(old_idx - old_tot);
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
					final Object[] objects = idx_to_duplicate.get(old_idx - old_tot - this.nArchive.getToAdd().size());
					final ISimpleInArchiveItem ref_item = this.nArchive.getIInArchive().getSimpleInterface().getArchiveItem((Integer) objects[0]);
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
}