package jrm.profile.scan.options;

import jrm.Messages;

public enum HashCollisionOptions
{
	SINGLEFILE(Messages.getString("HashCollisionOptions.SingleFile")), //$NON-NLS-1$
	SINGLECLONE(Messages.getString("HashCollisionOptions.SingleClone")), //$NON-NLS-1$
	ALLCLONES(Messages.getString("HashCollisionOptions.AllClones")), //$NON-NLS-1$
	HALFDUMB(Messages.getString("HashCollisionOptions.AllClonesHalfDumb")), //$NON-NLS-1$
	DUMB(Messages.getString("HashCollisionOptions.AllClonesDumb")), //$NON-NLS-1$
	DUMBER(Messages.getString("HashCollisionOptions.AllClonesDumber")); //$NON-NLS-1$

	private String name;

	private HashCollisionOptions(String name)
	{
		this.name = name;
	}

	public String getDesc()
	{
		return name;
	}
}
