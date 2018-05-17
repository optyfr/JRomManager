package jrm.profile.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

@SuppressWarnings("serial")
public final class Samples extends AnywareBase implements Serializable, Iterable<Sample>
{
	public HashMap<String, Sample> samples = new HashMap<>();

	public Samples(String name)
	{
		setName(name);
	}

	public void add(Sample sample)
	{
		if (!samples.containsKey(sample.name))
			samples.put(sample.name, sample);
	}

	@Override
	public AnywareBase getParent()
	{
		return parent;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public String getFullName()
	{
		return name;
	}

	@Override
	public String getFullName(String filename)
	{
		return filename;
	}

	@Override
	public CharSequence getDescription()
	{
		return "";
	}

	@Override
	public Iterator<Sample> iterator()
	{
		return samples.values().iterator();
	}

}
