package jrm.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Merge overlapping intervals
 * Originally came from <a href="https://stackoverflow.com/questions/31670849/merge-overlapping-intervals">here</a>
 * @author Gosu
 *
 */
public final class Ideone
{
	private final ArrayList<Interval> list = new ArrayList<>();
	
	/**
	 * Interval with a {@link #start} and an {@link #end}
	 */
	public final static class Interval
	{
		private final int start;
		private final int end;

		Interval()
		{
			start = 0;
			end = 0;
		}

		Interval(final int s, final int e)
		{
			start = s;
			end = e;
		}

		public int getStart()
		{
			return start;
		}

		public int getEnd()
		{
			return end;
		}
	}

	/**
	 * And an Interval
	 * @param start
	 * @param end
	 */
	public void add(final int start, final int end)
	{
		list.add(new Interval(start, end));
	}
	
	/**
	 * Merge all added intervals into a list of non overlapped intervals
	 * @return an {@link ArrayList} of non overlapped intervals
	 */
	public ArrayList<Interval> merge()
	{
		return merge(list);
	}
	
	/**
	 * Merge all provided intervals into a list of non overlapped intervals
	 * @param intervals to merge
	 * @return an {@link ArrayList} of non overlapped intervals
	 */
	public static ArrayList<Interval> merge(final ArrayList<Interval> intervals)
	{
		if (intervals.size() <= 1)
			return intervals;

		Collections.sort(intervals, new Comparator<Interval>()
		{
			@Override
			public int compare(final Interval i1, final Interval i2)
			{
				return i1.getStart() - i2.getStart();
			}
		});

		final Interval first = intervals.get(0);
		int start = first.getStart();
		int end = first.getEnd();

		final ArrayList<Interval> result = new ArrayList<>();

		for (int i = 1; i < intervals.size(); i++)
		{
			final Interval current = intervals.get(i);
			if (current.getStart() <= end)
			{
				end = Math.max(current.getEnd(), end);
			}
			else
			{
				result.add(new Interval(start, end));
				start = current.getStart();
				end = current.getEnd();
			}
		}

		result.add(new Interval(start, end));
		return result;
	}
}
