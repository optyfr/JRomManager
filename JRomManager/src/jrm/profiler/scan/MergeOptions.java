package jrm.profiler.scan;

public enum MergeOptions
{
	FULLMERGE("Full Merge (with BIOS)"),
	MERGE("Merged (no BIOS)"),
	FULLNOMERGE("Non Merged (with BIOS)"),
	NOMERGE("Non Merged (no BIOS)"),
	SPLIT("Split (default)");
	
	private String name;
	
	private MergeOptions(String name)
	{
		this.name = name;
	}
	
	public String getDesc()
	{
		return name;
	}
}
