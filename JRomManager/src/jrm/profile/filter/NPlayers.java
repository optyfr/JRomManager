package jrm.profile.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import javax.swing.AbstractListModel;

import org.apache.commons.lang3.StringUtils;

import jrm.Messages;
import jrm.profile.data.PropertyStub;

/**
 * nplayers.ini management class and {@link AbstractListModel}
 * @author optyfr
 */
@SuppressWarnings("serial")
public final class NPlayers extends AbstractListModel<jrm.profile.filter.NPlayers.NPlayer> implements Iterable<NPlayers.NPlayer>
{
	/**
	 * nplayer mode which list games compatible with that mode 
	 * @author optyfr
	 */
	public final class NPlayer implements List<String>,PropertyStub
	{
		/**
		 * the name of the nplayer mode
		 */
		final private String name;
		/**
		 * The {@link List} of compatible games identified by code names
		 */
		final private List<String> games = new ArrayList<>();

		/**
		 * The NPlayer constructor
		 * @param name the mode name
		 */
		public NPlayer(final String name)
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
		public boolean contains(final Object o)
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

		@Override
		public String toString()
		{
			return name + " (" + games.size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		@Override
		public String getPropertyName()
		{
			return "filter.nplayer."+name; //$NON-NLS-1$
		}
	}

	/**
	 * The {@link List} of {@link NPlayer} modes
	 */
	final private List<NPlayer> list_nplayers = new ArrayList<>();
	/**
	 * The location of the nplayers.ini {@link File}
	 */
	final public File file;

	/**
	 * The main constructor will read provided nplayers.ini and initialize list of nplayer/games
	 * @param file the nplayers.ini {@link File} to read
	 * @throws IOException
	 */
	private NPlayers(final File file) throws IOException
	{
		try(BufferedReader reader = new BufferedReader(new FileReader(file));)
		{
			final Map<String, NPlayer> nplayers = new TreeMap<>();
			this.file = file;
			String line;
			boolean in_section = false;
			while(null != (line = reader.readLine()))
			{
				if(line.equalsIgnoreCase("[NPlayers]")) //$NON-NLS-1$
					in_section = true;
				else if(line.startsWith("[") && in_section) //$NON-NLS-1$
					break;
				else if(in_section)
				{
					final String[] kv = StringUtils.split(line, '=');
					if(kv.length == 2)
					{
						final String k = kv[0].trim();
						final String v = kv[1].trim();
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
				throw new IOException(Messages.getString("NPlayers.NoNPlayersData")); //$NON-NLS-1$
		}
	}

	/**
	 * static method shortcut to constructor
	 * @param file the nplayers.ini to read
	 * @return an initialized {@link NPlayers}
	 * @throws IOException
	 */
	public static NPlayers read(final File file) throws IOException
	{
		return new NPlayers(file);
	}

	@Override
	public int getSize()
	{
		return list_nplayers.size();
	}

	@Override
	public NPlayer getElementAt(final int index)
	{
		return list_nplayers.get(index);
	}

	@Override
	public Iterator<NPlayer> iterator()
	{
		return list_nplayers.iterator();
	}

}
