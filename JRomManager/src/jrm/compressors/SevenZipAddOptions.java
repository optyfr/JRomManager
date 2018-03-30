package jrm.compressors;

public enum SevenZipAddOptions
{
	SEVENZIP_STORE("Add with '7z' command using STORE method","a -y -r -t7z -ms=off -mx=0 %1 %2"),
	SEVENZIP_FAST("Add with '7z' command using FAST presets","a -y -r -t7z -ms=off -mx=1 %1 %2"),
	SEVENZIP_NORMAL("Add with '7z' command using NORMAL presets","a -y -r -t7z -ms=off -mx=5 %1 %2"),
	SEVENZIP_ULTRA("Add with '7z' command using ULTRA presets","a -y -r -t7z -ms=off -mx=9 %1 %2");
	
	private String desc;
	private String args;
	
	private SevenZipAddOptions(String desc, String args)
	{
		this.desc = desc;
		this.args = args;
	}
	
	public String getName()
	{
		return desc;
	}
	
	@Override
	public String toString()
	{
		return args;
	}
}
