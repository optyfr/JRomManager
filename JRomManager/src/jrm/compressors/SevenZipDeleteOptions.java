package jrm.compressors;

public enum SevenZipDeleteOptions
{
	SEVENZIP_STORE("Delete with '7z' command using STORE method","d -y -t7z -ms=off -mx=0 %1 %2"),
	SEVENZIP_FAST("Delete with '7z' command using FAST presets","d -y -t7z -ms=off -mx=1 %1 %2"),
	SEVENZIP_NORMAL("Delete with '7z' command using NORMAL presets","d -y -t7z -ms=off -mx=5 %1 %2"),
	SEVENZIP_ULTRA("Delete with '7z' command using ULTRA presets","d -y -t7z -ms=off -mx=9 %1 %2");
	
	private String desc;
	private String args;
	
	private SevenZipDeleteOptions(String desc, String args)
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
