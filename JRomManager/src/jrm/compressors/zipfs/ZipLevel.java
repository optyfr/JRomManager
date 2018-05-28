package jrm.compressors.zipfs;

import jrm.Messages;

public enum ZipLevel
{
	DEFAULT(Messages.getString("ZipOptions.DEFAULT"), -1), //$NON-NLS-1$
	STORE(Messages.getString("ZipOptions.STORE"), 0), //$NON-NLS-1$
	FASTEST(Messages.getString("ZipOptions.FASTEST"), 1), //$NON-NLS-1$
	FAST(Messages.getString("ZipOptions.FAST"), 3), //$NON-NLS-1$
	NORMAL(Messages.getString("ZipOptions.NORMAL"), 5), //$NON-NLS-1$
	MAXIMUM(Messages.getString("ZipOptions.MAXIMUM"), 7), //$NON-NLS-1$
	ULTRA(Messages.getString("ZipOptions.ULTRA"), 9); //$NON-NLS-1$

	private String desc;
	private int level;

	private ZipLevel(final String desc, final int level)
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