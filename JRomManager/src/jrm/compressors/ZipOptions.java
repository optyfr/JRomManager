package jrm.compressors;

public enum ZipOptions
{
	SEVENZIP_STORE("'7z' command using STORE method","-mx=0"),
	SEVENZIP_FAST("'7z' command using FAST presets","-mx=1"),
	SEVENZIP_NORMAL("'7z' command using NORMAL presets","-mx=5"),
	SEVENZIP_ULTRA("'7z' command using ULTRA presets","-mx=9"),
	ZIP_STORE("'zip' command using STORE method","-0"),
	ZIP_FAST("'zip' command using FAST presets","-1"),
	ZIP_NORMAL("'zip' command using NORMAL presets","-5"),
	ZIP_ULTRA("'zip' command using ULTRA presets","-9");
	
	private String desc;
	private String args;
	
	private ZipOptions(String desc, String args)
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
