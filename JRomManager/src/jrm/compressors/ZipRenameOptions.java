package jrm.compressors;

public enum ZipRenameOptions
{
	SEVENZIP_DELETE("Delete with '7z' command","rn -y -tzip %1 %2 %3");
	
	private String desc;
	private String args;
	
	private ZipRenameOptions(String desc, String args)
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
