package jrm.compressors;

import jrm.Messages;

public enum SevenZipOptions
{
	STORE(Messages.getString("SevenZipOptions.STORE"), 0), //$NON-NLS-1$
	FASTEST(Messages.getString("SevenZipOptions.FASTEST"), 1), //$NON-NLS-1$
	FAST(Messages.getString("SevenZipOptions.FAST"), 3), //$NON-NLS-1$
	NORMAL(Messages.getString("SevenZipOptions.NORMAL"), 5), //$NON-NLS-1$
	MAXIMUM(Messages.getString("SevenZipOptions.MAXIMUM"), 7), //$NON-NLS-1$
	ULTRA(Messages.getString("SevenZipOptions.ULTRA"), 9); //$NON-NLS-1$

	private String desc;
	private int level;

	private SevenZipOptions(String desc, int level)
	{
		this.desc = desc;
		this.level = level;
	}

	public String getName()
	{
		return desc;
	}

	public int getLevel()
	{
		return level;
	}
}
