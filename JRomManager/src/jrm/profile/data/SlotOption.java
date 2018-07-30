package jrm.profile.data;

import java.io.Serializable;

/**
 * A slot option with a device name and a default flags
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class SlotOption extends NameBase implements Serializable
{
	/**
	 * name of the used device
	 */
	public String devname;
	/**
	 * is this the default slot option
	 */
	public boolean def = false;
	
	@Override
	public String getName()
	{
		return name;
	}
}
