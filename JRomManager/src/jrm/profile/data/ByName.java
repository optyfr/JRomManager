package jrm.profile.data;

public interface ByName<T>
{
	public boolean containsName(String name);
	public T getByName(String name);
}
