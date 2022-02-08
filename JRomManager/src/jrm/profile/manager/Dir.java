package jrm.profile.manager;

import java.io.File;

public class Dir
{
	private final File file;
	private final String name;

	public Dir(final File file)
	{
		this.file = file;
		name = file.getName();
		if(!this.file.exists())
			this.file.mkdirs();
	}

	public Dir(final File file, final String name)
	{
		this.file = file;
		this.name = name;
	}

	public File getFile()
	{
		return file;
	}

	@Override
	public String toString()
	{
		return name;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof Dir d)
			return file.equals(d.file);
		return false;
	}
	
	@Override
	public int hashCode()
	{
		return file.hashCode();
	}
	
	public Dir renameTo(File newFile)
	{
		if(this.file.renameTo(newFile))
			return new Dir(newFile);
		return this;
	}
}