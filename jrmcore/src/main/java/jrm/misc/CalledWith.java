package jrm.misc;

@FunctionalInterface
public interface CalledWith<T>
{
	public void call(final T t) throws Exception;	//NOSONAR
}