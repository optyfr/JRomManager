package jrm.compressors;

public enum SevenZipOptions
{
	SEVENZIP_STORE("STORE presets","-ms=off -mx=0"),
	SEVENZIP_FAST("FAST presets","-ms=off -mx=1"),
	SEVENZIP_NORMAL("NORMAL presets","-ms=off -mx=5"),
	SEVENZIP_ULTRA("ULTRA presets","-ms=off -mx=9");
	
	private String desc;
	private String args;
	
	private SevenZipOptions(String desc, String args)
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
