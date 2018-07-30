package jrm.profile.data;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * The abstract base class for {@link EntityBase} and {@link Sample}, main purpose is to define parent relationship and scan status
 * @author optyfr
 */
@SuppressWarnings("serial")
public abstract class EntityBase extends NameBase implements Serializable
{
	/**
	 * The scan status, defaulting to {@link EntityStatus#UNKNOWN}
	 */
	protected EntityStatus own_status = EntityStatus.UNKNOWN;
	/**
	 * The parent {@link AnywareBase}
	 */
	protected final AnywareBase parent;

	/**
	 * The constructor with its required parent
	 * @param parent the required {@link AnywareBase} parent 
	 */
	protected EntityBase(AnywareBase parent)
	{
		this.parent = parent;
	}

	/**
	 * get the entity status
	 * @return an {@link EntityStatus} value
	 */
	public abstract EntityStatus getStatus();

	/**
	 * set the {@link Entity} status
	 * @param status the {@link EntityStatus} to set
	 */
	public void setStatus(EntityStatus status)
	{
		this.own_status = status;
	}

	/**
	 * get the parent casted according the given class
	 * @param type the class to cast, must extends {@link AnywareBase}
	 * @param <T> a class which extends {@link AnywareBase}
	 * @return the type casted parent
	 */
	protected <T extends AnywareBase> T getParent(final Class<T> type)
	{
		return type.cast(parent);
	}

	/**
	 * get the parent
	 * @return the parent (can't be null)
	 */
	public abstract AnywareBase getParent();

	/**
	 * special method to get the value of a field outside its scope<br>
	 * <center><b style='color:red'>*** USE WITH CAUTION ***</b></center> 
	 * @param name the property name as a string (case sensitive)
	 * @return the value as an {@link Object} or null;
	 */
	public Object getProperty(String name)
	{
		try
		{
			Field field  = this.getClass().getField(name);
			field.setAccessible(true);
			return field.get(this);
		}
		catch(NoSuchFieldException e)
		{
			return null;
		}
		catch (SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
