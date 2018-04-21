package jrm.profile.scan.options;

import java.util.EnumSet;

import jrm.Messages;

public enum MergeOptions
{
	FULLMERGE(Messages.getString("MergeOptions.FullMerge")), //$NON-NLS-1$
	MERGE(Messages.getString("MergeOptions.Merge")), //$NON-NLS-1$
	FULLNOMERGE(Messages.getString("MergeOptions.NoMergeInclBios")), //$NON-NLS-1$
	NOMERGE(Messages.getString("MergeOptions.NoMerge")), //$NON-NLS-1$
	SPLIT(Messages.getString("MergeOptions.Split")); //$NON-NLS-1$

	private String name;

	private MergeOptions(String name)
	{
		this.name = name;
	}

	public String getDesc()
	{
		return name;
	}

	public boolean isMerge()
	{
		return EnumSet.of(MERGE, FULLMERGE).contains(this);
	}
}
