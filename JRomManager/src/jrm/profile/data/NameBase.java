package jrm.profile.data;

import java.io.Serializable;

/**
 * The base class for named data entities
 * @author optyfr
 */
@SuppressWarnings("serial")
abstract class NameBase implements Serializable, Comparable<NameBase>
{
	/**
	 * The name of the entity
	 */
	protected String name = ""; // required //$NON-NLS-1$

	/**
	 * get the name of the entity, may be forged depending on its extending class
	 * @return the name of the entity
	 */
	public abstract String getName();

	/**
	 * get the untouched (non forged) name of the entity
	 * @return the name as defined initially by {@link #setName(String)}
	 */
	public final String getBaseName()
	{
		return name;
	}

	/**
	 * set the name of the entity
	 * @param name the name to set
	 */
	public final void setName(final String name)
	{
		this.name = name;
	}

	@Override
	public final int compareTo(final NameBase o)
	{
		return name.compareTo(o.name);
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
}
