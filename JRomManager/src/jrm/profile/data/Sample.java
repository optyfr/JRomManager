package jrm.profile.data;

import java.io.Serializable;

/**
 * A Sample as defined in Machines sets
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public final class Sample extends EntityBase implements Serializable
{
	/**
	 * The Sample constructor
	 * @param parent the {@link Samples} parent
	 * @param name the name of the sample (with or without .wav extension)
	 */
	public Sample(AnywareBase parent, String name)
	{
		super(parent);
		setName(name);
	}

	@Override
	public String getName()
	{
		if(!name.endsWith(".wav"))
			return name + ".wav"; //$NON-NLS-1$
		return name;
	}

	@Override
	public EntityStatus getStatus()
	{
		return own_status;
	}

	@Override
	public boolean equals(Object obj)
	{
		return this.toString().equals(obj.toString());
	}

	@Override
	public AnywareBase getParent()
	{
		return parent;
	}

}
