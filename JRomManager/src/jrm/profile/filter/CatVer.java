/* Copyright (C) 2018  optyfr
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package jrm.profile.filter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

import jrm.profile.Profile;
import jrm.profile.data.PropertyStub;
import lombok.Getter;

/**
 * catver.ini management class
 * @author optyfr
 */
@SuppressWarnings("serial")
public final class CatVer implements Iterable<jrm.profile.filter.CatVer.Category>, PropertyStub
{
	/**
	 * class describing games category with sub-categories list
	 * @author optyfr
	 */
	public final class Category implements Map<String, Category.SubCategory>, Iterable<Category.SubCategory>, PropertyStub
	{
		/**
		 * Category name
		 */
		public final String name;
		/**
		 * {@link Map} of {@link SubCategory} with {@link SubCategory#name} as key
		 */
		private final Map<String, SubCategory> subcategories = new TreeMap<>();
		/**
		 * {@link List} of {@link SubCategory}
		 */
		private final @Getter List<SubCategory> listSubCategories = new ArrayList<>();

		/**
		 * Build a Category
		 * @param name the name of the category
		 */
		public Category(final String name)
		{
			this.name = name;
		}

		@Override
		public int size()
		{
			return subcategories.size();
		}

		@Override
		public boolean isEmpty()
		{
			return subcategories.isEmpty();
		}

		@Override
		public boolean containsKey(final Object key)
		{
			return subcategories.containsKey(key);
		}

		@Override
		public boolean containsValue(final Object value)
		{
			return subcategories.containsValue(value);
		}

		@Override
		public SubCategory get(final Object key)
		{
			return subcategories.get(key);
		}

		@Override
		public SubCategory put(final String key, final SubCategory value)
		{
			return subcategories.put(key, value);
		}

		@Override
		public SubCategory remove(final Object key)
		{
			return subcategories.remove(key);
		}

		@Override
		public void putAll(final Map<? extends String, ? extends SubCategory> m)
		{
			subcategories.putAll(m);
		}

		@Override
		public void clear()
		{
			subcategories.clear();
		}

		@Override
		public Set<String> keySet()
		{
			return subcategories.keySet();
		}

		@Override
		public Collection<SubCategory> values()
		{
			return subcategories.values();
		}

		@Override
		public Set<Entry<String, SubCategory>> entrySet()
		{
			return subcategories.entrySet();
		}

		public Object getUserObject()
		{
			return String.format("%s (%d)", name, listSubCategories.stream().filter(SubCategory::isSelected).mapToInt(SubCategory::size).sum()); //$NON-NLS-1$
		}

		@Override
		public Iterator<SubCategory> iterator()
		{
			return listSubCategories.iterator();
		}

		@Override
		public String getPropertyName()
		{
			return "filter.cat." + name; //$NON-NLS-1$
		}

		public void setSelected(final boolean selected)
		{
			PropertyStub.super.setSelected(profile, selected);
		}

		public boolean isSelected()
		{
			return PropertyStub.super.isSelected(profile);
		}

		/**
		 * Describing games subcategory with all corresponding games listed by code names
		 * @author optyfr
		 *
		 */
		public final class SubCategory implements List<String>, PropertyStub
		{
			/**
			 * name of the subcategory
			 */
			public final String name;
			/**
			 * the {@link List} of games code names
			 */
			private final List<String> games = new ArrayList<>();

			/**
			 * Build a sub-category
			 * @param name name of the sub-category
			 */
			public SubCategory(final String name)
			{
				this.name = name;
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

			public Object getUserObject()
			{
				return name + " (" + games.size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}

			@Override
			public String getPropertyName()
			{
				return "filter.cat." + Category.this.name + "." + name; //$NON-NLS-1$ //$NON-NLS-2$
			}

			public void setSelected(final boolean selected)
			{
				PropertyStub.super.setSelected(profile, selected);
			}

			public boolean isSelected()
			{
				return PropertyStub.super.isSelected(profile);
			}
		}
	}


	private final Profile profile;
	/**
	 * The list of fetched {@link Category}
	 */
	private final @Getter List<Category> listCategories = new ArrayList<>();
	/**
	 * The {@link File} location to catver.ini
	 */
	public final File file;

	/**
	 * Main constructor, will read provided catver.ini and initialize list of categories/sub-categories/games
	 * @param file the catver.ini {@link File} to read
	 * @throws IOException
	 */
	private CatVer(final Profile profile, final File file) throws IOException
	{
		this.profile = profile;
		try(BufferedReader reader = new BufferedReader(new FileReader(file));)
		{
			final Map<String, Category> categories = new TreeMap<>();
			this.file = file;
			String line;
			boolean in_section = false;
			while(null != (line = reader.readLine()))
			{
				if(line.equalsIgnoreCase("[Category]")) //$NON-NLS-1$
					in_section = true;
				else if(line.startsWith("[") && in_section) //$NON-NLS-1$
					break;
				else if(in_section)
				{
					final String[] kv = StringUtils.split(line, '=');
					if(kv.length == 2)
					{
						final String k = kv[0].trim();
						final String[] v = StringUtils.split(kv[1], '/');
						if(v.length == 2)
						{
							final String c = v[0].trim();
							final String sc = v[1].trim();
							Category cat;
							if(!categories.containsKey(c))
								categories.put(c, cat = new Category(c));
							else
								cat = categories.get(c);
							Category.SubCategory subcat;
							if(!cat.containsKey(sc))
								cat.put(sc, subcat = cat.new SubCategory(sc));
							else
								subcat = cat.get(sc);
							subcat.add(k);
						}
					}
				}
			}
			listCategories.addAll(categories.values());
			for(final Category cat : listCategories)
				cat.listSubCategories.addAll(cat.subcategories.values());
			if(listCategories.isEmpty())
				throw new IOException(profile.session.msgs.getString("CatVer.NoCatVerData")); //$NON-NLS-1$
		}
	}

	/**
	 * static method shortcut to constructor
	 * @param file the catver.ini to read
	 * @return an initialized {@link CatVer}
	 * @throws IOException
	 */
	public static CatVer read(final Profile profile, final File file) throws IOException
	{
		return new CatVer(profile, file);
	}

	public Object getUserObject()
	{
		return String.format("%s (%d)", profile.session.msgs.getString("CatVer.AllCategories"), listCategories.stream().flatMap(c -> c.listSubCategories.stream().filter(Category.SubCategory::isSelected)).mapToInt(Category.SubCategory::size).sum()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public Iterator<Category> iterator()
	{
		return listCategories.iterator();
	}

	@Override
	public String getPropertyName()
	{
		return "filter.cat"; //$NON-NLS-1$
	}

	public void setSelected(final boolean selected)
	{
		PropertyStub.super.setSelected(profile, selected);
	}

	public boolean isSelected()
	{
		return PropertyStub.super.isSelected(profile);
	}

}
