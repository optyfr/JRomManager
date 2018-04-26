package jrm.profile;

import java.io.*;
import java.util.Date;

public final class ProfileNFO implements Serializable
{
	private static final long serialVersionUID = 1L;

	public File file = null;
	public String name = null;
	public ProfileNFOStats stats = new ProfileNFOStats();
	public ProfileNFOMame mame = new ProfileNFOMame();

	private static final ObjectStreamField[] serialPersistentFields = { 
		new ObjectStreamField("file", File.class),
		new ObjectStreamField("name", String.class),
		new ObjectStreamField("stats", ProfileNFOStats.class),
		new ObjectStreamField("mame", ProfileNFOMame.class),
	};

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("file", file);
		fields.put("name", name);
		fields.put("stats", stats);
		fields.put("mame", mame);
		stream.writeFields();
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		ObjectInputStream.GetField fields = stream.readFields();
		file = (File)fields.get("file", null);
		name = (String)fields.get("name", null);
		stats = (ProfileNFOStats)fields.get("stats", new ProfileNFOStats());
		mame = (ProfileNFOMame)fields.get("mame", new ProfileNFOMame());
	}
	
	private ProfileNFO(File file)
	{
		this.file = file;
		this.name = file.getName();
		this.stats.created = new Date();
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
		if(filenfo.lastModified() >= file.lastModified()) // $NON-NLS-1$
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
