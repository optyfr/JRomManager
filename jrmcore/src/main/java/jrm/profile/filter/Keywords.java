package jrm.profile.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jrm.profile.data.Anyware;
import jrm.profile.data.AnywareList;

public abstract class Keywords {
	final Pattern pattern = Pattern.compile("^(.*?)(\\(.*\\))++"); //$NON-NLS-1$
	final Pattern patternParenthesis = Pattern.compile("\\((.*?)\\)"); //$NON-NLS-1$
	final Pattern patternSplit = Pattern.compile(","); //$NON-NLS-1$
	final Pattern patternAlpha = Pattern.compile("^[a-zA-Z]*$"); //$NON-NLS-1$
	final HashSet<String> keywordSet = new HashSet<>();

	/**
	 * The Interface CallBack.
	 */
	public interface KFCallBack
	{
		
		/**
		 * Call.
		 *
		 * @param filter the filter
		 */
		public void call(AnywareList<? extends Anyware> list, List<String> filter);
	}

	/**
	 * The Class keypref.
	 */
	private class KeyPref
	{
		
		/** The order. */
		int order;
		
		/** The wares. */
		List<Anyware> wares = new ArrayList<>();

		/**
		 * Instantiates a new keypref.
		 *
		 * @param order the order
		 * @param ware the ware
		 */
		private KeyPref(int order, Anyware ware)
		{
			this.order = order;
			add(ware);
		}
		
		/**
		 * Adds the.
		 *
		 * @param ware the ware
		 */
		private void add(Anyware ware)
		{
			ware.setSelected(true);
			this.wares.add(ware);
		}
		
		/**
		 * Clear.
		 */
		private void clear()
		{
			wares.forEach(w -> w.setSelected(false));
			wares.clear();
		}
	}

	/**
	 * 
	 */
	public void filter(AnywareList<? extends Anyware> list)
	{
		list.getFilteredStream().forEach(ware -> {
			final var matcher = pattern.matcher(ware.getDescription());
			if (matcher.find() && matcher.groupCount() > 1 && matcher.group(2) != null)
			{
				final var matcherParenthesis = patternParenthesis.matcher(matcher.group(2));
				while (matcherParenthesis.find())
				{
					Arrays.asList(patternSplit.split(matcherParenthesis.group(1))).stream().map(s -> s.trim().toLowerCase()).filter(patternAlpha.asPredicate()).forEach(keywordSet::add);
				}
			}
		});
		showFilter(keywordSet.stream().sorted(String::compareToIgnoreCase).toArray(String[]::new), this::filterCallBack);
	}

	protected abstract void showFilter(String[] keywords, KFCallBack callback);
	
	protected abstract void updateList();
	
	/**
	 * @param f
	 */
	public void filterCallBack(AnywareList<? extends Anyware> list, List<String> filter)
	{
		HashMap<String, KeyPref> prefmap = new HashMap<>();
		list.getFilteredStream().forEach(ware -> {
			final var matcher = pattern.matcher(ware.getDescription());
			keywordSet.clear();
			if (matcher.find())
			{
				if (matcher.groupCount() > 1 && matcher.group(2) != null)
				{
					final var matcherParenthesis = patternParenthesis.matcher(matcher.group(2));
					while (matcherParenthesis.find())
					{
						Arrays.asList(patternSplit.split(matcherParenthesis.group(1))).stream().map(s -> s.trim().toLowerCase()).filter(patternAlpha.asPredicate()).forEach(keywordSet::add);
					}
				}
//				ware.setSelected(false);
				selectFromKeywords(filter, prefmap, ware, matcher);
			}
			else
			{
//				prefmap.computeIfAbsent(ware.getDescription().toString(), k -> new KeyPref(Integer.MAX_VALUE, ware));
			}
		});
		
		updateList();
		//list.fireTableChanged(new TableModelEvent(list, 0, list.getRowCount() - 1, TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE));
	}

	/**
	 * @param filter
	 * @param prefmap
	 * @param ware
	 * @param matcher
	 */
	private void selectFromKeywords(final List<String> filter, HashMap<String, KeyPref> prefmap, Anyware ware, final Matcher matcher)
	{
		for (var i = 0; i < filter.size(); i++)
		{
			if (keywordSet.contains(filter.get(i)))
			{
				final var pos = i;
				prefmap.compute(matcher.group(1), (key, pref) -> {
					if (pref == null)
						return new KeyPref(pos, ware);
					else if (pos < pref.order)
					{
						pref.clear();
						return new KeyPref(pos, ware);
					}
					else if (pos == pref.order)
						pref.add(ware);
					return pref;
				});
				break;
			}
		}
	}
	

}
