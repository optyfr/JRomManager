package jrm.profile.data;

import java.io.Serializable;

/**
 * The scan status of {@link Anyware}
 * @author optyfr
 *
 */
public enum AnywareStatus implements Serializable
{
	/**
	 * Not yet scanned
	 */
	UNKNOWN,
	/**
	 * Not found
	 */
	MISSING,
	/**
	 * Partially found : some roms, disks, samples are available
	 */
	PARTIAL,
	/**
	 * The set is complete, all necessary files are present
	 */
	COMPLETE
}
