package jrm.profile.scan.options;

import jrm.Messages;

/**
 * All possible hash collision options
 * @author optyfr
 *
 */
public enum HashCollisionOptions
{
	/**
	 * only collisioned entries in clone subfolder
	 */
	SINGLEFILE(Messages.getString("HashCollisionOptions.SingleFile")), //$NON-NLS-1$
	/**
	 * all entries in collisioning clones subfolder
	 */
	SINGLECLONE(Messages.getString("HashCollisionOptions.SingleClone")), //$NON-NLS-1$
	/**
	 * all clones in subfolder as soon there is a collision
	 */
	ALLCLONES(Messages.getString("HashCollisionOptions.AllClones")), //$NON-NLS-1$
	/**
	 * all clones in subfolder as soon there is a collision, with some optimisations
	 */
	HALFDUMB(Messages.getString("HashCollisionOptions.AllClonesHalfDumb")), //$NON-NLS-1$
	/**
	 * all clones in subfolder even if there is no hash collision
	 */
	DUMB(Messages.getString("HashCollisionOptions.AllClonesDumb")), //$NON-NLS-1$
	/**
	 * all clones in subfolder even if there is no hash collision (disk included)
	 */
	DUMBER(Messages.getString("HashCollisionOptions.AllClonesDumber")); //$NON-NLS-1$

	/**
	 * The name of the option
	 */
	private String name;

	/**
	 * internal constructor
	 * @param name the name of the option
	 */
	private HashCollisionOptions(String name)
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
}
