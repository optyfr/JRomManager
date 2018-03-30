package jrm.compressors;

public enum SevenZipExtractOptions
{
	SEVENZIP("Extract with '7z' command","x -y -o%4 %1");
	
	private String desc;
	private String args;
	
	private SevenZipExtractOptions(String desc, String args)
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
