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
package jrm.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Merge overlapping intervals
 * Original idea came from <a href="https://stackoverflow.com/questions/31670849/merge-overlapping-intervals">here</a>
 * @author Gosu
 *
 */
public final class Ideone
{
	private final ArrayList<Interval> list = new ArrayList<>();
	
	/**
	 * Interval with a {@link #start} and an {@link #end}
	 */
	public static final class Interval
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
	 * @param start the starting index for this interval
	 * @param end the ending index for this interval
	 */
	public void add(final int start, final int end)
	{
		list.add(new Interval(start, end));
	}
	
	/**
	 * Merge all added intervals into a list of non overlapped intervals
	 * @return an {@link ArrayList} of non overlapped intervals
	 */
	public List<Interval> merge()
	{
		return merge(list);
	}
	
	/**
	 * Merge all provided intervals into a list of non overlapped intervals
	 * @param intervals to merge
	 * @return an {@link ArrayList} of non overlapped intervals
	 */
	public static List<Interval> merge(final List<Interval> intervals)
	{
		if (intervals.size() <= 1)
			return intervals;

		Collections.sort(intervals, (i1, i2) -> i1.getStart() - i2.getStart());

		final var first = intervals.get(0);
		int start = first.getStart();
		int end = first.getEnd();

		final ArrayList<Interval> result = new ArrayList<>();

		for (var i = 1; i < intervals.size(); i++)
		{
			final var current = intervals.get(i);
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
