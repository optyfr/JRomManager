package jrm.profile.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import javax.swing.AbstractListModel;

import org.apache.commons.lang3.StringUtils;

import jrm.profile.data.PropertyStub;

@SuppressWarnings("serial")
public final class NPlayers extends AbstractListModel<jrm.profile.filter.NPlayers.NPlayer> implements Iterable<NPlayers.NPlayer>
{
	final private Map<String, NPlayer> nplayers = new TreeMap<>();
	final private List<NPlayer> list_nplayers = new ArrayList<>();
	public final File file;

	public final class NPlayer implements List<String>,PropertyStub
	{
		final private String name;
		final private List<String> games = new ArrayList<>();

		public NPlayer(String name)
		{
			this.name = name;
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
		public boolean contains(Object o)
		{
			return games.contains(o);
		}

		@Override
		public Iterator<String> iterator()
		{
			return games.iterator();
		}

		@Override
		public Object[] toArray()
		{
			return games.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a)
		{
			return games.toArray(a);
		}

		@Override
		public boolean add(String e)
		{
			return games.add(e);
		}

		@Override
		public boolean remove(Object o)
		{
			return games.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c)
		{
			return games.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends String> c)
		{
			return games.addAll(c);
		}

		@Override
		public boolean addAll(int index, Collection<? extends String> c)
		{
			return games.addAll(index, c);
		}

		@Override
		public boolean removeAll(Collection<?> c)
		{
			return games.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c)
		{
			return games.retainAll(c);
		}

		@Override
		public void clear()
		{
			games.clear();
		}

		@Override
		public String get(int index)
		{
			return games.get(index);
		}

		@Override
		public String set(int index, String element)
		{
			return games.set(index, element);
		}

		@Override
		public void add(int index, String element)
		{
			games.add(index, element);
		}

		@Override
		public String remove(int index)
		{
			return games.remove(index);
		}

		@Override
		public int indexOf(Object o)
		{
			return games.indexOf(o);
		}

		@Override
		public int lastIndexOf(Object o)
		{
			return games.lastIndexOf(o);
		}

		@Override
		public ListIterator<String> listIterator()
		{
			return games.listIterator();
		}

		@Override
		public ListIterator<String> listIterator(int index)
		{
			return games.listIterator(index);
		}

		@Override
		public List<String> subList(int fromIndex, int toIndex)
		{
			return games.subList(fromIndex, toIndex);
		}

		@Override
		public String toString()
		{
			return name + " (" + games.size() + ")";
		}

		@Override
		public String getPropertyName()
		{
			return "filter.nplayer."+name;
		}
	}

	private NPlayers(File file) throws IOException
	{
		try(BufferedReader reader = new BufferedReader(new FileReader(file));)
		{
			this.file = file;
			String line;
			boolean in_section = false;
			while(null != (line = reader.readLine()))
			{
				if(line.equalsIgnoreCase("[NPlayers]"))
					in_section = true;
				else if(line.startsWith("[") && in_section)
					break;
				else if(in_section)
				{
					String[] kv = StringUtils.split(line, '=');
					if(kv.length == 2)
					{
						String k = kv[0].trim();
						String v = kv[1].trim();
						NPlayer nplayer;
						if(!nplayers.containsKey(v))
							nplayers.put(v, nplayer = new NPlayer(v));
						else
							nplayer = nplayers.get(v);
						nplayer.add(k);
					}
				}
			}
			list_nplayers.addAll(nplayers.values());
			if(list_nplayers.isEmpty())
				throw new IOException("No NPlayers data");
		}
	}

	public static NPlayers read(File file) throws IOException
	{
		return new NPlayers(file);
	}

	@Override
	public int getSize()
	{
		return list_nplayers.size();
	}

	@Override
	public NPlayer getElementAt(int index)
	{
		return list_nplayers.get(index);
	}

	@Override
	public Iterator<NPlayer> iterator()
	{
		return list_nplayers.iterator();
	}

}
