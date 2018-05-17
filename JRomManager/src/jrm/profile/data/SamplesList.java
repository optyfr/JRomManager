package jrm.profile.data;

import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("serial")
public class SamplesList implements Serializable, ByName<Samples>
{
	public final HashMap<String, Samples> samplesets = new HashMap<>();

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

}
