package jrm.profile;

import java.io.*;

public final class ProfileNFOMame implements Serializable
{
	private static final long serialVersionUID = 1L;

	private File file = null;
	private Long modified = null;
	private boolean sl = false;

	private static final ObjectStreamField[] serialPersistentFields = { 
		new ObjectStreamField("file", File.class), 
		new ObjectStreamField("modified", Long.class),
		new ObjectStreamField("sl", Boolean.TYPE)
	};

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("file", file);
		fields.put("modified", modified);
		fields.put("sl", sl);
		stream.writeFields();
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		ObjectInputStream.GetField fields = stream.readFields();
		file = (File) fields.get("file", null);
		modified = (Long) fields.get("modified", null);
		sl = fields.get("sl", false);
	}

	public enum MameStatus
	{
		UNKNOWN,
		UPTODATE,
		NEEDUPDATE,
		NOTFOUND
	}

	public void set(File mame, boolean sl)
	{
		if(mame.exists())
		{
			this.file = mame;
			this.modified = mame.lastModified();
			this.sl = sl;
		}
	}

	public MameStatus getStatus()
	{
		if(this.file != null)
		{
			if(this.file.exists())
			{
				if(this.file.lastModified() > this.modified)
					return MameStatus.NEEDUPDATE;
				return MameStatus.UPTODATE;
			}
			return MameStatus.NOTFOUND;
		}
		return MameStatus.UNKNOWN;
	}

	public File getFile()
	{
		return file;
	}

	public void setUpdated()
	{
		this.modified = this.file.lastModified();
	}

	public MameStatus relocate(File newFile)
	{
		if(this.file != null && newFile != null)
		{
			if(newFile.exists())
			{
				this.file = newFile;
				if(this.file.lastModified() > this.modified)
					return MameStatus.NEEDUPDATE;
				return MameStatus.UPTODATE;
			}
			return MameStatus.NOTFOUND;
		}
		return MameStatus.UNKNOWN;
	}

	public boolean isSL()
	{
		return sl;
	}
}
