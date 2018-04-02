package jrm.profiler.scan;

import java.util.EnumSet;

public enum FormatOptions
{
	DIR("Directories",Ext.DIR),
	ZIP("Zip archive",Ext.ZIP),
	ZIPE("Zip archive (external)",Ext.ZIP),
	SEVENZIP("7Zip archive (external)",Ext.SEVENZIP),
	TZIP("TorrentZip archive (external)",Ext.ZIP);
	
	public enum Ext
	{
		DIR(""),
		ZIP(".zip"),
		SEVENZIP(".7z");
		
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
			return EnumSet.complementOf(EnumSet.of(this,DIR));
		}
		
		public boolean isDir()
		{
			return this==DIR;
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
		return EnumSet.complementOf(EnumSet.of(this,DIR));
	}
}
