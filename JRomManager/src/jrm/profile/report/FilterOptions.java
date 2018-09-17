package jrm.profile.report;

import java.io.Serializable;

/**
 * Report filtering options
 * @author optyfr
 *
 */
public enum FilterOptions implements Serializable
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
