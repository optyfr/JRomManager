package jrm.profile.scan.options;

import java.util.EnumSet;

import jrm.Messages;

public enum FormatOptions
{
	DIR(Messages.getString("FormatOptions.Directories"), Ext.DIR), //$NON-NLS-1$
	ZIP(Messages.getString("FormatOptions.Zip"), Ext.ZIP), //$NON-NLS-1$
	ZIPE(Messages.getString("FormatOptions.ZipExternal"), Ext.ZIP), //$NON-NLS-1$
	SEVENZIP(Messages.getString("FormatOptions.SevenZip"), Ext.SEVENZIP), //$NON-NLS-1$
	TZIP(Messages.getString("FormatOptions.TorrentZip"), Ext.ZIP); //$NON-NLS-1$

	public enum Ext
	{
		DIR(""), //$NON-NLS-1$
		ZIP(".zip"), //$NON-NLS-1$
		SEVENZIP(".7z"); //$NON-NLS-1$

		private String ext;

		private Ext(String ext)
		{
			this.ext = ext;
		}

		@Override
		public String toString()
		{
			return ext;
		}

		public EnumSet<Ext> allExcept()
		{
			return EnumSet.complementOf(EnumSet.of(this, DIR));
		}

		public boolean isDir()
		{
			return this == DIR;
		}
	}

	private String desc;
	private Ext ext;

	private FormatOptions(String desc, Ext ext)
	{
		this.desc = desc;
		this.ext = ext;
	}

	public Ext getExt()
	{
		return ext;
	}

	public String getDesc()
	{
		return desc;
	}

	public EnumSet<FormatOptions> allExcept()
	{
		return EnumSet.complementOf(EnumSet.of(this, DIR));
	}
}
