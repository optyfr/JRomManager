package jrm.profile.scan.options;

import java.util.EnumSet;

import jrm.Messages;

/**
 * All possible merge options
 * @author optyfr
 *
 */
public enum MergeOptions
{
	/**
	 * merge clones and bioses into parent
	 */
	FULLMERGE(Messages.getString("MergeOptions.FullMerge")), //$NON-NLS-1$
	/**
	 * merge clones into parent
	 */
	MERGE(Messages.getString("MergeOptions.Merge")), //$NON-NLS-1$
	/**
	 * no merge (keep individual), and include bios + devices
	 */
	SUPERFULLNOMERGE(Messages.getString("MergeOptions.NoMergeInclBiosAndDevices")), //$NON-NLS-1$
	/**
	 * no merge (keep individual), and include bios but not devices
	 */
	FULLNOMERGE(Messages.getString("MergeOptions.NoMergeInclBios")), //$NON-NLS-1$
	/**
	 * no merge (keep individual), excluding bios and devices
	 */
	NOMERGE(Messages.getString("MergeOptions.NoMerge")), //$NON-NLS-1$
	/**
	 * split all
	 */
	SPLIT(Messages.getString("MergeOptions.Split")); //$NON-NLS-1$

	/**
	 * the name of the option
	 */
	private String name;

	/**
	 * internal constructor
	 * @param name the name of the option
	 */
	private MergeOptions(String name)
	{
		this.name = name;
	}

	/**
	 * get description
	 * @return description {@link String}
	 */
	public String getDesc()
	{
		return name;
	}

	/**
	 * Is is a merge option?
	 * @return true if current option is either {@link #MERGE} or {@link #FULLMERGE}
	 */
	public boolean isMerge()
	{
		return EnumSet.of(MERGE, FULLMERGE).contains(this);
	}
}
