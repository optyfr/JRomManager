package jrm.compressors;

public enum SevenZipRenameOptions
{
	SEVENZIP_STORE("Rename with '7z' command using STORE method","rn -y -t7z -ms=off -mx=0 %1 %2 %3"),
	SEVENZIP_FAST("Rename with '7z' command using FAST presets","rn -y -t7z -ms=off -mx=1 %1 %2 %3"),
	SEVENZIP_NORMAL("Rename with '7z' command using NORMAL presets","rn -y -t7z -ms=off -mx=5 %1 %2 %3"),
	SEVENZIP_ULTRA("Rename with '7z' command using ULTRA presets","rn -y -t7z -ms=off -mx=9 %1 %2 %3");
	
	private String desc;
	private String args;
	
	private SevenZipRenameOptions(String desc, String args)
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
