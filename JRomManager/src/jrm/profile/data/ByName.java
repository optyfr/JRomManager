package jrm.profile.data;

import java.io.Serializable;

/**
 * Interface describing methods related to entity name manipulation
 * @author optyfr
 *
 * @param <T> any class that extends {@link NameBase}
 */
public interface ByName<T extends NameBase> extends Serializable
{
	/**
	 * tells reevaluate the named map filtered cache
	 */
	public void resetFilteredName();
	/**
	 * Ask if this name is contained in the named map filtered cache
	 * @param name the name to search
	 * @return true if it was found
	 */
	public boolean containsFilteredName(String name);
	/**
	 * Ask if this name is contained in the named map non-filtered cache
	 * @param name the name to search
	 * @return true if it was found
	 */
	public boolean containsName(String name);
	/**
	 * get the {@link T} by its name from the named map filtered cache
	 * @param name the name to search
	 * @return {@link T} or null
	 */
	public T getFilteredByName(String name);
	/**
	 * get the {@link T} by its name from the named map non-filtered cache
	 * @param name the name to search
	 * @return {@link T} or null
	 */
	public T getByName(String name);
	/**
	 * add {@link T} to the named map non-filtered cache
	 * @param t the {@link T} to add
	 * @return the previous {@link T} value stored with this name or null
	 */
	public abstract T putByName(T t);
}
