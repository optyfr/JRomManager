package jrm.profile.data;

import java.io.Serializable;

/**
 * The scan status of an {@link EntityBase}
 * @author optyfr
 *
 */
public enum EntityStatus implements Serializable
{
	/**
	 * unknown, not scanned
	 */
	UNKNOWN,
	/**
	 * not found or wrong
	 */
	KO,
	/**
	 * found and good
	 */
	OK
}
