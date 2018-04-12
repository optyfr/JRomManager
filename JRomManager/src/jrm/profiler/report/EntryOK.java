package jrm.profiler.report;

import jrm.Messages;
import jrm.profiler.data.Entity;

public class EntryOK extends Note
{
	Entity entity;
	
	public EntryOK(Entity entity)
	{
		this.entity = entity;
	}

	@Override
	public String toString()
	{
		return String.format(Messages.getString("EntryOK.OK"), parent.machine.name, entity.getName()); //$NON-NLS-1$
	}

}
