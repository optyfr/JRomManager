package jrm.compressors;

public enum ZipDeleteOptions
{
	SEVENZIP_DELETE("Delete with '7z' command","d -y -tzip %1 %2"),
	ZIP_DELETE("Delete with 'zip' command","-d %1 %2");
	
	private String desc;
	private String args;
	
	private ZipDeleteOptions(String desc, String args)
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
