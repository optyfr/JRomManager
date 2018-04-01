package jrm.profiler.scan;

public enum FormatOptions
{
	DIR("Directories",null),
	ZIP("Zip archive",".zip"),
	ZIPE("Zip archive (external)",".zip"),
	SEVENZIP("7Zip archive (external)",".7z"),
	TZIP("TorrentZip archive (external)",".zip");
	
	private String desc, ext;
	
	private FormatOptions(String desc, String ext)
	{
		this.desc = desc;
		this.ext = ext;
	}
	
	public String getExt()
	{
		return ext;
	}
	
	public String getDesc()
	{
		return desc;
	}
}
