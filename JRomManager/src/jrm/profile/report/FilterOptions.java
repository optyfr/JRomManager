package jrm.profile.report;

/**
 * Report filtering options
 * @author optyfr
 *
 */
public enum FilterOptions
{
	/**
	 * Show OK containers (that do not need to be rebuild)
	 */
	SHOWOK,
	/**
	 * Hide totally missing containers (that can't be rebuild)
	 */
	HIDEMISSING
}
