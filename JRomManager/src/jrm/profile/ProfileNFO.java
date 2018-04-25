package jrm.profile;

import java.io.*;
import java.util.Date;

@SuppressWarnings("serial")
public final class ProfileNFO implements Serializable
{
	public File file = null;
	public String name = null;
	public String version = null;
	public Long haveSets= null;
	public Long totalSets= null;
	public Long haveRoms = null;
	public Long totalRoms = null;
	public Long haveDisks = null;
	public Long totalDisks = null;
	public Date created = null;
	public Date scanned = null;
	public Date fixed = null;
	
	private ProfileNFO(File file)
	{
		this.file = file;
		this.name = file.getName();
		this.created = new Date();
	}

	private static File getFileNfo(File file)
	{
		return new File(file.getParentFile(), file.getName() + ".nfo");
	}
	
	public void relocate(File file)
	{
		getFileNfo(this.file).delete();
		this.file = file;
		this.name = file.getName();
		this.save();
	}
	
	public static ProfileNFO load(File file)
	{
		File filenfo = getFileNfo(file);
		if(filenfo.lastModified() >= file.lastModified()) //$NON-NLS-1$
		{
			try(ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filenfo))))
			{
				return (ProfileNFO) ois.readObject();
			}
			catch(Throwable e)
			{
			}
		}
		return new ProfileNFO(file);
	}
	
	public void save()
	{
		try(ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(getFileNfo(file)))))
		{
			oos.writeObject(this);
		}
		catch(Throwable e)
		{

		}
	}
}
