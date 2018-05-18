package jrm.profile.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings("serial")
public class SamplesList implements Serializable, ByName<Samples>, Iterable<Samples>
{
	protected final HashMap<String, Samples> samplesets = new HashMap<>();

	public SamplesList()
	{
	}

	@Override
	public boolean containsName(String name)
	{
		return samplesets.containsKey(name);
	}

	@Override
	public Samples getByName(String name)
	{
		return samplesets.get(name);
	}

	@Override
	public Samples putByName(Samples t)
	{
		return samplesets.put(t.name, t);
	}

	@Override
	public Iterator<Samples> iterator()
	{
		return samplesets.values().iterator();
	}

	public int size()
	{
		return samplesets.size();
	}
}
