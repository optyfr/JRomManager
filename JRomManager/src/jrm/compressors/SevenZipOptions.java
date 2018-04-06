package jrm.compressors;

public enum SevenZipOptions
{
	STORE("STORE method",0),
	FAST("FAST presets",1),
	NORMAL("NORMAL presets",5),
	ULTRA("ULTRA presets",9);
	
	private String desc;
	private int level;
	
	private SevenZipOptions(String desc, int level)
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
