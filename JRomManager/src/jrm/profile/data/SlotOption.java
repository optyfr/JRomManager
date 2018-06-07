package jrm.profile.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class SlotOption extends NameBase implements Serializable
{
	public String devname;
	public boolean def = false;
	
	@Override
	public String getName()
	{
		return name;
	}
}
