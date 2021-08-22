package jrm.profile.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

abstract class GamesList implements List<String>
{
	/**
	 * the {@link List} of games code names
	 */
	protected final List<String> games = new ArrayList<>();

	protected GamesList()
	{
	}

	@Override
	public Iterator<String> iterator()
	{
		return games.iterator();
	}

	@Override
	public int size()
	{
		return games.size();
	}

	@Override
	public boolean isEmpty()
	{
		return games.isEmpty();
	}

	@Override
	public boolean contains(final Object o)
	{
		return games.contains(o);
	}

	@Override
	public Object[] toArray()
	{
		return games.toArray();
	}

	@Override
	public <T> T[] toArray(final T[] a)
	{
		return games.toArray(a);
	}

	@Override
	public boolean add(final String e)
	{
		return games.add(e);
	}

	@Override
	public boolean remove(final Object o)
	{
		return games.remove(o);
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		return games.containsAll(c);
	}

	@Override
	public boolean addAll(final Collection<? extends String> c)
	{
		return games.addAll(c);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends String> c)
	{
		return games.addAll(index, c);
	}

	@Override
	public boolean removeAll(final Collection<?> c)
	{
		return games.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		return games.retainAll(c);
	}

	@Override
	public void clear()
	{
		games.clear();
	}

	@Override
	public String get(final int index)
	{
		return games.get(index);
	}

	@Override
	public String set(final int index, final String element)
	{
		return games.set(index, element);
	}

	@Override
	public void add(final int index, final String element)
	{
		games.add(index, element);
	}

	@Override
	public String remove(final int index)
	{
		return games.remove(index);
	}

	@Override
	public int indexOf(final Object o)
	{
		return games.indexOf(o);
	}

	@Override
	public int lastIndexOf(final Object o)
	{
		return games.lastIndexOf(o);
	}

	@Override
	public ListIterator<String> listIterator()
	{
		return games.listIterator();
	}

	@Override
	public ListIterator<String> listIterator(final int index)
	{
		return games.listIterator(index);
	}

	@Override
	public List<String> subList(final int fromIndex, final int toIndex)
	{
		return games.subList(fromIndex, toIndex);
	}


}
