package jrm.profile.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import lombok.Getter;

@SuppressWarnings("serial")
public class Sources implements Serializable, Iterable<Source>
{
	private final @Getter ArrayList<Source> srces = new ArrayList<>();

	public boolean add(final Source source)
	{
		return srces.add(source);
	}

	@Override
	public Iterator<Source> iterator()
	{
		return srces.iterator();
	}
}
