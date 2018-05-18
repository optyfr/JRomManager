package jrm.profile.data;

import java.io.Serializable;
import java.lang.reflect.Field;

@SuppressWarnings("serial")
public abstract class EntityBase extends NameBase implements Serializable
{
	protected EntityStatus own_status = EntityStatus.UNKNOWN;
	protected final AnywareBase parent;

	public EntityBase(AnywareBase parent)
	{
		this.parent = parent;
	}

	public abstract EntityStatus getStatus();

	public void setStatus(EntityStatus status)
	{
		this.own_status = status;
	}

	public <T extends AnywareBase> T getParent(final Class<T> type)
	{
		return type.cast(parent);
	}

	public abstract AnywareBase getParent();

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
