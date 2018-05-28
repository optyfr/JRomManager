package jrm.compressors.zipfs;

public enum ZipTempThreshold
{
	_NEVER("Never (Safe mode)", -1L),
	_1MB("Above 1MB", 1_000_000L),
	_2MB("Above 2MB", 2_000_000L),
	_5MB("Above 5MB", 5_000_000L),
	_10MB("Above 10MB (Recommended limit)", 10_000_000L),
	_25MB("Above 25MB", 25_000_000L),
	_50MB("Above 50MB", 50_000_000L),
	_100MB("Above 100MB", 100_000_000L),
	_250MB("Above 250MB", 250_000_000L),
	_500MB("Above 500MB", 500_000_000L);
	
	String name;
	long threshold;
	
	private ZipTempThreshold(String name, long threshold)
	{
		this.name = name;
		this.threshold = threshold;
	}


	public String getName()
	{
		return name;
	}

	public long getThreshold()
	{
		return threshold;
	}
}
