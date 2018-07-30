package jrm.profile.data;

import java.io.Serializable;

/**
 * This interface define System types
 * @author optyfr
 */
public interface Systm extends Serializable, PropertyStub
{
	/**
	 * The types definitions 
	 */
	public enum Type
	{
		/**
		 * Standard machine
		 */
		STANDARD,
		/**
		 * Electro-Mechanical machine
		 */
		MECHANICAL,
		/**
		 * Device pseudo-machine
		 */
		DEVICE,
		/**
		 * BIOS
		 */
		BIOS,
		/**
		 * Software list
		 */
		SOFTWARELIST
	}

	/**
	 * get the type of system
	 * @return return {@link Type}
	 */
	public Type getType();

	/**
	 * get the System
	 * @return {@link Systm}
	 */
	public Systm getSystem();

	/**
	 * get the name of the system
	 * @return the name of the system
	 */
	public String getName();

	@Override
	public default String getPropertyName()
	{
		if(getType()==Type.SOFTWARELIST)
			return "filter.systems.swlist." + getName();
		return "filter.systems." + getName();
	}
}
