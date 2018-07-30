package jrm.profile.data;

import java.io.Serializable;

/**
 * Standard system
 * @author optyfr
 */
@SuppressWarnings("serial")
public class SystmStandard implements Systm, Serializable
{
	/**
	 * the static STANDARD object
	 */
	public final static SystmStandard STANDARD = new SystmStandard();

	@Override
	public Type getType()
	{
		return Type.STANDARD;
	}

	@Override
	public Systm getSystem()
	{
		return SystmStandard.STANDARD;
	}

	@Override
	public String toString()
	{
		return "["+getType()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getName()
	{
		return "standard"; //$NON-NLS-1$
	}
}
