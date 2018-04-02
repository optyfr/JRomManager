package jrm.profiler.scan;

public enum HashCollisionOptions
{
	SINGLEFILE("Single file"),
	SINGLECLONE("Single clone"),
	ALLCLONES("All clones"),
	DUMB("All clones even if no collision");
	
	private String name;
	
	private HashCollisionOptions(String name)
	{
		this.name = name;
	}
	
	public String getDesc()
	{
		return name;
	}
}
