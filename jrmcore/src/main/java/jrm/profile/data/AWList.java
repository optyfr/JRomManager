package jrm.profile.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Stream;

interface AWList<T> extends List<T>
{

	/**
	 * return a non filtered list of {@link T}
	 * @return {@link List}&lt;{@link T}&gt;
	 */
	public abstract List<T> getList();

	@Override
	public default int size()
	{
		return getList().size();
	}

	@Override
	public default boolean isEmpty()
	{
		return getList().isEmpty();
	}

	@Override
	public default boolean contains(final Object o)
	{
		return getList().contains(o);
	}

	@Override
	public default Iterator<T> iterator()
	{
		return getList().iterator();
	}

	@Override
	public default Object[] toArray()
	{
		return getList().toArray();
	}

	@Override
	public default <E> E[] toArray(final E[] a)
	{
		return getList().toArray(a);
	}

	@Override
	public default boolean add(final T e)
	{
		return getList().add(e);
	}

	@Override
	public default boolean remove(final Object o)
	{
		return getList().remove(o);
	}

	@Override
	public default boolean containsAll(final Collection<?> c)
	{
		return getList().containsAll(c);
	}

	@Override
	public default boolean addAll(final Collection<? extends T> c)
	{
		return getList().addAll(c);
	}

	@Override
	public default boolean addAll(final int index, final Collection<? extends T> c)
	{
		return getList().addAll(index, c);
	}

	@Override
	public default boolean removeAll(final Collection<?> c)
	{
		return getList().removeAll(c);
	}

	@Override
	public default boolean retainAll(final Collection<?> c)
	{
		return getList().retainAll(c);
	}

	@Override
	public default void clear()
	{
		getList().clear();
	}

	@Override
	public default T get(final int index)
	{
		return getList().get(index);
	}

	@Override
	public default T set(final int index, final T element)
	{
		return getList().set(index, element);
	}

	@Override
	public default void add(final int index, final T element)
	{
		getList().add(index, element);
	}

	@Override
	public default T remove(final int index)
	{
		return getList().remove(index);
	}

	@Override
	public default int indexOf(final Object o)
	{
		return getList().indexOf(o);
	}

	@Override
	public default int lastIndexOf(final Object o)
	{
		return getList().lastIndexOf(o);
	}

	@Override
	public default ListIterator<T> listIterator()
	{
		return getList().listIterator();
	}

	@Override
	public default ListIterator<T> listIterator(final int index)
	{
		return getList().listIterator(index);
	}

	@Override
	public default List<T> subList(final int fromIndex, final int toIndex)
	{
		return getList().subList(fromIndex, toIndex);
	}

	/**
	 * get a cached filtered list
	 * @return {@link List}&lt;{@link T}&gt;
	 */
	public abstract List<T> getFilteredList();

	/**
	 * get a cached and filtered stream
	 * @return {@link Stream}&lt;{@link T}&gt;
	 */
	public abstract Stream<T> getFilteredStream();


}
