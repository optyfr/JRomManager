package jrm.profile.data;

import java.io.Serializable;

/**
 * The abstract base class for {@link Anyware}, its main purpose is to define parent relationship
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public abstract class AnywareBase extends NameBase implements Serializable
{
	/**
	 * {@link AnywareBase} can have parent of the same type
	 */
	public AnywareBase parent = null;

	/**
	 * get the parent casted according the given class
	 * @param type the class to cast, must extends {@link AnywareBase}
	 * @param <T> a class which extends {@link AnywareBase}
	 * @return the type casted parent
	 */
	public <T extends AnywareBase> T getParent(final Class<T> type)
	{
		return type.cast(parent);
	}
	
	/**
	 * set the parent
	 * @param parent an object which is an instance of {@link AnywareBase}
	 * @param <T> a class which extends {@link AnywareBase}
	 */
	public <T extends AnywareBase> void setParent(T parent)
	{
		this.parent = parent;
	}

	/**
	 * get the parent
	 * @return the parent or null if not set
	 */
	public abstract AnywareBase getParent();

	/**
	 * get the name of this object, eventually concatenated with its list name
	 * @return the full name of this object as a {@link String}
	 */
	public abstract String getFullName();
	/**
	 * get the extended filename of this object, it will eventually concatenate with its list name
	 * @param filename the filename to use
	 * @return the full filename of this object as a {@link String}
	 */
	public abstract String getFullName(final String filename);
	/**
	 * get the description of this object
	 * @return a {@link CharSequence} containing the description
	 */
	public abstract CharSequence getDescription();
	/**
	 * get the status of this object
	 * @return an {@link AnywareStatus} describing the status of this object
	 */
	public abstract AnywareStatus getStatus();


}
