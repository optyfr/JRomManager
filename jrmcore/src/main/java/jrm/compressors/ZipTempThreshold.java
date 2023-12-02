package jrm.compressors;

import jrm.locale.Messages;

public enum ZipTempThreshold
{
	_NEVER(Messages.getString("ZipTempThreshold.Never"), -1L), //$NON-NLS-1$ // NOSONAR
	_1MB(Messages.getString("ZipTempThreshold.1MB"), 1_000_000L), //$NON-NLS-1$ // NOSONAR
	_2MB(Messages.getString("ZipTempThreshold.2MB"), 2_000_000L), //$NON-NLS-1$ // NOSONAR
	_5MB(Messages.getString("ZipTempThreshold.5MB"), 5_000_000L), //$NON-NLS-1$ // NOSONAR
	_10MB(Messages.getString("ZipTempThreshold.10MB"), 10_000_000L), //$NON-NLS-1$ // NOSONAR
	_25MB(Messages.getString("ZipTempThreshold.25MB"), 25_000_000L), //$NON-NLS-1$ // NOSONAR
	_50MB(Messages.getString("ZipTempThreshold.50MB"), 50_000_000L), //$NON-NLS-1$ // NOSONAR
	_100MB(Messages.getString("ZipTempThreshold.100MB"), 100_000_000L), //$NON-NLS-1$ // NOSONAR
	_250MB(Messages.getString("ZipTempThreshold.250MB"), 250_000_000L), //$NON-NLS-1$ // NOSONAR
	_500MB(Messages.getString("ZipTempThreshold.500MB"), 500_000_000L); //$NON-NLS-1$ // NOSONAR
	
	String desc;
	long threshold;
	
	private ZipTempThreshold(String name, long threshold)
	{
		this.desc = name;
		this.threshold = threshold;
	}

	public String getDesc()
	{
		return desc;
	}

	public long getThreshold()
	{
		return threshold;
	}
}
