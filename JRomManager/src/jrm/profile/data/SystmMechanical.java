package jrm.profile.data;

import java.io.Serializable;

/**
 * Mechanical system
 * @author optyfr
 */
@SuppressWarnings("serial")
public class SystmMechanical implements Systm, Serializable
{
	/**
	 * the static MECHANICAL object
	 */
	public final static SystmMechanical MECHANICAL = new SystmMechanical();

	@Override
	public Type getType()
	{
		return Type.MECHANICAL;
	}

	@Override
	public Systm getSystem()
	{
		return SystmMechanical.MECHANICAL;
	}

	@Override
	public String toString()
	{
		return "["+getType()+"]"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public String getName()
	{
		return "mechanical"; //$NON-NLS-1$
	}
}
