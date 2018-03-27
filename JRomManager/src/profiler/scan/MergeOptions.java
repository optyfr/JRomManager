package profiler.scan;

public enum MergeOptions
{
	FULLMERGE("Full Merge (with BIOS)"),
	MERGE("Merged"),
	NOMERGE("Non Merged"),
	SPLIT("Split (default)");
	
	private String name;
	
	private MergeOptions(String name)
	{
		this.name = name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
}
