package jrm.compressors;

public enum ZipAddOptions
{
	SEVENZIP_STORE("Add with '7z' command using STORE method","a -y -r -tzip -mx=0 %1 %2"),
	SEVENZIP_FAST("Add with '7z' command using FAST presets","a -y -r -tzip -mx=1 %1 %2"),
	SEVENZIP_NORMAL("Add with '7z' command using NORMAL presets","a -y -r -tzip -mx=5 %1 %2"),
	SEVENZIP_ULTRA("Add with '7z' command using ULTRA presets","a -y -r -tzip -mx=9 %1 %2"),
	ZIP_STORE("Add with 'zip' command using STORE method","-r -0 %1 %2"),
	ZIP_FAST("Add with 'zip' command using FAST presets","-r -1 %1 %2"),
	ZIP_NORMAL("Add with 'zip' command using NORMAL presets","-r -5 %1 %2"),
	ZIP_ULTRA("Add with 'zip' command using ULTRA presets","-r -9 %1 %2");
	
	private String desc;
	private String args;
	
	private ZipAddOptions(String desc, String args)
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
