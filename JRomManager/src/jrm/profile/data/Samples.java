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

	public Sample add(Sample sample)
	{
		if (!samples.containsKey(sample.name))
		{
			samples.put(sample.name, sample);
			return sample;
		}
		return samples.get(sample.name);
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
		return ""; //$NON-NLS-1$
	}

	@Override
	public Iterator<Sample> iterator()
	{
		return samples.values().iterator();
	}

	public AnywareStatus getStatus()
	{
		AnywareStatus status = AnywareStatus.COMPLETE;
		boolean ok = false;
		for(final Sample sample : this)
		{
			final EntityStatus estatus = sample.getStatus();
			if(estatus == EntityStatus.KO)
				status = AnywareStatus.PARTIAL;
			else if(estatus == EntityStatus.OK)
				ok = true;
			else if(estatus == EntityStatus.UNKNOWN)
			{
				status = AnywareStatus.UNKNOWN;
				break;
			}
		}
		if(status == AnywareStatus.PARTIAL && !ok)
			status = AnywareStatus.MISSING;
		return status;
	}
}
