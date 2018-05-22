package jrm.profile.data;

import java.io.Serializable;

public interface ByName<T extends NameBase> extends Serializable
{
	public void resetFilteredName();
	public boolean containsFilteredName(String name);
	public boolean containsName(String name);
	public T getFilteredByName(String name);
	public T getByName(String name);
	public abstract T putByName(T t);
}
