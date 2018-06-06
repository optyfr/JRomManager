package jrm.profile;

import java.io.*;

import jrm.Messages;

public final class ProfileNFOMame implements Serializable
{
	private static final long serialVersionUID = 1L;

	private File file = null;
	private Long modified = null;
	private boolean sl = false;
	public File fileroms = null;
	public File filesl = null;

	private static final ObjectStreamField[] serialPersistentFields = {
			new ObjectStreamField("file", File.class), //$NON-NLS-1$
			new ObjectStreamField("modified", Long.class), //$NON-NLS-1$
			new ObjectStreamField("sl", Boolean.TYPE), //$NON-NLS-1$
			new ObjectStreamField("fileroms", File.class), //$NON-NLS-1$
			new ObjectStreamField("filesl", File.class), //$NON-NLS-1$
	};

	private void writeObject(final java.io.ObjectOutputStream stream) throws IOException
	{
		final ObjectOutputStream.PutField fields = stream.putFields();
		fields.put("file", file); //$NON-NLS-1$
		fields.put("modified", modified); //$NON-NLS-1$
		fields.put("sl", sl); //$NON-NLS-1$
		fields.put("fileroms", fileroms); //$NON-NLS-1$
		fields.put("filesl", filesl); //$NON-NLS-1$
		stream.writeFields();
	}

	private void readObject(final java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
		final ObjectInputStream.GetField fields = stream.readFields();
		file = (File) fields.get("file", null); //$NON-NLS-1$
		modified = (Long) fields.get("modified", null); //$NON-NLS-1$
		sl = fields.get("sl", false); //$NON-NLS-1$
		fileroms = (File)fields.get("fileroms", null); //$NON-NLS-1$
		filesl = (File)fields.get("filesl", null); //$NON-NLS-1$
	}

	public enum MameStatus
	{
		UNKNOWN(Messages.getString("ProfileNFOMame.Unknown")), //$NON-NLS-1$
		UPTODATE(Messages.getString("ProfileNFOMame.UpToDate")), //$NON-NLS-1$
		NEEDUPDATE(Messages.getString("ProfileNFOMame.NeedUpdate")), //$NON-NLS-1$
		NOTFOUND(Messages.getString("ProfileNFOMame.NotFound")); //$NON-NLS-1$

		private final String msg;

		private MameStatus(final String msg)
		{
			this.msg = msg;
		}

		public String getMsg()
		{
			return msg;
		}
	}

	public void set(final File mame, final boolean sl)
	{
		if(mame.exists())
		{
			file = mame;
			modified = mame.lastModified();
			this.sl = sl;
		}
	}

	public MameStatus getStatus()
	{
		if(file != null)
		{
			if(file.exists())
			{
				if(file.lastModified() > modified)
					return MameStatus.NEEDUPDATE;
				if(!fileroms.exists())
					return MameStatus.NEEDUPDATE;
				if(isSL() && !filesl.exists())
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
		modified = file.lastModified();
	}

	public MameStatus relocate(final File newFile)
	{
		if(file != null && newFile != null)
		{
			if(newFile.exists())
			{
				file = newFile;
				if(file.lastModified() > modified)
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

	public void delete()
	{
		if(fileroms!=null)
			fileroms.delete();
		if(filesl!=null)
			filesl.delete();
	}
}
